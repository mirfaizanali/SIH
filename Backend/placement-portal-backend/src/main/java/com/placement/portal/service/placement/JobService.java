package com.placement.portal.service.placement;

import com.placement.portal.domain.EmployerProfile;
import com.placement.portal.domain.Job;
import com.placement.portal.domain.JobSkill;
import com.placement.portal.domain.JobSkillId;
import com.placement.portal.domain.SkillTag;
import com.placement.portal.domain.enums.JobStatus;
import com.placement.portal.dto.request.JobCreateRequest;
import com.placement.portal.dto.response.JobDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.EmployerProfileRepository;
import com.placement.portal.repository.JobRepository;
import com.placement.portal.repository.SkillTagRepository;
import com.placement.portal.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final SkillTagRepository skillTagRepository;
    private final EntityMapper entityMapper;
    private final SecurityUtils securityUtils;

    /**
     * Creates a new job posting for the currently authenticated employer.
     * Status defaults to DRAFT.
     */
    public JobDto createJob(JobCreateRequest req) {
        String userId = securityUtils.getCurrentUserId();
        EmployerProfile employer = employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "EmployerProfile for user " + userId + " not found"));

        Job job = Job.builder()
                .employer(employer)
                .title(req.getTitle())
                .description(req.getDescription())
                .location(req.getLocation())
                .jobType(req.getJobType())
                .experienceLevel(req.getExperienceLevel())
                .minCgpa(req.getMinCgpa())
                .salaryMin(req.getSalaryMin())
                .salaryMax(req.getSalaryMax())
                .openingsCount(req.getOpeningsCount())
                .applicationDeadline(req.getApplicationDeadline())
                .status(JobStatus.ACTIVE)
                .build();

        Job saved = jobRepository.save(job);
        addSkillsToJob(saved, req.getSkillIds(), req.getMandatorySkillIds());
        return entityMapper.toJobDto(jobRepository.findById(saved.getId()).orElse(saved));
    }

    /**
     * Retrieves a job by its UUID.
     */
    @Transactional(readOnly = true)
    public JobDto getJobById(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job", id));
        return entityMapper.toJobDto(job);
    }

    /**
     * Returns a paginated list of active (OPEN) jobs with a future deadline.
     * Results are cached under the key {@code active_jobs}.
     */
    @Transactional(readOnly = true)
    @Cacheable("active_jobs")
    public Page<JobDto> getActiveJobs(String location, String experienceLevel, Pageable pageable) {
        Page<Job> page = jobRepository.findActiveJobs(
                JobStatus.ACTIVE, LocalDate.now(), pageable);
        return page.map(job -> {
            if (location != null && !location.equalsIgnoreCase(job.getLocation())) return null;
            if (experienceLevel != null && job.getExperienceLevel() != null
                    && !experienceLevel.equalsIgnoreCase(job.getExperienceLevel().name())) return null;
            return entityMapper.toJobDto(job);
        });
    }

    /**
     * Returns the currently authenticated employer's own jobs.
     */
    @Transactional(readOnly = true)
    public Page<JobDto> getMyJobs(Pageable pageable) {
        String userId = securityUtils.getCurrentUserId();
        EmployerProfile employer = employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "EmployerProfile for user " + userId + " not found"));

        List<Job> jobs = jobRepository.findByEmployerId(employer.getId());
        // Manual paging — acceptable for employer-level volume
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), jobs.size());
        List<Job> slice = (start > jobs.size()) ? List.of() : jobs.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(
                slice.stream().map(entityMapper::toJobDto).toList(),
                pageable, jobs.size());
    }

    /**
     * Updates an existing job.  Only the employer who owns the job may update it.
     */
    @CacheEvict(value = "active_jobs", allEntries = true)
    public JobDto updateJob(String id, JobCreateRequest req) {
        String userId = securityUtils.getCurrentUserId();
        EmployerProfile employer = employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "EmployerProfile for user " + userId + " not found"));

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job", id));

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new SecurityException("You do not own this job posting");
        }

        job.setTitle(req.getTitle());
        if (req.getDescription()        != null) job.setDescription(req.getDescription());
        if (req.getLocation()           != null) job.setLocation(req.getLocation());
        if (req.getJobType()            != null) job.setJobType(req.getJobType());
        if (req.getExperienceLevel()    != null) job.setExperienceLevel(req.getExperienceLevel());
        if (req.getMinCgpa()            != null) job.setMinCgpa(req.getMinCgpa());
        if (req.getSalaryMin()          != null) job.setSalaryMin(req.getSalaryMin());
        if (req.getSalaryMax()          != null) job.setSalaryMax(req.getSalaryMax());
        job.setOpeningsCount(req.getOpeningsCount());
        if (req.getApplicationDeadline() != null) job.setApplicationDeadline(req.getApplicationDeadline());

        // Replace skills
        job.getJobSkills().clear();
        Job saved = jobRepository.save(job);
        addSkillsToJob(saved, req.getSkillIds(), req.getMandatorySkillIds());
        return entityMapper.toJobDto(jobRepository.findById(saved.getId()).orElse(saved));
    }

    /**
     * Updates the status of a job.
     */
    @CacheEvict(value = "active_jobs", allEntries = true)
    public void updateJobStatus(String id, JobStatus status) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job", id));
        job.setStatus(status);
        jobRepository.save(job);
    }

    /**
     * Soft-deletes a job by setting its status to CLOSED.
     */
    @CacheEvict(value = "active_jobs", allEntries = true)
    public void deleteJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job", id));
        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
        log.info("Job {} soft-deleted (status=CLOSED)", id);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void addSkillsToJob(Job job, List<Integer> skillIds, List<Integer> mandatorySkillIds) {
        if (skillIds != null) {
            for (Integer skillId : skillIds) {
                skillTagRepository.findById(skillId).ifPresent(tag -> {
                    boolean mandatory = mandatorySkillIds != null && mandatorySkillIds.contains(skillId);
                    JobSkill js = JobSkill.builder()
                            .id(new JobSkillId(job.getId(), skillId))
                            .job(job)
                            .skillTag(tag)
                            .isMandatory(mandatory)
                            .build();
                    job.getJobSkills().add(js);
                });
            }
        }
    }
}
