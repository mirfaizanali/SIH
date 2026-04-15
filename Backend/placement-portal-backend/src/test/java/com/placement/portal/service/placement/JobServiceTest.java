package com.placement.portal.service.placement;

import com.placement.portal.domain.Job;
import com.placement.portal.domain.enums.JobStatus;
import com.placement.portal.dto.response.JobDto;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.EmployerProfileRepository;
import com.placement.portal.repository.JobRepository;
import com.placement.portal.repository.SkillTagRepository;
import com.placement.portal.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @Mock
    private SkillTagRepository skillTagRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private JobService jobService;

    private Job job1;
    private JobDto jobDto1;

    @BeforeEach
    void setUp() {
        job1 = new Job();
        job1.setId("job1");
        job1.setStatus(JobStatus.ACTIVE);
        job1.setApplicationDeadline(LocalDate.now()); // Today
        
        jobDto1 = new JobDto();
        jobDto1.setId("job1");
    }

    @Test
    void testGetActiveJobs_IncludesTodayAndNullDeadlines() {
        // Arrange
        Page<Job> jobPage = new PageImpl<>(List.of(job1));
        when(jobRepository.findActiveJobs(eq(JobStatus.ACTIVE), eq(LocalDate.now()), any(Pageable.class)))
                .thenReturn(jobPage);
        when(entityMapper.toJobDto(job1)).thenReturn(jobDto1);

        // Act
        Page<JobDto> result = jobService.getActiveJobs(null, null, Pageable.unpaged());

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("job1", result.getContent().get(0).getId());
    }
}
