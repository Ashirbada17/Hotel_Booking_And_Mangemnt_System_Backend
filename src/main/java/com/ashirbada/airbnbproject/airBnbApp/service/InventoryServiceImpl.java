package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.dto.*;
import com.ashirbada.airbnbproject.airBnbApp.entity.Inventory;
import com.ashirbada.airbnbproject.airBnbApp.entity.Room;
import com.ashirbada.airbnbproject.airBnbApp.entity.User;
import com.ashirbada.airbnbproject.airBnbApp.exception.ResourceNotFoundException;
import com.ashirbada.airbnbproject.airBnbApp.repository.HotelMinPriceRepository;
import com.ashirbada.airbnbproject.airBnbApp.repository.InventoryRepository;
import com.ashirbada.airbnbproject.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.ashirbada.airbnbproject.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;

    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for (; !today.isAfter(endDate); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .reservedCount(0)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting all inventories for room with id : {}", room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceResponseDTO> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching hotels in city : {} from date : {} to date : {} for rooms count : {}",
                hotelSearchRequest.getCity(), hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(), hotelSearchRequest.getRoomsCount());
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());
        long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate()) + 1;

        Page<HotelPriceDTO> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(
                hotelSearchRequest.getCity(),
                hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(),
                hotelSearchRequest.getRoomsCount(),
                dateCount,
                pageable
        );

        return hotelPage.map(hotelPriceDTO -> {
            HotelPriceResponseDTO response = modelMapper.map(hotelPriceDTO.getHotel(), HotelPriceResponseDTO.class);
            response.setPrice(hotelPriceDTO.getPrice());
            return response;
        });
    }

    @Override
    public List<InventoryDTO> getAllInventoryByRoom(Long roomId) {
        log.info("Getting all inventory by room for room with id : {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id : " + roomId));
        User user = getCurrentUser();
        if (!user.equals(room.getHotel().getOwner()))
            throw new AccessDeniedException("You are not the owner of this hotel room with id " + roomId);

        return inventoryRepository.findByRoomOrderByDate(room)
                .stream()
                .map(element -> modelMapper.map(element, InventoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDTO updateInventoryRequestDTO) {
        log.info("Updating all inventory by room for room with id : {} between date range : {} - {}",
                roomId, updateInventoryRequestDTO.getStartDate(), updateInventoryRequestDTO.getEndDate());
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id : " + roomId));
        User user = getCurrentUser();
        if (!user.equals(room.getHotel().getOwner()))
            throw new AccessDeniedException("You are not the owner of this hotel room with id " + roomId);

        inventoryRepository.getInventoryAndLockBeforeUpdate(
                roomId, updateInventoryRequestDTO.getStartDate(), updateInventoryRequestDTO.getEndDate());
        inventoryRepository.updateInventory(
                roomId, updateInventoryRequestDTO.getStartDate(), updateInventoryRequestDTO.getEndDate(),
                updateInventoryRequestDTO.getClosed(), updateInventoryRequestDTO.getSurgeFactor());
    }
}
