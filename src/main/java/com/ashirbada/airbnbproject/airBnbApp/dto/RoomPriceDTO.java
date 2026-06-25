package com.ashirbada.airbnbproject.airBnbApp.dto;

import com.ashirbada.airbnbproject.airBnbApp.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomPriceDTO {
    private Room room;
    private Double price;
}
