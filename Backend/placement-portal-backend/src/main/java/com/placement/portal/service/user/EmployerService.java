package com.placement.portal.service.user;

import com.placement.portal.domain.EmployerProfile;
import com.placement.portal.domain.enums.CompanySize;
import com.placement.portal.dto.request.EmployerProfileUpdateRequest;
import com.placement.portal.dto.response.EmployerProfileDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.EmployerProfileRepository;
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
public class EmployerService {

    private final EmployerProfileRepository employerProfileRepository;
    private final EntityMapper entityMapper;
    private final SecurityUtils securityUtils;

    /**
     * Returns the profile of the currently authenticated employer.
     */
    @Transactional(readOnly = true)
    public EmployerProfileDto getMyProfile() {
        String userId = securityUtils.getCurrentUserId();
        EmployerProfile profile = employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "EmployerProfile for user " + userId + " not found"));
        return entityMapper.toEmployerProfileDto(profile);
    }

    /**
     * Updates the mutable fields of the currently authenticated employer's profile.
     */
    public EmployerProfileDto updateMyProfile(EmployerProfileUpdateRequest req) {
        String userId = securityUtils.getCurrentUserId();
        EmployerProfile profile = employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "EmployerProfile for user " + userId + " not found"));

        if (req.getCompanyName()    != null && !req.getCompanyName().isBlank()) profile.setCompanyName(req.getCompanyName());
        if (req.getCompanyWebsite() != null) profile.setCompanyWebsite(req.getCompanyWebsite());
        if (req.getIndustry()       != null) profile.setIndustry(req.getIndustry());
        if (req.getCompanySize()    != null) {
            try {
                profile.setCompanySize(CompanySize.valueOf(req.getCompanySize().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid company size value: " + req.getCompanySize());
            }
        }
        if (req.getHrContactName()  != null) profile.setHrContactName(req.getHrContactName());
        if (req.getHrContactPhone() != null) profile.setHrContactPhone(req.getHrContactPhone());
        if (req.getLocation()       != null) profile.setLocation(req.getLocation());
        if (req.getLogoUrl()        != null) profile.setLogoUrl(req.getLogoUrl());
        if (req.getDescription()    != null) profile.setDescription(req.getDescription());

        EmployerProfile saved = employerProfileRepository.save(profile);
        return entityMapper.toEmployerProfileDto(saved);
    }

    /**
     * Sets {@code isVerified = true} for the given employer.
     * Caller must hold PLACEMENT_OFFICER or ADMIN role.
     */
    public void verifyEmployer(String employerProfileId) {
        EmployerProfile profile = employerProfileRepository.findById(employerProfileId)
                .orElseThrow(() -> new EntityNotFoundException("EmployerProfile", employerProfileId));
        profile.setVerified(true);
        employerProfileRepository.save(profile);
        log.info("Employer {} verified", employerProfileId);
    }

    /**
     * Returns a paginated list of employer profiles, optionally filtered by verification status.
     */
    @Transactional(readOnly = true)
    public Page<EmployerProfileDto> getAllEmployers(Boolean isVerified, Pageable pageable) {
        Page<EmployerProfile> page = employerProfileRepository.findAll(pageable);
        return page.map(ep -> {
            if (isVerified != null && ep.isVerified() != isVerified) return null;
            return entityMapper.toEmployerProfileDto(ep);
        });
    }
}
