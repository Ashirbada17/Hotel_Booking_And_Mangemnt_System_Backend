package com.ashirbada.airbnbproject.airBnbApp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {
    private Long hotelId;
    private Long roomId;
    private LocalDate checkedInDate;
    private LocalDate checkedOutDate;
    private Integer roomsCount;
}
