package com.placement.portal.service.placement;

import com.placement.portal.domain.Application;
import com.placement.portal.domain.Interview;
import com.placement.portal.domain.enums.InterviewStatus;
import com.placement.portal.dto.request.InterviewCreateRequest;
import com.placement.portal.dto.request.InterviewFeedbackRequest;
import com.placement.portal.dto.response.InterviewDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.ApplicationRepository;
import com.placement.portal.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final EntityMapper entityMapper;

    /**
     * Schedules a new interview round for an application.
     */
    public InterviewDto scheduleInterview(InterviewCreateRequest req) {
        Application application = applicationRepository.findById(req.getApplicationId())
                .orElseThrow(() -> new EntityNotFoundException("Application", req.getApplicationId()));

        Interview interview = Interview.builder()
                .application(application)
                .roundNumber(req.getRoundNumber())
                .interviewType(req.getInterviewType())
                .scheduledAt(req.getScheduledAt())
                .durationMins(req.getDurationMins() != null ? req.getDurationMins() : 60)
                .meetingLink(req.getMeetingLink())
                .location(req.getLocation())
                .status(InterviewStatus.SCHEDULED)
                .build();

        return entityMapper.toInterviewDto(interviewRepository.save(interview));
    }

    /**
     * Retrieves an interview by its UUID.
     */
    @Transactional(readOnly = true)
    public InterviewDto getInterviewById(String id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interview", id));
        return entityMapper.toInterviewDto(interview);
    }

    /**
     * Returns all interview rounds for a given application.
     */
    @Transactional(readOnly = true)
    public List<InterviewDto> getInterviewsForApplication(String applicationId) {
        return interviewRepository.findByApplicationId(applicationId)
                .stream()
                .map(entityMapper::toInterviewDto)
                .collect(Collectors.toList());
    }

    /**
     * Records feedback and outcome for an interview.
     */
    public InterviewDto updateInterviewFeedback(String id, InterviewFeedbackRequest req) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interview", id));

        interview.setStatus(req.getStatus());
        if (req.getFeedback() != null) interview.setFeedback(req.getFeedback());
        if (req.getScore()    != null) interview.setScore(req.getScore());

        return entityMapper.toInterviewDto(interviewRepository.save(interview));
    }

    /**
     * Cancels an interview by setting its status to CANCELLED.
     */
    public void cancelInterview(String id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interview", id));
        interview.setStatus(InterviewStatus.CANCELLED);
        interviewRepository.save(interview);
        log.info("Interview {} cancelled", id);
    }
}
