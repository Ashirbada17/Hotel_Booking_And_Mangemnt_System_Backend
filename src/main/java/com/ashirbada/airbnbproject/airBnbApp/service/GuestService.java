package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.dto.GuestDTO;

import java.util.List;

public interface GuestService {

    List<GuestDTO> getAllGuests();

    void updateGuest(Long guestId, GuestDTO guestDTO);

    void deleteGuest(Long guestId);

    GuestDTO addNewGuest(GuestDTO guestDTO);
}
