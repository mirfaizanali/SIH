package com.placement.portal.domain;

import com.placement.portal.domain.enums.ProficiencyLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSkill {

    @EmbeddedId
    private StudentSkillId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentProfileId")
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile studentProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("skillTagId")
    @JoinColumn(name = "skill_tag_id", nullable = false)
    private SkillTag skillTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level")
    private ProficiencyLevel proficiencyLevel;
}
