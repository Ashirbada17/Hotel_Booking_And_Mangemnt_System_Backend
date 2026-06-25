package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.dto.BookingDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.BookingRequest;
import com.ashirbada.airbnbproject.airBnbApp.dto.BookingsTableResponseDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.HotelReportDTO;
import com.ashirbada.airbnbproject.airBnbApp.entity.*;
import com.ashirbada.airbnbproject.airBnbApp.entity.enums.BookingStatus;
import com.ashirbada.airbnbproject.airBnbApp.exception.ResourceNotFoundException;
import com.ashirbada.airbnbproject.airBnbApp.exception.UnAuthoriseException;
import com.ashirbada.airbnbproject.airBnbApp.repository.*;
import com.ashirbada.airbnbproject.airBnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ashirbada.airbnbproject.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;
    private final ModelMapper modelMapper;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDTO initialiseBooking(BookingRequest bookingRequest) {
        log.info("Initialising booking for hotel id : {} and room id : {} from date : {} to date : {} for rooms count : {}",
                bookingRequest.getHotelId(), bookingRequest.getRoomId(),
                bookingRequest.getCheckedInDate(), bookingRequest.getCheckedOutDate(), bookingRequest.getRoomsCount());

        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + bookingRequest.getHotelId()));
        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id : " + bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                room.getId(), bookingRequest.getCheckedInDate(), bookingRequest.getCheckedOutDate(), bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckedInDate(), bookingRequest.getCheckedOutDate()) + 1;
        if (inventoryList.size() != daysCount) throw new IllegalStateException("Room is not available anymore");

        inventoryRepository.initBooking(room.getId(), bookingRequest.getCheckedInDate(),
                bookingRequest.getCheckedOutDate(), bookingRequest.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPricing(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()))
                .setScale(2, RoundingMode.CEILING);

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkedInDate(bookingRequest.getCheckedInDate())
                .checkedOutDate(bookingRequest.getCheckedOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
                .build();
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDTO.class);
    }

    @Override
    @Transactional
    public BookingDTO addGuests(Long bookingId, List<Long> guestIdList) {
        log.info("Adding guests to booking with id : {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id : " + bookingId));
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthoriseException("Booking does not belong to this user with id " + user.getId());
        }
        if (hasBookingExpired(booking)) throw new IllegalStateException("Booking has already expired");
        if (booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Guests can only be added to reserved bookings");
        }

        for (Long guestId : guestIdList) {
            Guest guest = guestRepository.findById(guestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDTO.class);
    }

    @Override
    @Transactional
    public BookingDTO removeGuestFromBooking(Long bookingId, List<Long> guestIdList) {
        log.info("Removing guests with ids: {} from booking with id: {}", guestIdList, bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthoriseException("Booking does not belong to this user with id: " + user.getId());
        }
        if (hasBookingExpired(booking)) throw new IllegalStateException("Booking has already expired");

        for (Long guestId : guestIdList) {
            Guest guest = guestRepository.findById(guestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));
            if (!booking.getGuests().remove(guest)) {
                throw new IllegalStateException("Guest with id: " + guestId + " is not associated with this booking");
            }
        }

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDTO.class);
    }

    @Override
    @Transactional
    public String initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthoriseException("Booking does not belong to this user with id " + user.getId());
        }
        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }
        String sessionUrl = checkoutService.getCheckoutSession(booking,
                frontendUrl + "/payments/" + bookingId + "/status",
                frontendUrl + "/payments/" + bookingId + "/status");
        booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
