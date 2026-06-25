package com.ashirbada.airbnbproject.airBnbApp.dto;

import com.ashirbada.airbnbproject.airBnbApp.entity.enums.Gender;
import com.ashirbada.airbnbproject.airBnbApp.entity.enums.Role;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Set<Role> roles;
}
