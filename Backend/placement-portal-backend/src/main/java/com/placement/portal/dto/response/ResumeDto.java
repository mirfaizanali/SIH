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
public class ResumeDto {

    private String id;
    private String studentProfileId;
    private String fileName;
    private Long fileSizeBytes;
    private String contentType;
    private boolean isPrimary;
    private LocalDateTime uploadedAt;
}
