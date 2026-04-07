package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfileDto {

    private String id;
    private String userId;
    private String companyName;
    private String companyWebsite;
    private String industry;
    private String companySize;
    private String hrContactName;
    private String hrContactPhone;
    private String location;
    private boolean isVerified;
    private String logoUrl;
    private String description;
}
