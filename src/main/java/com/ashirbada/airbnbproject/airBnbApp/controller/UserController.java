package com.ashirbada.airbnbproject.airBnbApp.controller;

import com.ashirbada.airbnbproject.airBnbApp.dto.BookingDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.GuestDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.UserDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.UserProfileUpdateRequestDTO;
import com.ashirbada.airbnbproject.airBnbApp.service.BookingService;
import com.ashirbada.airbnbproject.airBnbApp.service.GuestService;
import com.ashirbada.airbnbproject.airBnbApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;
    private final GuestService guestService;

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody UserProfileUpdateRequestDTO userProfileUpdateRequestDTO) {
        userService.updateProfile(userProfileUpdateRequestDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDTO>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @GetMapping("/guests")
    public ResponseEntity<List<GuestDTO>> getAllGuests() {
        return ResponseEntity.ok(guestService.getAllGuests());
    }

    @PostMapping("/guests")
    public ResponseEntity<GuestDTO> addNewGuest(@RequestBody GuestDTO guestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guestService.addNewGuest(guestDTO));
    }

    @PutMapping("/guests/{guestId}")
    public ResponseEntity<Void> updateGuest(@PathVariable Long guestId, @RequestBody GuestDTO guestDTO) {
        guestService.updateGuest(guestId, guestDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/guests/{guestId}")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long guestId) {
        guestService.deleteGuest(guestId);
        return ResponseEntity.noContent().build();
    }
}
