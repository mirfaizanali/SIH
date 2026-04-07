package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileDto {

    private String id;
    private String userId;
    private String fullName;
    private String email;
    private String rollNumber;
    private String department;
    private Integer batchYear;
    private BigDecimal cgpa;
    private String phone;
    private String linkedinUrl;
    private String githubUrl;
    private String bio;
    private boolean isPlaced;
    private BigDecimal placementPackage;
    private String placedCompany;
    private String facultyMentorId;
    private List<SkillDto> skills;
    private LocalDateTime createdAt;
}
