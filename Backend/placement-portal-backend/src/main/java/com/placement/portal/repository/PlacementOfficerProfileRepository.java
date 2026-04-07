package com.placement.portal.repository;

import com.placement.portal.domain.PlacementOfficerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlacementOfficerProfileRepository extends JpaRepository<PlacementOfficerProfile, String> {

    Optional<PlacementOfficerProfile> findByUserId(String userId);
}
