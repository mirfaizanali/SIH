package com.placement.portal.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InternshipCreateRequest {

    @NotBlank
    private String title;

    private String description;
    private Integer durationMonths;
    private BigDecimal stipend;
    private String location;
    private boolean isRemote;
    private BigDecimal minCgpa;
    private LocalDate applicationDeadline;
}
