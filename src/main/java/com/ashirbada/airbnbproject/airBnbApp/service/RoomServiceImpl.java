package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.dto.RoomDTO;
import com.ashirbada.airbnbproject.airBnbApp.entity.Hotel;
import com.ashirbada.airbnbproject.airBnbApp.entity.Room;
import com.ashirbada.airbnbproject.airBnbApp.entity.User;
import com.ashirbada.airbnbproject.airBnbApp.exception.ResourceNotFoundException;
import com.ashirbada.airbnbproject.airBnbApp.exception.UnAuthoriseException;
import com.ashirbada.airbnbproject.airBnbApp.repository.HotelRepository;
import com.ashirbada.airbnbproject.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.ashirbada.airbnbproject.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final PricingUpdateService pricingUpdateService;

    @Override
    public RoomDTO createNewRoom(Long hotelId, RoomDTO roomDto) {
        log.info("Creating a new room in hotel with id : {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthoriseException("This user does not own this hotel with id " + hotelId);
        }
        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        inventoryService.initializeRoomForAYear(room);
        pricingUpdateService.updateHotelPrices(hotel);

        log.info("Created a new room with ID : {} in hotel with id : {}", room.getId(), hotelId);
        return modelMapper.map(room, RoomDTO.class);
    }

    @Override
    public List<RoomDTO> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with id : {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthoriseException("This user does not own this hotel with id " + hotelId);
        }
        return hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room, RoomDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDTO getRoomById(Long roomId) {
        log.info("Getting the room with id : {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id : " + roomId));
        return modelMapper.map(room, RoomDTO.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with id : {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id : " + roomId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(room.getHotel().getOwner())) {
            throw new UnAuthoriseException("This user does not own this room with id " + roomId);
        }
        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);
        log.info("Deleted the room with id : {}", roomId);
    }

    @Override
    @Transactional
    public RoomDTO updateRoomById(Long hotelId, Long roomId, RoomDTO roomDTO) {
        log.info("Updating the room with id : {}", roomId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));
        User user = getCurrentUser();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthoriseException("This user does not own this hotel with id " + hotelId);
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id : " + roomId));
        modelMapper.map(roomDTO, room);
        room.setId(roomId);
        room = roomRepository.save(room);
        return modelMapper.map(room, RoomDTO.class);
    }
}
