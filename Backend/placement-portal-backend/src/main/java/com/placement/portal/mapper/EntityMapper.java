package com.placement.portal.mapper;

import com.placement.portal.domain.*;
import com.placement.portal.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central mapping utility that converts JPA entities to their corresponding
 * response DTOs.  All methods are instance methods so that this bean can be
 * injected and mocked in tests.
 */
@Component
public class EntityMapper {

    // -----------------------------------------------------------------------
    // User
    // -----------------------------------------------------------------------

    public UserDto toUserDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // -----------------------------------------------------------------------
    // Student profile
    // -----------------------------------------------------------------------

    public StudentProfileDto toStudentProfileDto(StudentProfile sp) {
        if (sp == null) return null;

        String fullName = sp.getUser() != null ? sp.getUser().getFullName() : null;
        String email    = sp.getUser() != null ? sp.getUser().getEmail()    : null;

        List<SkillDto> skillDtos = sp.getSkills() == null
                ? Collections.emptyList()
                : sp.getSkills().stream()
                        .map(this::toSkillDtoFromStudentSkill)
                        .collect(Collectors.toList());

        String facultyMentorId = sp.getFacultyMentor() != null
                ? sp.getFacultyMentor().getId() : null;

        return StudentProfileDto.builder()
                .id(sp.getId())
                .userId(sp.getUserId())
                .fullName(fullName)
                .email(email)
                .rollNumber(sp.getRollNumber())
                .department(sp.getDepartment())
                .batchYear(sp.getBatchYear())
                .cgpa(sp.getCgpa())
                .phone(sp.getPhone())
                .linkedinUrl(sp.getLinkedinUrl())
                .githubUrl(sp.getGithubUrl())
                .bio(sp.getBio())
                .isPlaced(sp.isPlaced())
                .placementPackage(sp.getPlacementPackage())
                .placedCompany(sp.getPlacedCompany())
                .facultyMentorId(facultyMentorId)
                .preferredLocations(sp.getPreferredLocations())
                .preferredJobTypes(sp.getPreferredJobTypes())
                .skills(skillDtos)
                .createdAt(sp.getCreatedAt())
                .build();
    }

    // -----------------------------------------------------------------------
    // Faculty profile
    // -----------------------------------------------------------------------

    public FacultyProfileDto toFacultyProfileDto(FacultyProfile fp) {
        if (fp == null) return null;

        String fullName = fp.getUser() != null ? fp.getUser().getFullName() : null;
        String email    = fp.getUser() != null ? fp.getUser().getEmail()    : null;
        String userId   = fp.getUser() != null ? fp.getUser().getId()       : null;

        return FacultyProfileDto.builder()
                .id(fp.getId())
                .userId(userId)
                .fullName(fullName)
                .email(email)
                .employeeId(fp.getEmployeeId())
                .department(fp.getDepartment())
                .designation(fp.getDesignation())
                .phone(fp.getPhone())
                .build();
    }

    // -----------------------------------------------------------------------
    // Employer profile
    // -----------------------------------------------------------------------

    public EmployerProfileDto toEmployerProfileDto(EmployerProfile ep) {
        if (ep == null) return null;

        String userId = ep.getUser() != null ? ep.getUser().getId() : null;

        return EmployerProfileDto.builder()
                .id(ep.getId())
                .userId(userId)
                .companyName(ep.getCompanyName())
                .companyWebsite(ep.getCompanyWebsite())
                .industry(ep.getIndustry())
                .companySize(ep.getCompanySize() != null ? ep.getCompanySize().name() : null)
                .hrContactName(ep.getHrContactName())
                .hrContactPhone(ep.getHrContactPhone())
                .location(ep.getLocation())
                .isVerified(ep.isVerified())
                .logoUrl(ep.getLogoUrl())
                .description(ep.getDescription())
                .build();
    }

    // -----------------------------------------------------------------------
    // Job
    // -----------------------------------------------------------------------

    public JobDto toJobDto(Job job) {
        if (job == null) return null;

        String employerId   = job.getEmployer() != null ? job.getEmployer().getId()          : null;
        String companyName  = job.getEmployer() != null ? job.getEmployer().getCompanyName() : null;

        List<SkillDto> skillDtos = job.getJobSkills() == null
                ? Collections.emptyList()
                : job.getJobSkills().stream()
                        .map(js -> toSkillDtoFromSkillTag(js.getSkillTag()))
                        .collect(Collectors.toList());

        return JobDto.builder()
                .id(job.getId())
                .employerId(employerId)
                .companyName(companyName)
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .jobType(job.getJobType() != null ? job.getJobType().name() : null)
                .experienceLevel(job.getExperienceLevel() != null ? job.getExperienceLevel().name() : null)
                .minCgpa(job.getMinCgpa())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .openingsCount(job.getOpeningsCount())
                .applicationDeadline(job.getApplicationDeadline())
                .status(job.getStatus() != null ? job.getStatus().name() : null)
                .skills(skillDtos)
                .createdAt(job.getCreatedAt())
                .build();
    }

