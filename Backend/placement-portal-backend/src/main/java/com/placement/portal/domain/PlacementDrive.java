package com.placement.portal.domain;

import com.placement.portal.domain.enums.DriveMode;
import com.placement.portal.domain.enums.DriveStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "placement_drives")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementDrive {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private EmployerProfile employer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organized_by_id")
    private PlacementOfficerProfile organizedBy;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "drive_date")
    private LocalDate driveDate;

    @Column(name = "venue", length = 255)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private DriveMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DriveStatus status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "drive_jobs",
        joinColumns = @JoinColumn(name = "drive_id"),
        inverseJoinColumns = @JoinColumn(name = "job_id")
    )
    private Set<Job> jobs = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = DriveStatus.SCHEDULED;
        }
        this.createdAt = LocalDateTime.now();
    }
}
