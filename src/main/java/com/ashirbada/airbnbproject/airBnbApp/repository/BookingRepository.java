package com.ashirbada.airbnbproject.airBnbApp.repository;

import com.ashirbada.airbnbproject.airBnbApp.entity.Booking;
import com.ashirbada.airbnbproject.airBnbApp.entity.Hotel;
import com.ashirbada.airbnbproject.airBnbApp.entity.User;
import com.ashirbada.airbnbproject.airBnbApp.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Booking> findByUser(User user);

    List<Booking> findByUserAndBookingStatusIn(User user, Collection<BookingStatus> statuses);

    List<Booking> findByHotelAndBookingStatusInOrderByCreatedAtDesc(Hotel hotel, Collection<BookingStatus> statuses);
}
