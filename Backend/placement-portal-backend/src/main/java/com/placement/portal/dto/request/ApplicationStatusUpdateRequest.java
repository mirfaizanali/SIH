package com.placement.portal.dto.request;

import com.placement.portal.domain.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationStatusUpdateRequest {

    @NotNull
    private ApplicationStatus status;
}
