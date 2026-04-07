package com.placement.portal.service.user;

import com.placement.portal.domain.FacultyProfile;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.dto.response.FacultyProfileDto;
import com.placement.portal.dto.response.StudentProfileDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.FacultyProfileRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyProfileRepository facultyProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EntityMapper entityMapper;
    private final SecurityUtils securityUtils;

    /**
     * Returns the profile of the currently authenticated faculty member.
     */
    @Transactional(readOnly = true)
    public FacultyProfileDto getMyProfile() {
        String userId = securityUtils.getCurrentUserId();
        FacultyProfile profile = facultyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "FacultyProfile for user " + userId + " not found"));
        return entityMapper.toFacultyProfileDto(profile);
    }

    /**
     * Returns a faculty profile by its primary key.
     *
     * @param facultyProfileId the faculty profile UUID
     */
    @Transactional(readOnly = true)
    public FacultyProfileDto getFacultyById(String facultyProfileId) {
        FacultyProfile profile = facultyProfileRepository.findById(facultyProfileId)
                .orElseThrow(() -> new EntityNotFoundException("FacultyProfile", facultyProfileId));
        return entityMapper.toFacultyProfileDto(profile);
    }

    /**
     * Returns all student profiles whose assigned faculty mentor is the current user.
     */
    @Transactional(readOnly = true)
    public List<StudentProfileDto> getMyMentees() {
        String userId = securityUtils.getCurrentUserId();
        FacultyProfile profile = facultyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "FacultyProfile for user " + userId + " not found"));

        List<StudentProfile> mentees = studentProfileRepository
                .findByFacultyMentorId(profile.getId());

        return mentees.stream()
                .map(entityMapper::toStudentProfileDto)
                .collect(Collectors.toList());
    }
}
