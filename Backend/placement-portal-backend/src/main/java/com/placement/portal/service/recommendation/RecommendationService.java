package com.placement.portal.service.recommendation;

import com.placement.portal.domain.Job;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.dto.response.JobDto;
import com.placement.portal.dto.response.StudentProfileDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.JobRepository;
import com.placement.portal.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrates the recommendation pipeline.
 *
 * <p>Two perspectives are supported:</p>
 * <ul>
 *   <li><b>Student perspective</b>: {@link #getTopJobsForCurrentStudent} returns the
 *       best-matching active jobs for the logged-in student.</li>
 *   <li><b>Employer/Officer perspective</b>: {@link #getTopStudentsForJob} returns the
 *       best-matching student profiles for a given job posting.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final MatchCandidateBuilder      matchCandidateBuilder;
    private final ScoringEngine              scoringEngine;
    private final EntityMapper               entityMapper;
    private final JobRepository              jobRepository;
    private final StudentProfileRepository   studentProfileRepository;

    // ---------------------------------------------------------------------------
    // Student-facing: recommended jobs
    // ---------------------------------------------------------------------------

    /**
     * Returns up to {@code limit} job recommendations for the currently authenticated student,
     * ranked by match score descending.
     *
     * <p>Each returned {@link JobDto} has the {@code matchScore} field populated with the
     * weighted composite score (0–100) so the client can display a confidence indicator.</p>
     *
     * @param limit maximum number of results to return
     * @return list of recommended jobs with match scores attached
     */
    @Transactional(readOnly = true)
    public List<JobDto> getTopJobsForCurrentStudent(int limit) {
        // 1. Load the current student (with skills initialised)
        StudentProfile student = matchCandidateBuilder.loadCurrentStudent();

        // 2. Load all active jobs (cached)
        List<Job> jobs = matchCandidateBuilder.loadActiveCandidateJobs(student);

        // 3. Count prior internships for the student
        int priorCount = matchCandidateBuilder.countPriorInternships(student.getId());

        // 4. Score all jobs, filter hard disqualifiers, sort descending
        List<MatchScore> scores = scoringEngine.scoreAll(
                student, jobs, Map.of(student.getId(), priorCount));

        // 5. Build a quick job-by-id lookup from the already-loaded list
        Map<String, Job> jobById = jobs.stream()
                .collect(Collectors.toMap(Job::getId, j -> j));

        // 6. Take top N, map to DTO, attach match score
        List<JobDto> result = new ArrayList<>();
        int taken = 0;
        for (MatchScore ms : scores) {
            if (taken >= limit) break;
            Job job = jobById.get(ms.entityId());
            if (job == null) continue;
            JobDto dto = entityMapper.toJobDto(job);
            dto.setMatchScore(Math.round(ms.totalScore() * 100.0) / 100.0);
            result.add(dto);
            taken++;
        }

        log.debug("Recommendation: student={} eligible={} returned={}",
                student.getId(), scores.size(), result.size());
        return result;
    }

    // ---------------------------------------------------------------------------
    // Employer/Officer-facing: top student candidates for a job
    // ---------------------------------------------------------------------------

    /**
     * Returns up to {@code limit} student profile recommendations for the given job,
     * ranked by match score descending.
     *
     * <p>Students whose CGPA is below the job's minimum are filtered out before scoring
     * (hard disqualifier).</p>
     *
     * @param jobId job to find candidates for
     * @param limit maximum number of results to return
     * @return list of recommended student profiles
     * @throws EntityNotFoundException if the job does not exist
     */
    @Transactional(readOnly = true)
    public List<StudentProfileDto> getTopStudentsForJob(String jobId, int limit) {
        // 1. Load the job (with its skills)
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found: " + jobId));

        // Initialise job skills if lazy
        Hibernate.initialize(job.getJobSkills());
        if (job.getJobSkills() != null) {
            job.getJobSkills().forEach(js -> Hibernate.initialize(js.getSkillTag()));
        }

        // 2. Load eligible students: all, or pre-filtered by CGPA if minCgpa is set
        List<StudentProfile> candidates;
        if (job.getMinCgpa() != null) {
            candidates = studentProfileRepository
                    .findByIsPlacedFalseAndCgpaGreaterThanEqual(job.getMinCgpa());
        } else {
            candidates = studentProfileRepository.findAll();
        }

        // Initialise skills for all candidates
        candidates.forEach(sp -> {
            Hibernate.initialize(sp.getSkills());
            if (sp.getSkills() != null) {
                sp.getSkills().forEach(ss -> Hibernate.initialize(ss.getSkillTag()));
            }
        });

        // 3. Score each student against the job
        List<MatchScore> scores = new ArrayList<>();
        for (StudentProfile student : candidates) {
            int priorCount = matchCandidateBuilder.countPriorInternships(student.getId());
            MatchScore ms = scoringEngine.scoreStudentForJob(student, job, priorCount);
            if (ms.cgpaScore() > 0.0) {
                scores.add(ms);
            }
        }

        // 4. Sort descending by totalScore
        scores.sort((a, b) -> Double.compare(b.totalScore(), a.totalScore()));

        // 5. Build student-by-id lookup
        Map<String, StudentProfile> studentById = candidates.stream()
                .collect(Collectors.toMap(StudentProfile::getId, s -> s));

        // 6. Take top N, map to DTO
        List<StudentProfileDto> result = new ArrayList<>();
        int taken = 0;
        for (MatchScore ms : scores) {
            if (taken >= limit) break;
            StudentProfile sp = studentById.get(ms.entityId());
            if (sp == null) continue;
            result.add(entityMapper.toStudentProfileDto(sp));
            taken++;
        }

        log.debug("Recommendation: job={} candidates={} returned={}",
                jobId, scores.size(), result.size());
        return result;
    }
}
