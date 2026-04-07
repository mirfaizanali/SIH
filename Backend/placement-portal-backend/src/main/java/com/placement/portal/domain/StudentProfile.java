package com.placement.portal.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "student_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
                insertable = false, updatable = false)
    private User user;

    @Column(name = "roll_number", length = 50)
    private String rollNumber;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "batch_year")
    private Integer batchYear;

    @Column(name = "cgpa", precision = 4, scale = 2)
    private BigDecimal cgpa;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "preferred_locations", length = 500)
    private String preferredLocations;

    @Column(name = "preferred_job_types", length = 200)
    private String preferredJobTypes;

    @Builder.Default
    @Column(name = "is_placed", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isPlaced = false;

    @Column(name = "placement_package", precision = 10, scale = 2)
    private BigDecimal placementPackage;

    @Column(name = "placed_company", length = 255)
    private String placedCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_mentor_id")
    private FacultyProfile facultyMentor;

    @Builder.Default
    @OneToMany(mappedBy = "studentProfile", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentSkill> skills = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "studentProfile", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resume> resumes = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
