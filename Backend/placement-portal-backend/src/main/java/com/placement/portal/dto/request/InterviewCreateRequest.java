package com.placement.portal.dto.request;

import com.placement.portal.domain.enums.InterviewType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewCreateRequest {

    @NotBlank
    private String applicationId;

    private int roundNumber;

    @NotNull
    private InterviewType interviewType;

    @NotNull
    private LocalDateTime scheduledAt;

    private Integer durationMins;
    private String meetingLink;
    private String location;
}
