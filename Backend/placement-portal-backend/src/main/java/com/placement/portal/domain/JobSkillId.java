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
public class JobSkillId implements Serializable {

    @Column(name = "job_id", length = 36)
    private String jobId;

    @Column(name = "skill_tag_id")
    private Integer skillTagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobSkillId that = (JobSkillId) o;
        return Objects.equals(jobId, that.jobId) &&
               Objects.equals(skillTagId, that.skillTagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, skillTagId);
    }
}
