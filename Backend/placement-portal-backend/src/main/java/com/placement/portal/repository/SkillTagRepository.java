package com.placement.portal.repository;

import com.placement.portal.domain.SkillTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillTagRepository extends JpaRepository<SkillTag, Integer> {

    Optional<SkillTag> findByNameIgnoreCase(String name);

    List<SkillTag> findByCategory(String category);
}
