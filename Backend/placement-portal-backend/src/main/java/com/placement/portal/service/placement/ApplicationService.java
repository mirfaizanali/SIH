package com.placement.portal.service.placement;

import com.placement.portal.domain.Application;
import com.placement.portal.domain.Internship;
import com.placement.portal.domain.Job;
import com.placement.portal.domain.Resume;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.domain.enums.ApplicationStatus;
import com.placement.portal.dto.request.ApplicationRequest;
import com.placement.portal.dto.request.ApplicationStatusUpdateRequest;
import com.placement.portal.dto.response.ApplicationDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.ApplicationRepository;
import com.placement.portal.repository.InternshipRepository;
import com.placement.portal.repository.JobRepository;
import com.placement.portal.repository.ResumeRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JobRepository jobRepository;
    private final InternshipRepository internshipRepository;
    private final ResumeRepository resumeRepository;
    private final EntityMapper entityMapper;
    private final SecurityUtils securityUtils;

    /**
     * Submits an application on behalf of the currently authenticated student.
     * Validates that the student hasn't already applied and meets the minimum CGPA requirement.
     */
    public ApplicationDto applyToJob(ApplicationRequest req) {
        String userId = securityUtils.getCurrentUserId();
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StudentProfile for user " + userId + " not found"));

        Job job = null;
        Internship internship = null;

        if (req.getJobId() != null) {
            job = jobRepository.findById(req.getJobId())
                    .orElseThrow(() -> new EntityNotFoundException("Job", req.getJobId()));

            if (applicationRepository.existsByStudentIdAndJobId(student.getId(), job.getId())) {
                throw new IllegalStateException("You have already applied to this job");
            }
            if (job.getMinCgpa() != null && student.getCgpa() != null
                    && student.getCgpa().compareTo(job.getMinCgpa()) < 0) {
                throw new IllegalStateException(
                        "Your CGPA does not meet the minimum requirement of " + job.getMinCgpa());
            }
        } else if (req.getInternshipId() != null) {
            internship = internshipRepository.findById(req.getInternshipId())
                    .orElseThrow(() -> new EntityNotFoundException("Internship", req.getInternshipId()));

            if (applicationRepository.existsByStudentIdAndInternshipId(student.getId(), internship.getId())) {
                throw new IllegalStateException("You have already applied to this internship");
            }
            if (internship.getMinCgpa() != null && student.getCgpa() != null
                    && student.getCgpa().compareTo(internship.getMinCgpa()) < 0) {
                throw new IllegalStateException(
                        "Your CGPA does not meet the minimum requirement of " + internship.getMinCgpa());
            }
        } else {
            throw new IllegalArgumentException("Either jobId or internshipId must be provided");
        }

        Resume resume = null;
        if (req.getResumeId() != null) {
            resume = resumeRepository.findById(req.getResumeId())
                    .orElseThrow(() -> new EntityNotFoundException("Resume", req.getResumeId()));
        }

        Application application = Application.builder()
                .student(student)
                .job(job)
                .internship(internship)
                .resume(resume)
                .coverLetter(req.getCoverLetter())
                .status(ApplicationStatus.SUBMITTED)
                .build();

        return entityMapper.toApplicationDto(applicationRepository.save(application));
    }

    /**
     * Retrieves an application by its UUID.
     */
    @Transactional(readOnly = true)
    public ApplicationDto getApplicationById(String id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application", id));
        return entityMapper.toApplicationDto(application);
    }

    /**
     * Returns the currently authenticated student's own applications.
     */
    @Transactional(readOnly = true)
    public Page<ApplicationDto> getMyApplications(Pageable pageable) {
        String userId = securityUtils.getCurrentUserId();
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StudentProfile for user " + userId + " not found"));

        List<Application> apps = applicationRepository.findByStudentId(student.getId());
        return toPage(apps, pageable);
    }

    /**
     * Returns all applications for a given job (employer/officer use).
     */
    @Transactional(readOnly = true)
    public Page<ApplicationDto> getApplicationsForJob(String jobId, Pageable pageable) {
        List<Application> apps = applicationRepository.findByJobId(jobId);
        return toPage(apps, pageable);
    }

    /**
     * Updates the status of an application (employer/officer use).
     */
    public ApplicationDto updateApplicationStatus(String id, ApplicationStatusUpdateRequest req) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application", id));
        application.setStatus(req.getStatus());
        return entityMapper.toApplicationDto(applicationRepository.save(application));
    }

    /**
     * Allows a student to withdraw their own application, but only while it is in SUBMITTED status.
     */
    public void withdrawApplication(String id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application", id));

        String userId = securityUtils.getCurrentUserId();
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StudentProfile for user " + userId + " not found"));

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("You can only withdraw your own applications");
        }
        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Only SUBMITTED applications can be withdrawn; current status: "
                            + application.getStatus());
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
        log.info("Application {} withdrawn by student {}", id, student.getId());
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Page<ApplicationDto> toPage(List<Application> apps, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), apps.size());
        List<Application> slice = (start > apps.size()) ? List.of() : apps.subList(start, end);
        return new PageImpl<>(
                slice.stream().map(entityMapper::toApplicationDto).toList(),
                pageable, apps.size());
    }
}
