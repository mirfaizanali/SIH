package com.placement.portal.domain;

import com.placement.portal.domain.enums.CompanySize;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employer_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfile {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "company_website", length = 255)
    private String companyWebsite;

    @Column(name = "industry", length = 100)
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_size")
    private CompanySize companySize;

    @Column(name = "hr_contact_name", length = 255)
    private String hrContactName;

    @Column(name = "hr_contact_phone", length = 20)
    private String hrContactPhone;

    @Column(name = "location", length = 255)
    private String location;

    @Builder.Default
    @Column(name = "is_verified", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isVerified = false;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        this.createdAt = LocalDateTime.now();
    }
}
