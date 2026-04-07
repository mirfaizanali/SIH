package com.placement.portal.domain;

import com.placement.portal.domain.enums.InterviewStatus;
import com.placement.portal.domain.enums.InterviewType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Builder.Default
    @Column(name = "round_number", nullable = false)
    private int roundNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type")
    private InterviewType interviewType;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Builder.Default
    @Column(name = "duration_mins")
    private Integer durationMins = 60;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "location", length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterviewStatus status;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = InterviewStatus.SCHEDULED;
        }
        this.createdAt = LocalDateTime.now();
    }
}
