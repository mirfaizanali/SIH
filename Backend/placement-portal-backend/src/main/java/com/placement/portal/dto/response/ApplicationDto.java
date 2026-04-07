package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {

    private String id;
    private String studentId;
    private String studentName;
    private String jobId;
    private String jobTitle;
    private String internshipId;
    private String internshipTitle;
    private String resumeId;
    private String coverLetter;
    private String status;
    private LocalDateTime appliedAt;
}
