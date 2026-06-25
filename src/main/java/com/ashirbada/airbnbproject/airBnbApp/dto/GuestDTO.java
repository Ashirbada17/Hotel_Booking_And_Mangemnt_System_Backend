package com.ashirbada.airbnbproject.airBnbApp.dto;

import com.ashirbada.airbnbproject.airBnbApp.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestDTO {

    private Long id;
    private String name;
    private Gender gender;
    private LocalDate dateOfBirth;
}
