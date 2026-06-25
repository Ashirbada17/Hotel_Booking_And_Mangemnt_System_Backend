package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.dto.HotelDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.HotelInfoDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.HotelInfoRequestDTO;

import java.util.List;

public interface HotelService {
    HotelDTO createNewHotel(HotelDTO hotelDTO);
    HotelDTO getHotelById(Long hotelId);
    HotelDTO updateHotelById(Long id, HotelDTO hotelDTO);
    Boolean deleteHotelById(Long hotelId);
    void activateHotel(Long hotelId);

    HotelInfoDTO getHotelInfoById(Long hotelId, HotelInfoRequestDTO hotelInfoRequestDTO);

    List<HotelDTO> getAllHotels();
}
