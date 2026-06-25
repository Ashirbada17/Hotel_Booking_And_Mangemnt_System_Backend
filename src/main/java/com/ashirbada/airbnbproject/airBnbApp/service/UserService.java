package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.dto.UserDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.UserProfileUpdateRequestDTO;
import com.ashirbada.airbnbproject.airBnbApp.entity.User;

public interface UserService {
    User getUserById(Long id);

    void updateProfile(UserProfileUpdateRequestDTO userProfileUpdateRequestDTO);

    UserDTO getMyProfile();
}
