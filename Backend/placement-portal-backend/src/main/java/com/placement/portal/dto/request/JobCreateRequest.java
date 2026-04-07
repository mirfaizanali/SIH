package com.placement.portal.dto.request;

import com.placement.portal.domain.enums.ExperienceLevel;
import com.placement.portal.domain.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class JobCreateRequest {

    @NotBlank
    private String title;

    private String description;
    private String location;

    @NotNull
    private JobType jobType;

    @NotNull
    private ExperienceLevel experienceLevel;

    private BigDecimal minCgpa;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private int openingsCount;
    private LocalDate applicationDeadline;
    private List<Integer> skillIds;
    private List<Integer> mandatorySkillIds;
}
