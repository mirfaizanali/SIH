package com.placement.portal.repository;

import com.placement.portal.domain.InternshipEnrollment;
import com.placement.portal.domain.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipEnrollmentRepository extends JpaRepository<InternshipEnrollment, String> {

    List<InternshipEnrollment> findByStudentId(String studentId);

    List<InternshipEnrollment> findByInternshipId(String internshipId);

    List<InternshipEnrollment> findByFacultyMentorId(String facultyMentorId);

    List<InternshipEnrollment> findByStatus(EnrollmentStatus status);
}
