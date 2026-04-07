package com.placement.portal.service.recommendation;

import com.placement.portal.domain.Job;
import com.placement.portal.domain.JobSkill;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.domain.enums.EnrollmentStatus;
import com.placement.portal.domain.enums.JobStatus;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.repository.InternshipEnrollmentRepository;
import com.placement.portal.repository.JobRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Assembles the raw inputs that the {@link ScoringEngine} needs to compute match scores.
 *
 * <p>This component is responsible for all database I/O in the recommendation pipeline.
 * It isolates data-fetching from the pure scoring logic.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchCandidateBuilder {

    private final JobRepository                   jobRepository;
    private final StudentProfileRepository        studentProfileRepository;
    private final InternshipEnrollmentRepository  internshipEnrollmentRepository;
    private final SecurityUtils                   securityUtils;

    // ---------------------------------------------------------------------------
    // Student loading
    // ---------------------------------------------------------------------------

    /**
     * Loads the {@link StudentProfile} for the currently authenticated user.
     *
     * <p>The student's skill collection is eagerly initialized within the same
     * transaction so that the downstream {@link ScoringEngine} can access it
     * without triggering lazy-loading outside of a session.</p>
     *
     * @return the loaded StudentProfile (never null)
     * @throws EntityNotFoundException if no profile exists for the current user
     */
    @Transactional(readOnly = true)
    public StudentProfile loadCurrentStudent() {
        String userId = securityUtils.getCurrentUserId();
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StudentProfile not found for user: " + userId));

        // Force-initialize the lazy skills collection while still inside the session
        Hibernate.initialize(student.getSkills());
        if (student.getSkills() != null) {
            student.getSkills().forEach(ss -> {
                Hibernate.initialize(ss.getSkillTag());
            });
        }

        return student;
    }

    // ---------------------------------------------------------------------------
    // Job loading
    // ---------------------------------------------------------------------------

    /**
     * Returns all currently ACTIVE jobs.
     *
     * <p>Results are cached under the {@code active_jobs} cache to avoid
     * repeated database hits for every recommendation request.</p>
     *
     * @return list of active jobs; never null
     */
    @Cacheable("active_jobs")
    @Transactional(readOnly = true)
    public List<Job> loadActiveCandidateJobs() {
        List<Job> jobs = jobRepository.findByStatus(JobStatus.ACTIVE);
        // Eagerly initialize job-skill associations while in the session
        jobs.forEach(job -> {
            Hibernate.initialize(job.getJobSkills());
            if (job.getJobSkills() != null) {
                job.getJobSkills().forEach(js -> Hibernate.initialize(js.getSkillTag()));
            }
        });
        return jobs;
    }

    /**
     * Loads active jobs and returns them filtered for the given student.
     *
     * <p>Uses the cached {@link #loadActiveCandidateJobs()} internally; the
     * CGPA hard-filter is intentionally delegated to {@link ScoringEngine#scoreAll}
     * so that the scoring layer remains the single source of disqualification truth.</p>
     *
     * @param student the student profile (used for potential future pre-filters)
     * @return list of active jobs
     */
    @Transactional(readOnly = true)
    public List<Job> loadActiveCandidateJobs(StudentProfile student) {
        return loadActiveCandidateJobs();
    }

    // ---------------------------------------------------------------------------
    // Prior internship count
    // ---------------------------------------------------------------------------

    /**
     * Counts the number of internships the student has completed.
     *
     * @param studentProfileId the student profile ID
     * @return count of completed enrollments
     */
    @Transactional(readOnly = true)
    public int countPriorInternships(String studentProfileId) {
        List<?> completed = internshipEnrollmentRepository
                .findByStudentId(studentProfileId)
                .stream()
                .filter(e -> EnrollmentStatus.COMPLETED.equals(e.getStatus()))
                .toList();
        return completed.size();
    }

    // ---------------------------------------------------------------------------
    // Skill index building
    // ---------------------------------------------------------------------------

    /**
     * Builds a map of jobId → set of skill names (both mandatory and optional) for
     * a given list of jobs.
     *
     * <p>This map is used by callers that need a quick skill-name lookup without
     * re-iterating job-skill sets.</p>
     *
     * @param jobs the jobs to index
     * @return map of jobId → Set&lt;skillName&gt;
     */
    public Map<String, Set<String>> extractJobSkills(List<Job> jobs) {
        Map<String, Set<String>> index = new HashMap<>();
        for (Job job : jobs) {
            Set<String> skillNames = new HashSet<>();
            if (job.getJobSkills() != null) {
                for (JobSkill js : job.getJobSkills()) {
                    if (js.getSkillTag() != null && js.getSkillTag().getName() != null) {
                        skillNames.add(js.getSkillTag().getName());
                    }
                }
            }
            index.put(job.getId(), skillNames);
        }
        return index;
    }
}
