package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.dto.BookingDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.BookingRequest;
import com.ashirbada.airbnbproject.airBnbApp.dto.BookingsTableResponseDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.HotelReportDTO;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingDTO initialiseBooking(BookingRequest bookingRequest);

    BookingDTO addGuests(Long bookingId, List<Long> guestIdList);

    BookingDTO removeGuestFromBooking(Long bookingId, List<Long> guestIdList);

    String initiatePayment(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    BookingDTO getBookingById(Long bookingId);

    List<BookingsTableResponseDTO> getAllBookingByHotelId(Long hotelId);

    HotelReportDTO getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDTO> getMyBookings();
}
