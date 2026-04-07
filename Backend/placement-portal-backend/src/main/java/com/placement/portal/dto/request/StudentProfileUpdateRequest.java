package com.placement.portal.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentProfileUpdateRequest {

    private String rollNumber;
    private String department;
    private Integer batchYear;
    private BigDecimal cgpa;
    private String phone;
    private String linkedinUrl;
    private String githubUrl;
    private String bio;
    private String preferredLocations;
    private String preferredJobTypes;
}
