package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDto {

    private Integer id;
    private String name;
    private String category;
    /** Null when the skill is sourced from a SkillTag without a proficiency context. */
    private String proficiencyLevel;
}
