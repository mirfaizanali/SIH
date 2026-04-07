package com.placement.portal.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skill_tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "category", length = 100)
    private String category;
}
