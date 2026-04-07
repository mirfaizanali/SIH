package com.placement.portal.repository;

import com.placement.portal.domain.StudentSkill;
import com.placement.portal.domain.StudentSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentSkillRepository extends JpaRepository<StudentSkill, StudentSkillId> {

    List<StudentSkill> findByStudentProfileId(String studentProfileId);
}
