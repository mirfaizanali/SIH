package com.placement.portal.dto.request;

import lombok.Data;

@Data
public class StudentProfileUpdateRequest {

    private String phone;
    private String linkedinUrl;
    private String githubUrl;
    private String bio;
    private String preferredLocations;
    private String preferredJobTypes;
}
