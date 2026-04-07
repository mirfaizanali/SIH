package com.placement.portal.service.placement;

import com.placement.portal.domain.EmployerProfile;
import com.placement.portal.domain.Internship;
import com.placement.portal.domain.enums.InternshipStatus;
import com.placement.portal.dto.request.InternshipCreateRequest;
import com.placement.portal.dto.response.InternshipDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.EmployerProfileRepository;
import com.placement.portal.repository.InternshipRepository;
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
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final EntityMapper entityMapper;
    private final SecurityUtils securityUtils;

    /**
     * Creates a new internship posting for the currently authenticated employer.
     */
    public InternshipDto createInternship(InternshipCreateRequest req) {
        String userId = securityUtils.getCurrentUserId();
        EmployerProfile employer = employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "EmployerProfile for user " + userId + " not found"));

        Internship internship = Internship.builder()
                .employer(employer)
                .title(req.getTitle())
                .description(req.getDescription())
                .durationMonths(req.getDurationMonths())
                .stipend(req.getStipend())
                .location(req.getLocation())
                .isRemote(req.isRemote())
                .minCgpa(req.getMinCgpa())
                .applicationDeadline(req.getApplicationDeadline())
                .status(InternshipStatus.ACTIVE)
                .build();

        return entityMapper.toInternshipDto(internshipRepository.save(internship));
    }

    /**
     * Retrieves an internship by its UUID.
     */
    @Transactional(readOnly = true)
    public InternshipDto getInternshipById(String id) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Internship", id));
        return entityMapper.toInternshipDto(internship);
    }

    /**
     * Returns a paginated list of active internships.
     */
    @Transactional(readOnly = true)
    public Page<InternshipDto> getActiveInternships(Pageable pageable) {
        List<Internship> active = internshipRepository.findByStatus(InternshipStatus.ACTIVE);
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), active.size());
        List<Internship> slice = (start > active.size()) ? List.of() : active.subList(start, end);
        return new PageImpl<>(
                slice.stream().map(entityMapper::toInternshipDto).toList(),
                pageable, active.size());
    }

    /**
     * Returns the currently authenticated employer's own internship postings.
     */
    @Transactional(readOnly = true)
    public Page<InternshipDto> getMyInternships(Pageable pageable) {
        String userId = securityUtils.getCurrentUserId();
        EmployerProfile employer = employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "EmployerProfile for user " + userId + " not found"));

        List<Internship> internships = internshipRepository.findByEmployerId(employer.getId());
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), internships.size());
        List<Internship> slice = (start > internships.size()) ? List.of() : internships.subList(start, end);
        return new PageImpl<>(
                slice.stream().map(entityMapper::toInternshipDto).toList(),
                pageable, internships.size());
    }

    /**
     * Updates the status of an internship.
     */
    public void updateInternshipStatus(String id, InternshipStatus status) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Internship", id));
        internship.setStatus(status);
        internshipRepository.save(internship);
    }
}
