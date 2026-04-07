package com.placement.portal.repository;

import com.placement.portal.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, String> {

    List<Resume> findByStudentProfileId(String studentProfileId);

    Optional<Resume> findByStudentProfileIdAndIsPrimaryTrue(String studentProfileId);
}
