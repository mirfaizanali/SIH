package com.placement.portal.dto.request;

import com.placement.portal.domain.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportReviewRequest {

    /** Only APPROVED or REVISION_REQUESTED are valid review outcomes. */
    @NotNull
    private ReportStatus status;

    private String reviewerComments;
}
