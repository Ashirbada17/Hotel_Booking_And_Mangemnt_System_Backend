package com.ashirbada.airbnbproject.airBnbApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelInfoRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long roomsCount;
}
