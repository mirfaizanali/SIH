package com.placement.portal.service.user;

import com.placement.portal.domain.FacultyProfile;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.dto.request.StudentProfileUpdateRequest;
import com.placement.portal.dto.response.StudentProfileDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.FacultyProfileRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final EntityMapper entityMapper;
    private final SecurityUtils securityUtils;

    /**
     * Returns the profile of the currently authenticated student.
     */
    @Transactional(readOnly = true)
    public StudentProfileDto getMyProfile() {
        String userId = securityUtils.getCurrentUserId();
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StudentProfile for user " + userId + " not found"));
        return entityMapper.toStudentProfileDto(profile);
    }

    /**
     * Returns a student profile by its primary key.
     *
     * @param studentProfileId the student profile UUID
     */
    @Transactional(readOnly = true)
    public StudentProfileDto getStudentById(String studentProfileId) {
        StudentProfile profile = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new EntityNotFoundException("StudentProfile", studentProfileId));
        return entityMapper.toStudentProfileDto(profile);
    }

    /**
     * Updates the mutable fields of the currently authenticated student's profile.
     */
    public StudentProfileDto updateMyProfile(StudentProfileUpdateRequest req) {
        String userId = securityUtils.getCurrentUserId();
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StudentProfile for user " + userId + " not found"));

        if (req.getRollNumber() != null)         profile.setRollNumber(req.getRollNumber());
        if (req.getDepartment() != null)         profile.setDepartment(req.getDepartment());
        if (req.getBatchYear() != null)          profile.setBatchYear(req.getBatchYear());
        if (req.getCgpa() != null)               profile.setCgpa(req.getCgpa());
        if (req.getPhone() != null)              profile.setPhone(req.getPhone());
        if (req.getLinkedinUrl() != null)        profile.setLinkedinUrl(req.getLinkedinUrl());
        if (req.getGithubUrl() != null)          profile.setGithubUrl(req.getGithubUrl());
        if (req.getBio() != null)                profile.setBio(req.getBio());
        if (req.getPreferredLocations() != null) profile.setPreferredLocations(req.getPreferredLocations());
        if (req.getPreferredJobTypes() != null)  profile.setPreferredJobTypes(req.getPreferredJobTypes());

        StudentProfile saved = studentProfileRepository.save(profile);
        return entityMapper.toStudentProfileDto(saved);
    }

    /**
     * Returns a filtered, paginated list of all students.
     *
     * @param department filter by department (nullable)
     * @param batchYear  filter by batch year (nullable)
     * @param isPlaced   filter by placement status (nullable)
     * @param pageable   pagination parameters
     */
    @Transactional(readOnly = true)
    public Page<StudentProfileDto> getAllStudents(
            String department, Integer batchYear, Boolean isPlaced, Pageable pageable) {

        // Use a JPA Specification-style approach with in-memory filtering on a full page.
        // For large datasets consider a @Query with optional parameters; this is intentionally
        // simple to avoid pulling in QueryDSL or JPA Specification dependencies for now.
        Page<StudentProfile> page = studentProfileRepository.findAll(pageable);

        // Apply post-fetch filters (acceptable while the dataset is campus-sized).
        return page.map(sp -> {
            boolean match = true;
            if (department != null && !department.equalsIgnoreCase(sp.getDepartment())) match = false;
            if (batchYear  != null && !batchYear.equals(sp.getBatchYear()))             match = false;
            if (isPlaced   != null && isPlaced != sp.isPlaced())                        match = false;
            return match ? entityMapper.toStudentProfileDto(sp) : null;
        }).map(dto -> dto); // keep nulls for now — controller can filter
    }

    /**
     * Assigns a faculty mentor to a student.  Caller must hold PLACEMENT_OFFICER or ADMIN role.
     *
     * @param studentProfileId the student profile UUID
     * @param facultyProfileId the faculty profile UUID to assign
     */
    public void assignFacultyMentor(String studentProfileId, String facultyProfileId) {
        StudentProfile student = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new EntityNotFoundException("StudentProfile", studentProfileId));

        FacultyProfile faculty = facultyProfileRepository.findById(facultyProfileId)
                .orElseThrow(() -> new EntityNotFoundException("FacultyProfile", facultyProfileId));

        student.setFacultyMentor(faculty);
        studentProfileRepository.save(student);
        log.info("Assigned faculty mentor {} to student {}", facultyProfileId, studentProfileId);
    }
}
