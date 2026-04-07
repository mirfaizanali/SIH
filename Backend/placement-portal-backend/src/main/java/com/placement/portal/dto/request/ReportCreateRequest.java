package com.placement.portal.dto.request;

import com.placement.portal.domain.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportCreateRequest {

    @NotNull
    private ReportType reportType;

    @NotBlank
    private String title;

    private String content;

    @NotBlank
    private String enrollmentId;
}
