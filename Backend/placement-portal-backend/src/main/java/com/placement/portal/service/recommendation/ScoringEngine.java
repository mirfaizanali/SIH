package com.placement.portal.service.recommendation;

import com.placement.portal.domain.Job;
import com.placement.portal.domain.JobSkill;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.domain.StudentSkill;
import com.placement.portal.domain.enums.ExperienceLevel;
import com.placement.portal.domain.enums.JobType;
import com.placement.portal.domain.enums.ProficiencyLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pure, stateless scoring engine.
 *
 * <p>This component performs <em>no</em> database I/O. All inputs are passed in
 * by the caller so that the logic remains unit-testable in isolation.</p>
 *
 * <h3>Weight table</h3>
 * <pre>
 *   Skill match  : 40 %
 *   CGPA         : 20 %
 *   Experience   : 15 %
 *   Preference   : 15 %
 *   Recency      : 10 %
 * </pre>
 */
@Slf4j
@Component
public class ScoringEngine {

    // ---------------------------------------------------------------------------
    // Weight constants
    // ---------------------------------------------------------------------------

    private static final double WEIGHT_SKILL      = 0.40;
    private static final double WEIGHT_CGPA       = 0.20;
    private static final double WEIGHT_EXPERIENCE = 0.15;
    private static final double WEIGHT_PREFERENCE = 0.15;
    private static final double WEIGHT_RECENCY    = 0.10;

    // ---------------------------------------------------------------------------
    // Public scoring methods
    // ---------------------------------------------------------------------------

    /**
     * Computes the skill-match score between a student and a job.
     *
     * <p>Algorithm:</p>
     * <ol>
     *   <li>Mandatory match rate = matched mandatory / total mandatory (1.0 if no mandatory skills)</li>
     *   <li>Optional match rate  = matched optional  / total optional  (1.0 if no optional skills)</li>
     *   <li>baseScore = (mandatoryMatchRate * 0.7 + optionalMatchRate * 0.3) * 100</li>
     *   <li>proficiencyBonus: +5 per matched skill with proficiency {@code >= INTERMEDIATE}, capped at 25</li>
     *   <li>return min(100, baseScore + proficiencyBonus)</li>
     * </ol>
     *
     * @param studentSkillNames       set of skill names the student holds
     * @param studentSkillProficiency map of skill name → proficiency level for the student
     * @param mandatoryJobSkillNames  set of mandatory skill names required by the job
     * @param optionalJobSkillNames   set of optional skill names desired by the job
     * @return skill score in [0, 100]
     */
    public double computeSkillScore(
            Set<String> studentSkillNames,
            Map<String, ProficiencyLevel> studentSkillProficiency,
            Set<String> mandatoryJobSkillNames,
            Set<String> optionalJobSkillNames
    ) {
        // --- mandatory match rate ---
        double mandatoryMatchRate;
        if (mandatoryJobSkillNames == null || mandatoryJobSkillNames.isEmpty()) {
            mandatoryMatchRate = 1.0;
        } else {
            long mandatoryMatched = mandatoryJobSkillNames.stream()
                    .filter(skill -> studentSkillNames != null && studentSkillNames.contains(skill))
                    .count();
            mandatoryMatchRate = (double) mandatoryMatched / mandatoryJobSkillNames.size();
        }

        // --- optional match rate ---
        double optionalMatchRate;
        if (optionalJobSkillNames == null || optionalJobSkillNames.isEmpty()) {
            optionalMatchRate = 1.0;
        } else {
            long optionalMatched = optionalJobSkillNames.stream()
                    .filter(skill -> studentSkillNames != null && studentSkillNames.contains(skill))
                    .count();
            optionalMatchRate = (double) optionalMatched / optionalJobSkillNames.size();
        }

        double baseScore = (mandatoryMatchRate * 0.7 + optionalMatchRate * 0.3) * 100.0;

        // --- proficiency bonus ---
        double proficiencyBonus = 0.0;
        Set<String> allJobSkills = new HashSet<>();
        if (mandatoryJobSkillNames != null) allJobSkills.addAll(mandatoryJobSkillNames);
        if (optionalJobSkillNames  != null) allJobSkills.addAll(optionalJobSkillNames);

        for (String skill : allJobSkills) {
            if (studentSkillNames != null && studentSkillNames.contains(skill)) {
                ProficiencyLevel level = studentSkillProficiency != null
                        ? studentSkillProficiency.get(skill) : null;
                if (level != null && level.ordinal() >= ProficiencyLevel.INTERMEDIATE.ordinal()) {
                    proficiencyBonus += 5.0;
                    if (proficiencyBonus >= 25.0) {
                        proficiencyBonus = 25.0;
                        break;
                    }
                }
            }
        }

        return Math.min(100.0, baseScore + proficiencyBonus);
    }

