package com.placement.portal.repository;

import com.placement.portal.domain.JobSkill;
import com.placement.portal.domain.JobSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkillId> {

    List<JobSkill> findByJobId(String jobId);
}