    // -----------------------------------------------------------------------
    // Internship
    // -----------------------------------------------------------------------

    public InternshipDto toInternshipDto(Internship internship) {
        if (internship == null) return null;

        String employerId  = internship.getEmployer() != null ? internship.getEmployer().getId()          : null;
        String companyName = internship.getEmployer() != null ? internship.getEmployer().getCompanyName() : null;

        return InternshipDto.builder()
                .id(internship.getId())
                .employerId(employerId)
                .companyName(companyName)
                .title(internship.getTitle())
                .description(internship.getDescription())
                .durationMonths(internship.getDurationMonths())
                .stipend(internship.getStipend())
                .location(internship.getLocation())
                .isRemote(internship.isRemote())
                .minCgpa(internship.getMinCgpa())
                .applicationDeadline(internship.getApplicationDeadline())
                .status(internship.getStatus() != null ? internship.getStatus().name() : null)
                .createdAt(internship.getCreatedAt())
                .build();
    }

    // -----------------------------------------------------------------------
    // Placement drive
    // -----------------------------------------------------------------------

    public PlacementDriveDto toPlacementDriveDto(PlacementDrive drive) {
        if (drive == null) return null;

        String employerId   = drive.getEmployer()    != null ? drive.getEmployer().getId()          : null;
        String companyName  = drive.getEmployer()    != null ? drive.getEmployer().getCompanyName() : null;
        String organizedById = drive.getOrganizedBy() != null ? drive.getOrganizedBy().getId()       : null;

        List<JobDto> jobDtos = drive.getJobs() == null
                ? Collections.emptyList()
                : drive.getJobs().stream()
                        .map(this::toJobDto)
                        .collect(Collectors.toList());

        return PlacementDriveDto.builder()
                .id(drive.getId())
                .employerId(employerId)
                .companyName(companyName)
                .organizedById(organizedById)
                .title(drive.getTitle())
                .driveDate(drive.getDriveDate())
                .venue(drive.getVenue())
                .mode(drive.getMode() != null ? drive.getMode().name() : null)
                .status(drive.getStatus() != null ? drive.getStatus().name() : null)
                .description(drive.getDescription())
                .jobs(jobDtos)
                .build();
    }

    // -----------------------------------------------------------------------
    // Application
    // -----------------------------------------------------------------------

    public ApplicationDto toApplicationDto(Application app) {
        if (app == null) return null;

        String studentId   = app.getStudent() != null ? app.getStudent().getId() : null;
        String studentName = (app.getStudent() != null && app.getStudent().getUser() != null)
                ? app.getStudent().getUser().getFullName() : null;
        String jobId    = app.getJob()        != null ? app.getJob().getId()        : null;
        String jobTitle = app.getJob()        != null ? app.getJob().getTitle()     : null;
        String internshipId    = app.getInternship() != null ? app.getInternship().getId()    : null;
        String internshipTitle = app.getInternship() != null ? app.getInternship().getTitle() : null;
        String resumeId = app.getResume() != null ? app.getResume().getId() : null;

        return ApplicationDto.builder()
                .id(app.getId())
                .studentId(studentId)
                .studentName(studentName)
                .jobId(jobId)
                .jobTitle(jobTitle)
                .internshipId(internshipId)
                .internshipTitle(internshipTitle)
                .resumeId(resumeId)
                .coverLetter(app.getCoverLetter())
                .status(app.getStatus() != null ? app.getStatus().name() : null)
                .appliedAt(app.getAppliedAt())
                .build();
    }

    // -----------------------------------------------------------------------
    // Interview
    // -----------------------------------------------------------------------

    public InterviewDto toInterviewDto(Interview interview) {
        if (interview == null) return null;

        String applicationId = interview.getApplication() != null
                ? interview.getApplication().getId() : null;

        return InterviewDto.builder()
                .id(interview.getId())
                .applicationId(applicationId)
                .roundNumber(interview.getRoundNumber())
                .interviewType(interview.getInterviewType() != null
                        ? interview.getInterviewType().name() : null)
                .scheduledAt(interview.getScheduledAt())
                .durationMins(interview.getDurationMins())
                .meetingLink(interview.getMeetingLink())
                .location(interview.getLocation())
                .status(interview.getStatus() != null ? interview.getStatus().name() : null)
                .feedback(interview.getFeedback())
                .score(interview.getScore())
                .build();
    }

