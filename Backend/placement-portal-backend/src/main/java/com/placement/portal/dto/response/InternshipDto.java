package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternshipDto {

    private String id;
    private String employerId;
    private String companyName;
    private String title;
    private String description;
    private Integer durationMonths;
    private BigDecimal stipend;
    private String location;
    private boolean isRemote;
    private BigDecimal minCgpa;
    private LocalDate applicationDeadline;
    private String status;
    private LocalDateTime createdAt;
}