    /**
     * Computes the CGPA score.
     *
     * <ul>
     *   <li>If {@code minCgpa} is null → 70.0 (no requirement)</li>
     *   <li>If student CGPA is null or below minimum → 0.0 (hard disqualifier)</li>
     *   <li>Otherwise → 60 + ((studentCgpa - minCgpa) / (10 - minCgpa)) * 40, capped at 100</li>
     * </ul>
     *
     * @param studentCgpa student's CGPA on a 10-point scale
     * @param minCgpa     job's minimum required CGPA (may be null)
     * @return CGPA score in [0, 100]
     */
    public double computeCgpaScore(BigDecimal studentCgpa, BigDecimal minCgpa) {
        if (minCgpa == null) {
            return 70.0;
        }
        if (studentCgpa == null || studentCgpa.compareTo(minCgpa) < 0) {
            return 0.0;
        }
        double sCgpa = studentCgpa.doubleValue();
        double mCgpa = minCgpa.doubleValue();
        double range = 10.0 - mCgpa;
        if (range <= 0) {
            // minCgpa is 10.0 — only perfect students qualify
            return sCgpa >= 10.0 ? 100.0 : 0.0;
        }
        double score = 60.0 + ((sCgpa - mCgpa) / range) * 40.0;
        return Math.min(100.0, score);
    }

    /**
     * Computes the experience-level fit score.
     *
     * <pre>
     *   FRESHER → 100
     *   JUNIOR  → 80 if priorInternshipsCount >= 1, else 60
     *   MID     → 80 if priorInternshipsCount >= 2, else 40
     *   SENIOR  → 30 (fresh candidates are unlikely for senior roles)
     *   null    → 70 (unknown level — neutral)
     * </pre>
     *
     * @param jobLevel               the experience level required by the job
     * @param priorInternshipsCount  number of completed internships the student has
     * @return experience score in [0, 100]
     */
    public double computeExperienceScore(ExperienceLevel jobLevel, int priorInternshipsCount) {
        if (jobLevel == null) {
            return 70.0;
        }
        return switch (jobLevel) {
            case FRESHER -> 100.0;
            case JUNIOR  -> priorInternshipsCount >= 1 ? 80.0 : 60.0;
            case MID     -> priorInternshipsCount >= 2 ? 80.0 : 40.0;
            case SENIOR  -> 30.0;
        };
    }

    /**
     * Computes the preference-match score based on location and job-type preferences.
     *
     * <pre>
     *   location match  → +40
     *   job-type match  → +60
     *   total: 0, 40, 60, or 100
     * </pre>
     *
     * @param studentPreferredLocations comma-separated preferred locations string (may be null)
     * @param studentPreferredJobTypes  comma-separated preferred job types string (may be null)
     * @param jobLocation               the job's location
     * @param jobType                   the job's type
     * @return preference score: 0, 40, 60, or 100
     */
    public double computePreferenceScore(
            String studentPreferredLocations,
            String studentPreferredJobTypes,
            String jobLocation,
            JobType jobType
    ) {
        double score = 0.0;

        if (studentPreferredLocations != null && jobLocation != null
                && !studentPreferredLocations.isBlank() && !jobLocation.isBlank()) {
            String locationLower = jobLocation.trim().toLowerCase();
            boolean locationMatch = List.of(studentPreferredLocations.split("[,;]+")).stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .anyMatch(pref -> pref.equalsIgnoreCase(locationLower)
                            || locationLower.contains(pref)
                            || pref.contains(locationLower));
            if (locationMatch) {
                score += 40.0;
            }
        }

        if (studentPreferredJobTypes != null && jobType != null
                && !studentPreferredJobTypes.isBlank()) {
            String jobTypeName = jobType.name();
            boolean typeMatch = List.of(studentPreferredJobTypes.split("[,;]+")).stream()
                    .map(String::trim)
                    .anyMatch(pref -> pref.equalsIgnoreCase(jobTypeName));
            if (typeMatch) {
                score += 60.0;
            }
        }

        return score;
    }

