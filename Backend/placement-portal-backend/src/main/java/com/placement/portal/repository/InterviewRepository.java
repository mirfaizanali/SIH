package com.placement.portal.repository;

import com.placement.portal.domain.Interview;
import com.placement.portal.domain.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, String> {

    List<Interview> findByApplicationId(String applicationId);

    List<Interview> findByStatusAndScheduledAtBetween(InterviewStatus status,
                                                       LocalDateTime from,
                                                       LocalDateTime to);
}
