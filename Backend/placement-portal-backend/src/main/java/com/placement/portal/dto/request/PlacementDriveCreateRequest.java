package com.placement.portal.dto.request;

import com.placement.portal.domain.enums.DriveMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PlacementDriveCreateRequest {

    @NotBlank
    private String title;

    @NotNull
    private LocalDate driveDate;

    private String venue;

    @NotNull
    private DriveMode mode;

    private List<String> jobIds;
    private String description;
}
