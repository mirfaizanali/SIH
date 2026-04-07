package com.placement.portal.service.placement;

import com.placement.portal.domain.FacultyProfile;
import com.placement.portal.domain.Internship;
import com.placement.portal.domain.InternshipEnrollment;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.domain.enums.EnrollmentStatus;
import com.placement.portal.dto.response.InternshipEnrollmentDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.FacultyProfileRepository;
import com.placement.portal.repository.InternshipEnrollmentRepository;
import com.placement.portal.repository.InternshipRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InternshipEnrollmentService {

    private final InternshipEnrollmentRepository enrollmentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final InternshipRepository internshipRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final EntityMapper entityMapper;
    private final SecurityUtils securityUtils;

    /**
     * Creates a new internship enrollment record for a student.
     *
     * @param internshipId       the internship UUID
     * @param studentProfileId   the student profile UUID
     * @param offerLetterPath    path to the stored offer letter (may be null)
     */
    public InternshipEnrollmentDto createEnrollment(
            String internshipId, String studentProfileId, String offerLetterPath) {

        StudentProfile student = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new EntityNotFoundException("StudentProfile", studentProfileId));

        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new EntityNotFoundException("Internship", internshipId));

        // Assign the student's current faculty mentor if present
        FacultyProfile mentor = student.getFacultyMentor();

        InternshipEnrollment enrollment = InternshipEnrollment.builder()
                .student(student)
                .internship(internship)
                .facultyMentor(mentor)
                .startDate(LocalDate.now())
                .status(EnrollmentStatus.ONGOING)
                .offerLetterPath(offerLetterPath)
                .build();

        return entityMapper.toInternshipEnrollmentDto(enrollmentRepository.save(enrollment));
    }

    /**
     * Retrieves an enrollment by its UUID.
     */
    @Transactional(readOnly = true)
    public InternshipEnrollmentDto getEnrollmentById(String id) {
        InternshipEnrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("InternshipEnrollment", id));
        return entityMapper.toInternshipEnrollmentDto(enrollment);
    }

    /**
     * Returns all enrollments for the currently authenticated student.
     */
    @Transactional(readOnly = true)
    public List<InternshipEnrollmentDto> getMyEnrollments() {
        String userId = securityUtils.getCurrentUserId();
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StudentProfile for user " + userId + " not found"));

        return enrollmentRepository.findByStudentId(student.getId())
                .stream()
                .map(entityMapper::toInternshipEnrollmentDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns enrollments for all students mentored by the currently authenticated faculty member.
     */
    @Transactional(readOnly = true)
    public List<InternshipEnrollmentDto> getMenteeEnrollments() {
        String userId = securityUtils.getCurrentUserId();
        FacultyProfile faculty = facultyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "FacultyProfile for user " + userId + " not found"));

        return enrollmentRepository.findByFacultyMentorId(faculty.getId())
                .stream()
                .map(entityMapper::toInternshipEnrollmentDto)
                .collect(Collectors.toList());
    }

    /**
     * Marks an enrollment as completed and records the end date.
     */
    public void completeEnrollment(String id, LocalDate endDate) {
        InternshipEnrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("InternshipEnrollment", id));
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setEndDate(endDate != null ? endDate : LocalDate.now());
        enrollmentRepository.save(enrollment);
        log.info("Enrollment {} marked as COMPLETED", id);
    }
}
