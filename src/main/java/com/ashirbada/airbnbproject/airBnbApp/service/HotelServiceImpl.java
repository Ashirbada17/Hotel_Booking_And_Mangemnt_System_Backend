package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.dto.*;
import com.ashirbada.airbnbproject.airBnbApp.entity.Hotel;
import com.ashirbada.airbnbproject.airBnbApp.entity.Room;
import com.ashirbada.airbnbproject.airBnbApp.entity.User;
import com.ashirbada.airbnbproject.airBnbApp.exception.ResourceNotFoundException;
import com.ashirbada.airbnbproject.airBnbApp.exception.UnAuthoriseException;
import com.ashirbada.airbnbproject.airBnbApp.repository.HotelRepository;
import com.ashirbada.airbnbproject.airBnbApp.repository.InventoryRepository;
import com.ashirbada.airbnbproject.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.ashirbada.airbnbproject.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingUpdateService pricingUpdateService;

    @Override
    public HotelDTO createNewHotel(HotelDTO hotelDTO) {
        log.info("Creating a new hotel with name : {}", hotelDTO.getName());
        Hotel hotel = modelMapper.map(hotelDTO, Hotel.class);
        hotel.setActive(false);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
        Hotel saveHotel = hotelRepository.save(hotel);
        log.info("Created a new hotel with ID : {}", saveHotel.getId());
        return modelMapper.map(saveHotel, HotelDTO.class);
    }

    @Override
    public HotelDTO getHotelById(Long hotelId) {
        log.info("Getting the hotel with id : {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthoriseException("This user does not own this hotel with id " + hotelId);
        }
        return modelMapper.map(hotel, HotelDTO.class);
    }

    @Override
    public HotelDTO updateHotelById(Long hotelId, HotelDTO hotelDTO) {
        log.info("Updating the hotel with id : {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthoriseException("This user does not own this hotel with id " + hotelId);
        }
        modelMapper.map(hotelDTO, hotel);
        hotel.setId(hotelId);
        Hotel updatedHotel = hotelRepository.save(hotel);
        return modelMapper.map(updatedHotel, HotelDTO.class);
    }

    @Override
    @Transactional
    public Boolean deleteHotelById(Long hotelId) {
        boolean exists = hotelRepository.existsById(hotelId);
        if (!exists) throw new ResourceNotFoundException("Hotel not found with id : " + hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthoriseException("This user does not own this hotel with id " + hotelId);
        }
        for (Room room : hotel.getRooms()) {
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(hotelId);
        return true;
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating the hotel with id : {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthoriseException("This user does not own this hotel with id " + hotelId);
        }
        hotel.setActive(true);
        hotelRepository.save(hotel);

        // Initialize inventory for all rooms then update prices
        for (Room room : hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }
        pricingUpdateService.updateHotelPrices(hotel);
    }

    @Override
    public HotelInfoDTO getHotelInfoById(Long hotelId, HotelInfoRequestDTO hotelInfoRequestDTO) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));

        long daysCount = ChronoUnit.DAYS.between(hotelInfoRequestDTO.getStartDate(), hotelInfoRequestDTO.getEndDate()) + 1;

        List<RoomPriceDTO> roomPriceDTOList = inventoryRepository.findRoomAveragePrice(
                hotelId,
                hotelInfoRequestDTO.getStartDate(),
                hotelInfoRequestDTO.getEndDate(),
                hotelInfoRequestDTO.getRoomsCount(),
                daysCount);

        List<RoomPriceResponseDTO> rooms = roomPriceDTOList.stream()
                .map(roomPriceDTO -> {
                    RoomPriceResponseDTO roomPriceResponseDTO = modelMapper.map(roomPriceDTO.getRoom(), RoomPriceResponseDTO.class);
                    roomPriceResponseDTO.setPrice(roomPriceDTO.getPrice());
                    return roomPriceResponseDTO;
                })
                .collect(Collectors.toList());

        return new HotelInfoDTO(modelMapper.map(hotel, HotelDTO.class), rooms);
    }

    @Override
    public List<HotelDTO> getAllHotels() {
        User user = getCurrentUser();
        log.info("Getting all hotels for the admin user with ID : {}", user.getId());
        List<Hotel> hotels = hotelRepository.findByOwner(user);
        return hotels.stream()
                .map(element -> modelMapper.map(element, HotelDTO.class))
                .collect(Collectors.toList());
    }
}
