package com.placement.portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentSkillId implements Serializable {

    @Column(name = "student_profile_id", length = 36)
    private String studentProfileId;

    @Column(name = "skill_tag_id")
    private Integer skillTagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentSkillId that = (StudentSkillId) o;
        return Objects.equals(studentProfileId, that.studentProfileId) &&
               Objects.equals(skillTagId, that.skillTagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentProfileId, skillTagId);
    }
}
