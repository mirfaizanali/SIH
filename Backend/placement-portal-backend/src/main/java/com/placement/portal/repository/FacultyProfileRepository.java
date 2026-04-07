package com.placement.portal.repository;

import com.placement.portal.domain.FacultyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, String> {

    Optional<FacultyProfile> findByUserId(String userId);

    Optional<FacultyProfile> findByEmployeeId(String employeeId);
}
