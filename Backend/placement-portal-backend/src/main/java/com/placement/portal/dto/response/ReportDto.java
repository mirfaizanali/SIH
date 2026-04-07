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
public class ReportDto {

    private String id;
    private String enrollmentId;
    private String reportType;
    private String title;
    private String content;
    private String filePath;
    private LocalDateTime submittedAt;
    private String status;
    private String reviewerId;
    private String reviewerComments;
    private LocalDateTime reviewedAt;
}
