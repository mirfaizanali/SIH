package com.placement.portal.dto.request;

import com.placement.portal.domain.enums.InterviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InterviewFeedbackRequest {

    @NotNull
    private InterviewStatus status;

    private String feedback;
    private BigDecimal score;
}
