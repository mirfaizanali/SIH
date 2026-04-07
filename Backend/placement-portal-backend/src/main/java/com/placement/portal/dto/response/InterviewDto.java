package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewDto {

    private String id;
    private String applicationId;
    private int roundNumber;
    private String interviewType;
    private LocalDateTime scheduledAt;
    private Integer durationMins;
    private String meetingLink;
    private String location;
    private String status;
    private String feedback;
    private BigDecimal score;
}
