package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyProfileDto {

    private String id;
    private String userId;
    private String fullName;
    private String email;
    private String employeeId;
    private String department;
    private String designation;
    private String phone;
}