    // -----------------------------------------------------------------------
    // Internship enrollment
    // -----------------------------------------------------------------------

    public InternshipEnrollmentDto toInternshipEnrollmentDto(InternshipEnrollment enrollment) {
        if (enrollment == null) return null;

        String studentId   = enrollment.getStudent()    != null ? enrollment.getStudent().getId()           : null;
        String studentName = (enrollment.getStudent() != null && enrollment.getStudent().getUser() != null)
                ? enrollment.getStudent().getUser().getFullName() : null;
        String internshipId    = enrollment.getInternship() != null ? enrollment.getInternship().getId()    : null;
        String internshipTitle = enrollment.getInternship() != null ? enrollment.getInternship().getTitle() : null;
        String facultyMentorId = enrollment.getFacultyMentor() != null ? enrollment.getFacultyMentor().getId() : null;
        String mentorName = (enrollment.getFacultyMentor() != null
                && enrollment.getFacultyMentor().getUser() != null)
                ? enrollment.getFacultyMentor().getUser().getFullName() : null;

        return InternshipEnrollmentDto.builder()
                .id(enrollment.getId())
                .studentId(studentId)
                .studentName(studentName)
                .internshipId(internshipId)
                .internshipTitle(internshipTitle)
                .facultyMentorId(facultyMentorId)
                .mentorName(mentorName)
                .startDate(enrollment.getStartDate())
                .endDate(enrollment.getEndDate())
                .status(enrollment.getStatus() != null ? enrollment.getStatus().name() : null)
                .build();
    }

    // -----------------------------------------------------------------------
    // Report
    // -----------------------------------------------------------------------

    public ReportDto toReportDto(Report report) {
        if (report == null) return null;

        String enrollmentId = report.getEnrollment() != null ? report.getEnrollment().getId() : null;
        String reviewerId   = report.getReviewer()   != null ? report.getReviewer().getId()   : null;

        return ReportDto.builder()
                .id(report.getId())
                .enrollmentId(enrollmentId)
                .reportType(report.getReportType() != null ? report.getReportType().name() : null)
                .title(report.getTitle())
                .content(report.getContent())
                .filePath(report.getFilePath())
                .submittedAt(report.getSubmittedAt())
                .status(report.getStatus() != null ? report.getStatus().name() : null)
                .reviewerId(reviewerId)
                .reviewerComments(report.getReviewerComments())
                .reviewedAt(report.getReviewedAt())
                .build();
    }

    // -----------------------------------------------------------------------
    // Notification
    // -----------------------------------------------------------------------

    public NotificationDto toNotificationDto(Notification notification) {
        if (notification == null) return null;

        return NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType() != null ? notification.getType().name() : null)
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    // -----------------------------------------------------------------------
    // Resume
    // -----------------------------------------------------------------------

    public ResumeDto toResumeDto(Resume resume) {
        if (resume == null) return null;

        String studentProfileId = resume.getStudentProfile() != null
                ? resume.getStudentProfile().getId() : null;

        return ResumeDto.builder()
                .id(resume.getId())
                .studentProfileId(studentProfileId)
                .fileName(resume.getFileName())
                .fileSizeBytes(resume.getFileSizeBytes())
                .contentType(resume.getContentType())
                .isPrimary(resume.isPrimary())
                .uploadedAt(resume.getUploadedAt())
                .build();
    }

    // -----------------------------------------------------------------------
    // Skill helpers
    // -----------------------------------------------------------------------

    public SkillDto toSkillDtoFromSkillTag(SkillTag skillTag) {
        if (skillTag == null) return null;
        return SkillDto.builder()
                .id(skillTag.getId())
                .name(skillTag.getName())
                .category(skillTag.getCategory())
                .proficiencyLevel(null)
                .build();
    }

    public SkillDto toSkillDtoFromStudentSkill(StudentSkill studentSkill) {
        if (studentSkill == null || studentSkill.getSkillTag() == null) return null;
        SkillTag tag = studentSkill.getSkillTag();
        return SkillDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .category(tag.getCategory())
                .proficiencyLevel(studentSkill.getProficiencyLevel() != null
                        ? studentSkill.getProficiencyLevel().name() : null)
                .build();
    }

    // -----------------------------------------------------------------------
    // Pagination
    // -----------------------------------------------------------------------

    public <E, D> PagedResponse<D> toPagedResponse(Page<E> page, List<D> content) {
        return PagedResponse.<D>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
