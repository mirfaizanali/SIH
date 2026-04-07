package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementDriveDto {

    private String id;
    private String employerId;
    private String companyName;
    private String organizedById;
    private String title;
    private LocalDate driveDate;
    private String venue;
    private String mode;
    private String status;
    private String description;
    private List<JobDto> jobs;
}
