package com.placement.portal.service.placement;

import com.placement.portal.domain.FacultyProfile;
import com.placement.portal.domain.InternshipEnrollment;
import com.placement.portal.domain.Report;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.domain.enums.ReportStatus;
import com.placement.portal.dto.request.ReportCreateRequest;
import com.placement.portal.dto.request.ReportReviewRequest;
import com.placement.portal.dto.response.ReportDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.FacultyProfileRepository;
import com.placement.portal.repository.InternshipEnrollmentRepository;
import com.placement.portal.repository.ReportRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final InternshipEnrollmentRepository enrollmentRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EntityMapper entityMapper;
    private final SecurityUtils securityUtils;

    /**
     * Creates a new report in DRAFT status on behalf of the currently authenticated student.
     */
    public ReportDto createReport(ReportCreateRequest req) {
        InternshipEnrollment enrollment = enrollmentRepository.findById(req.getEnrollmentId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "InternshipEnrollment", req.getEnrollmentId()));

        Report report = Report.builder()
                .enrollment(enrollment)
                .reportType(req.getReportType())
                .title(req.getTitle())
                .content(req.getContent())
                .status(ReportStatus.DRAFT)
                .build();

        return entityMapper.toReportDto(reportRepository.save(report));
    }

    /**
     * Submits a draft report.  Sets status to SUBMITTED and records the submission timestamp.
     */
    public ReportDto submitReport(String id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report", id));

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT reports can be submitted; current status: " + report.getStatus());
        }

        report.setStatus(ReportStatus.SUBMITTED);
        report.setSubmittedAt(LocalDateTime.now());
        return entityMapper.toReportDto(reportRepository.save(report));
    }

    /**
     * Records a faculty mentor's review of a submitted report.
     */
    public ReportDto reviewReport(String id, ReportReviewRequest req) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report", id));

        String userId = securityUtils.getCurrentUserId();
        FacultyProfile faculty = facultyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "FacultyProfile for user " + userId + " not found"));

        report.setStatus(req.getStatus());
        report.setReviewer(faculty);
        report.setReviewerComments(req.getReviewerComments());
        report.setReviewedAt(LocalDateTime.now());

        return entityMapper.toReportDto(reportRepository.save(report));
    }

    /**
     * Returns all reports for the given enrollment.
     */
    @Transactional(readOnly = true)
    public List<ReportDto> getReportsForEnrollment(String enrollmentId) {
        return reportRepository.findByEnrollmentId(enrollmentId)
                .stream()
                .map(entityMapper::toReportDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns a paginated list of SUBMITTED reports assigned to the currently authenticated
     * faculty member (i.e. reports belonging to their mentees).
     */
    @Transactional(readOnly = true)
    public Page<ReportDto> getPendingReports(Pageable pageable) {
        String userId = securityUtils.getCurrentUserId();
        FacultyProfile faculty = facultyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "FacultyProfile for user " + userId + " not found"));

        // All submitted reports in the system, then filter to those belonging to this faculty's mentees
        List<Report> submitted = reportRepository.findByStatus(ReportStatus.SUBMITTED);
        List<ReportDto> relevant = submitted.stream()
                .filter(r -> {
                    InternshipEnrollment enrollment = r.getEnrollment();
                    return enrollment != null
                            && enrollment.getFacultyMentor() != null
                            && enrollment.getFacultyMentor().getId().equals(faculty.getId());
                })
                .map(entityMapper::toReportDto)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), relevant.size());
        List<ReportDto> slice = (start > relevant.size()) ? List.of() : relevant.subList(start, end);
        return new PageImpl<>(slice, pageable, relevant.size());
    }
}
