package com.placement.portal.repository;

import com.placement.portal.domain.Internship;
import com.placement.portal.domain.enums.InternshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, String> {

    List<Internship> findByStatus(InternshipStatus status);

    List<Internship> findByEmployerId(String employerId);

    // ---------------------------------------------------------------------------
    // Analytics queries
    // ---------------------------------------------------------------------------

    /** Returns the total number of internships with the given status. */
    long countByStatus(InternshipStatus status);
}
