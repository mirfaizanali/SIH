package com.placement.portal.dto.request;

import lombok.Data;

@Data
public class EmployerProfileUpdateRequest {

    private String companyName;
    private String companyWebsite;
    private String industry;
    private String companySize;
    private String hrContactName;
    private String hrContactPhone;
    private String logoUrl;
    private String location;
    private String description;
}
