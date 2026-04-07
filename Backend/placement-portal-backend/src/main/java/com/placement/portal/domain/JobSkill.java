package com.placement.portal.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSkill {

    @EmbeddedId
    private JobSkillId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jobId")
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("skillTagId")
    @JoinColumn(name = "skill_tag_id", nullable = false)
    private SkillTag skillTag;

    @Builder.Default
    @Column(name = "is_mandatory", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isMandatory = true;
}
