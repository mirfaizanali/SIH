package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {

    private String id;
    private String employerId;
    private String companyName;
    private String title;
    private String description;
    private String location;
    private String jobType;
    private String experienceLevel;
    private BigDecimal minCgpa;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private int openingsCount;
    private LocalDate applicationDeadline;
    private String status;
    private List<SkillDto> skills;
    private LocalDateTime createdAt;

    /**
     * Populated by the recommendation engine; {@code null} for ordinary job listings.
     * Represents the weighted match score (0–100) between this job and the requesting student.
     */
    private Double matchScore;
}