    /**
     * Computes the recency score based on how many days remain before the application deadline.
     *
     * <pre>
     *   null deadline         → 50.0
     *   > 30 days remaining   → 40.0  (plenty of time, lower urgency)
     *   > 14 days remaining   → 70.0
     *   > 3  days remaining   → 100.0 (sweet spot — closing soon)
     *   <= 3 days remaining   → 20.0  (near expiry)
     * </pre>
     *
     * @param applicationDeadline the job's application deadline (may be null)
     * @return recency score: 20, 40, 50, 70, or 100
     */
    public double computeRecencyScore(LocalDate applicationDeadline) {
        if (applicationDeadline == null) {
            return 50.0;
        }
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), applicationDeadline);
        if (daysRemaining > 30) {
            return 40.0;
        } else if (daysRemaining > 14) {
            return 70.0;
        } else if (daysRemaining > 3) {
            return 100.0;
        } else {
            return 20.0;
        }
    }

    // ---------------------------------------------------------------------------
    // Composite scoring — Job for Student
    // ---------------------------------------------------------------------------

    /**
     * Produces a {@link MatchScore} for a given student against a specific job.
     *
     * @param student               the student profile
     * @param job                   the job to score
     * @param priorInternshipsCount number of completed internships the student holds
     * @return a MatchScore where {@code entityId} is the job ID
     */
    public MatchScore scoreJobForStudent(StudentProfile student, Job job, int priorInternshipsCount) {
        // Extract student skills
        Set<String> studentSkillNames = new HashSet<>();
        Map<String, ProficiencyLevel> studentSkillProficiency = new HashMap<>();
        if (student.getSkills() != null) {
            for (StudentSkill ss : student.getSkills()) {
                if (ss.getSkillTag() != null && ss.getSkillTag().getName() != null) {
                    String skillName = ss.getSkillTag().getName();
                    studentSkillNames.add(skillName);
                    if (ss.getProficiencyLevel() != null) {
                        studentSkillProficiency.put(skillName, ss.getProficiencyLevel());
                    }
                }
            }
        }

        // Extract job skills (mandatory vs optional)
        Set<String> mandatorySkills = new HashSet<>();
        Set<String> optionalSkills  = new HashSet<>();
        if (job.getJobSkills() != null) {
            for (JobSkill js : job.getJobSkills()) {
                if (js.getSkillTag() != null && js.getSkillTag().getName() != null) {
                    String skillName = js.getSkillTag().getName();
                    if (js.isMandatory()) {
                        mandatorySkills.add(skillName);
                    } else {
                        optionalSkills.add(skillName);
                    }
                }
            }
        }

        double skillScore      = computeSkillScore(studentSkillNames, studentSkillProficiency,
                mandatorySkills, optionalSkills);
        double cgpaScore       = computeCgpaScore(student.getCgpa(), job.getMinCgpa());
        double experienceScore = computeExperienceScore(job.getExperienceLevel(), priorInternshipsCount);
        double preferenceScore = computePreferenceScore(
                student.getPreferredLocations(), student.getPreferredJobTypes(),
                job.getLocation(), job.getJobType());
        double recencyScore    = computeRecencyScore(job.getApplicationDeadline());

        double totalScore = skillScore      * WEIGHT_SKILL
                + cgpaScore       * WEIGHT_CGPA
                + experienceScore * WEIGHT_EXPERIENCE
                + preferenceScore * WEIGHT_PREFERENCE
                + recencyScore    * WEIGHT_RECENCY;

        return new MatchScore(job.getId(), totalScore, skillScore, cgpaScore,
                experienceScore, preferenceScore, recencyScore);
    }

    // ---------------------------------------------------------------------------
    // Composite scoring — Student for Job
    // ---------------------------------------------------------------------------

    /**
     * Produces a {@link MatchScore} for a given student as a candidate for a specific job.
     *
     * <p>The algorithm is identical to {@link #scoreJobForStudent} — only the framing
     * changes: {@code entityId} is the student profile ID rather than the job ID.</p>
     *
     * @param student               the student profile to score
     * @param job                   the job being applied to
     * @param priorInternshipsCount number of completed internships the student holds
     * @return a MatchScore where {@code entityId} is the student profile ID
     */
    public MatchScore scoreStudentForJob(StudentProfile student, Job job, int priorInternshipsCount) {
        // Extract student skills
        Set<String> studentSkillNames = new HashSet<>();
        Map<String, ProficiencyLevel> studentSkillProficiency = new HashMap<>();
        if (student.getSkills() != null) {
            for (StudentSkill ss : student.getSkills()) {
                if (ss.getSkillTag() != null && ss.getSkillTag().getName() != null) {
                    String skillName = ss.getSkillTag().getName();
                    studentSkillNames.add(skillName);
                    if (ss.getProficiencyLevel() != null) {
                        studentSkillProficiency.put(skillName, ss.getProficiencyLevel());
                    }
                }
            }
        }

        // Extract job skills
        Set<String> mandatorySkills = new HashSet<>();
        Set<String> optionalSkills  = new HashSet<>();
        if (job.getJobSkills() != null) {
            for (JobSkill js : job.getJobSkills()) {
                if (js.getSkillTag() != null && js.getSkillTag().getName() != null) {
                    String skillName = js.getSkillTag().getName();
                    if (js.isMandatory()) {
                        mandatorySkills.add(skillName);
                    } else {
                        optionalSkills.add(skillName);
                    }
                }
            }
        }

        double skillScore      = computeSkillScore(studentSkillNames, studentSkillProficiency,
                mandatorySkills, optionalSkills);
        double cgpaScore       = computeCgpaScore(student.getCgpa(), job.getMinCgpa());
        double experienceScore = computeExperienceScore(job.getExperienceLevel(), priorInternshipsCount);
        double preferenceScore = computePreferenceScore(
                student.getPreferredLocations(), student.getPreferredJobTypes(),
                job.getLocation(), job.getJobType());
        double recencyScore    = computeRecencyScore(job.getApplicationDeadline());

        double totalScore = skillScore      * WEIGHT_SKILL
                + cgpaScore       * WEIGHT_CGPA
                + experienceScore * WEIGHT_EXPERIENCE
                + preferenceScore * WEIGHT_PREFERENCE
                + recencyScore    * WEIGHT_RECENCY;

        // entityId is the student profile ID (caller-facing framing)
        return new MatchScore(student.getId(), totalScore, skillScore, cgpaScore,
                experienceScore, preferenceScore, recencyScore);
    }

    // ---------------------------------------------------------------------------
    // Batch scoring
    // ---------------------------------------------------------------------------

    /**
     * Scores all provided jobs for the given student, removes any where CGPA is a
     * hard disqualifier ({@code cgpaScore == 0.0}), and returns the list sorted
     * descending by {@code totalScore}.
     *
     * @param student                student whose fit is being assessed
     * @param jobs                   candidate jobs to evaluate
     * @param priorInternshipCounts  map of studentProfileId → prior internship count
     * @return filtered and sorted list of match scores
     */
    public List<MatchScore> scoreAll(
            StudentProfile student,
            List<Job> jobs,
            Map<String, Integer> priorInternshipCounts
    ) {
        int priorCount = priorInternshipCounts != null
                ? priorInternshipCounts.getOrDefault(student.getId(), 0)
                : 0;

        List<MatchScore> results = new ArrayList<>();
        for (Job job : jobs) {
            MatchScore score = scoreJobForStudent(student, job, priorCount);
            if (score.cgpaScore() > 0.0) {
                results.add(score);
            }
        }

        results.sort(Comparator.comparingDouble(MatchScore::totalScore).reversed());
        return results;
    }
}
