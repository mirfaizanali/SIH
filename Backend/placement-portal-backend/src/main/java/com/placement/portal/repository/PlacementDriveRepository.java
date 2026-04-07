package com.placement.portal.repository;

import com.placement.portal.domain.PlacementDrive;
import com.placement.portal.domain.enums.DriveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlacementDriveRepository extends JpaRepository<PlacementDrive, String> {

    List<PlacementDrive> findByStatus(DriveStatus status);

    List<PlacementDrive> findByOrganizedById(String organizedById);

    List<PlacementDrive> findByDriveDateBetween(LocalDate startDate, LocalDate endDate);

    // ---------------------------------------------------------------------------
    // Analytics queries
    // ---------------------------------------------------------------------------

    /** Returns the total number of placement drives with the given status. */
    long countByStatus(DriveStatus status);
}
