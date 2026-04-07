package com.placement.portal.repository;

import com.placement.portal.domain.EmployerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerProfileRepository extends JpaRepository<EmployerProfile, String> {

    Optional<EmployerProfile> findByUserId(String userId);

    List<EmployerProfile> findByIsVerifiedTrue();

    // ---------------------------------------------------------------------------
    // Analytics queries
    // ---------------------------------------------------------------------------

    /** Counts employers that have been verified by the placement office. */
    long countByIsVerifiedTrue();
}
