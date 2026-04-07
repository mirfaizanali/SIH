package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternshipEnrollmentDto {

    private String id;
    private String studentId;
    private String studentName;
    private String internshipId;
    private String internshipTitle;
    private String facultyMentorId;
    private String mentorName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
