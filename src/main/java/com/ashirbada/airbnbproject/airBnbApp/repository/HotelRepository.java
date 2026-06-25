package com.ashirbada.airbnbproject.airBnbApp.repository;

import com.ashirbada.airbnbproject.airBnbApp.dto.HotelDTO;
import com.ashirbada.airbnbproject.airBnbApp.entity.Hotel;
import com.ashirbada.airbnbproject.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByOwner(User user);
}
