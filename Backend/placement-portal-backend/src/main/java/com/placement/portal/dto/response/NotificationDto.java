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
public class NotificationDto {

    private String id;
    private String type;
    private String title;
    private String message;
    private boolean isRead;
    private String referenceType;
    private String referenceId;
    private LocalDateTime createdAt;
}