//    public void capturePayment(Event event) {
//        if (!"checkout.session.completed".equals(event.getType())) {
//            log.warn("Unhandled event type: {}", event.getType());
//            return;
//        }
//
//        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
//        Session session = null;
//
//        // 1. Try normal safe deserialization
//        StripeObject stripeObject = deserializer.getObject().orElse(null);
//
//        if (stripeObject instanceof Session s) {
//            session = s;
//            log.info("Session deserialized automatically for event {}", event.getId());
//        } else {
//            // 2. Fallback if Stripe couldn't deserialize because of API version mismatch
//            try {
//                StripeObject unsafeObject = deserializer.deserializeUnsafe();
//                if (unsafeObject instanceof Session s) {
//                    session = s;
//                    log.info("Session deserialized using deserializeUnsafe() for event {}", event.getId());
//                }
//            } catch (Exception e) {
//                log.error("Failed to deserialize checkout session for event {}", event.getId(), e);
//                return;
//            }
//        }
//
//        if (session == null) {
//            log.error("Session is null for event {}", event.getId());
//            return;
//        }
//
//        String sessionId = session.getId();
//        log.info("Checkout session id: {}", sessionId);
//
//        Booking booking = bookingRepository.findByPaymentSessionId(sessionId).orElse(null);
//        if (booking == null) {
//            log.error("Booking not found for session id: {}", sessionId);
//            return;
//        }
//
//        booking.setBookingStatus(BookingStatus.CONFIRMED);
//        bookingRepository.save(booking);
//
//        inventoryRepository.findAndLockReservedInventory(
//                booking.getRoom().getId(),
//                booking.getCheckedInDate(),
//                booking.getCheckedOutDate(),
//                booking.getRoomsCount()
//        );
//
//        inventoryRepository.confirmBooking(
//                booking.getRoom().getId(),
//                booking.getCheckedInDate(),
//                booking.getCheckedOutDate(),
//                booking.getRoomsCount()
//        );
//
//        log.info("Successfully confirmed booking for booking id: {}", booking.getId());
//    }
    public void capturePayment(Event event) {

        try {
            log.info("Received Stripe event: {} with id: {}",
                    event.getType(),
                    event.getId());

            if (!"checkout.session.completed".equals(event.getType())) {
                log.warn("Unhandled event type: {}", event.getType());
                return;
            }

            log.info("Processing checkout.session.completed event");

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            Session session = null;

            StripeObject stripeObject = deserializer.getObject().orElse(null);

            if (stripeObject instanceof Session s) {
                session = s;
                log.info("Session deserialized automatically for event {}", event.getId());
            } else {
                log.warn("Safe deserialization failed. Trying deserializeUnsafe()");

                try {
                    StripeObject unsafeObject = deserializer.deserializeUnsafe();

                    if (unsafeObject instanceof Session s) {
                        session = s;
                        log.info("Session deserialized using deserializeUnsafe() for event {}",
                                event.getId());
                    }

                } catch (Exception e) {
                    log.error("Failed to deserialize checkout session", e);
                    return;
                }
            }

            if (session == null) {
                log.error("Session is null for event {}", event.getId());
                return;
            }

            String sessionId = session.getId();
            log.info("Checkout session id: {}", sessionId);

            log.info("Fetching booking using paymentSessionId: {}", sessionId);

            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElse(null);

            if (booking == null) {
                log.error("Booking not found for session id: {}", sessionId);
                return;
            }

            log.info("Booking found. Booking id: {}", booking.getId());

            log.info("Updating booking status to CONFIRMED");

            booking.setBookingStatus(BookingStatus.CONFIRMED);

            log.info("Saving booking");

            bookingRepository.save(booking);

            log.info("Booking saved successfully");

            log.info("Room id: {}", booking.getRoom().getId());
            log.info("CheckIn: {}", booking.getCheckedInDate());
            log.info("CheckOut: {}", booking.getCheckedOutDate());
            log.info("Rooms Count: {}", booking.getRoomsCount());

            log.info("Calling findAndLockReservedInventory()");

            inventoryRepository.findAndLockReservedInventory(
                    booking.getRoom().getId(),
                    booking.getCheckedInDate(),
                    booking.getCheckedOutDate(),
                    booking.getRoomsCount()
            );

            log.info("findAndLockReservedInventory() completed");

            log.info("Calling confirmBooking()");

            inventoryRepository.confirmBooking(
                    booking.getRoom().getId(),
                    booking.getCheckedInDate(),
                    booking.getCheckedOutDate(),
                    booking.getRoomsCount()
            );

            log.info("confirmBooking() completed");

            log.info("Successfully confirmed booking for booking id: {}",
                    booking.getId());

        } catch (Exception e) {
            log.error("Error while processing Stripe webhook", e);
            throw e; // keep this for now so you can see the 500 cause
        }
    }



    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthoriseException("Booking does not belong to this user with id " + user.getId());
        }
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(
                booking.getRoom().getId(), booking.getCheckedInDate(), booking.getCheckedOutDate(), booking.getRoomsCount());
        inventoryRepository.cancelBooking(
                booking.getRoom().getId(), booking.getCheckedInDate(), booking.getCheckedOutDate(), booking.getRoomsCount());

        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BookingDTO getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthoriseException("Booking does not belong to this user with id: " + user.getId());
        }
        return modelMapper.map(booking, BookingDTO.class);
    }

    @Override
    public List<BookingsTableResponseDTO> getAllBookingByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));
        User user = getCurrentUser();
        log.info("Getting all bookings for the hotel with id {}", hotelId);
        if (!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id: " + hotelId);

        List<BookingStatus> statuses = Arrays.asList(
                BookingStatus.CONFIRMED,
                BookingStatus.CANCELLED,
                BookingStatus.PAYMENTS_PENDING
        );
        List<Booking> bookings = bookingRepository.findByHotelAndBookingStatusInOrderByCreatedAtDesc(hotel, statuses);

        return bookings.stream()
                .map(element -> {
                    BookingsTableResponseDTO dto = modelMapper.map(element, BookingsTableResponseDTO.class);
                    dto.setRoomType(element.getRoom().getType());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public HotelReportDTO getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));
        User user = getCurrentUser();
        log.info("Generating report for the hotel with id {}", hotelId);
        if (!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id: " + hotelId);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);

        Long totalConfirmedBooking = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();
        BigDecimal totalRevenueOfConfirmedBooking = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgRevenue = totalConfirmedBooking == 0 ? BigDecimal.ZERO :
                totalRevenueOfConfirmedBooking.divide(BigDecimal.valueOf(totalConfirmedBooking), RoundingMode.HALF_UP);

        return new HotelReportDTO(totalConfirmedBooking, totalRevenueOfConfirmedBooking, avgRevenue);
    }

    @Override
    public List<BookingDTO> getMyBookings() {
        User user = getCurrentUser();
        List<BookingStatus> statuses = Arrays.asList(
                BookingStatus.CONFIRMED,
                BookingStatus.CANCELLED
        );
        return bookingRepository.findByUserAndBookingStatusIn(user, statuses)
                .stream()
                .map(element -> modelMapper.map(element, BookingDTO.class))
                .collect(Collectors.toList());
    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
