package com.ashirbada.airbnbproject.airBnbApp.repository;

import com.ashirbada.airbnbproject.airBnbApp.entity.Room;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}
