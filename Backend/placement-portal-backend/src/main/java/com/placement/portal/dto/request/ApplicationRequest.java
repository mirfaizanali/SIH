package com.placement.portal.dto.request;

import lombok.Data;

@Data
public class ApplicationRequest {

    /** Mutually exclusive with internshipId. */
    private String jobId;

    /** Mutually exclusive with jobId. */
    private String internshipId;

    private String resumeId;
    private String coverLetter;
}
