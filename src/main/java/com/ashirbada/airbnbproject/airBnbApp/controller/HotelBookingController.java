package com.ashirbada.airbnbproject.airBnbApp.controller;

import com.ashirbada.airbnbproject.airBnbApp.dto.BookingDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.BookingPaymentInitResponseDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.BookingRequest;
import com.ashirbada.airbnbproject.airBnbApp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDTO> initialiseBooking(@RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDTO> addGuests(@PathVariable Long bookingId,
                                                @RequestBody List<Long> guestIdList) {
        return ResponseEntity.ok(bookingService.addGuests(bookingId, guestIdList));
    }

    @PostMapping("/{bookingId}/removeGuests")
    public ResponseEntity<BookingDTO> removeGuest(@PathVariable Long bookingId,
                                                  @RequestBody List<Long> guestIdList) {
        return ResponseEntity.ok(bookingService.removeGuestFromBooking(bookingId, guestIdList));
    }

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<BookingPaymentInitResponseDTO> initiatePayment(@PathVariable Long bookingId) {
        String sessionUrl = bookingService.initiatePayment(bookingId);
        return ResponseEntity.ok(new BookingPaymentInitResponseDTO(sessionUrl));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }
}
