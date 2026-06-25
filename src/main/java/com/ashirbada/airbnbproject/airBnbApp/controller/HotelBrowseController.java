package com.ashirbada.airbnbproject.airBnbApp.controller;

import com.ashirbada.airbnbproject.airBnbApp.dto.HotelInfoDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.HotelInfoRequestDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.HotelPriceResponseDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.HotelSearchRequest;
import com.ashirbada.airbnbproject.airBnbApp.service.HotelService;
import com.ashirbada.airbnbproject.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceResponseDTO>> searchHotels(
            @RequestParam String city,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Integer roomsCount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        HotelSearchRequest hotelSearchRequest = new HotelSearchRequest(city, startDate, endDate, roomsCount, page, size);
        return ResponseEntity.ok(inventoryService.searchHotels(hotelSearchRequest));
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDTO> getHotelInfo(
            @PathVariable Long hotelId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Long roomsCount) {

        HotelInfoRequestDTO hotelInfoRequestDTO = new HotelInfoRequestDTO(startDate, endDate, roomsCount);
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId, hotelInfoRequestDTO));
    }
}
