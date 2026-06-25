package com.ashirbada.airbnbproject.airBnbApp.dto;

import com.ashirbada.airbnbproject.airBnbApp.entity.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingsTableResponseDTO {
    private Long id;
    private Integer roomsCount;
    private LocalDate checkedInDate;
    private LocalDate checkedOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDTO> guests;
    private BigDecimal amount;
    private String roomType;
    private UserDTO user;
}
