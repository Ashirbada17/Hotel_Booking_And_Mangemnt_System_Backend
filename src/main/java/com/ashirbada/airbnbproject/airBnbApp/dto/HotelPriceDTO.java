package com.ashirbada.airbnbproject.airBnbApp.dto;

import com.ashirbada.airbnbproject.airBnbApp.entity.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPriceDTO {
    private Hotel hotel;
    private Double price;
}
