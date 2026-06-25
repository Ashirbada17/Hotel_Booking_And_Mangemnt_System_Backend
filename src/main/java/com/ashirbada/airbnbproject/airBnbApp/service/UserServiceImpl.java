package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.dto.UserDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.UserProfileUpdateRequestDTO;
import com.ashirbada.airbnbproject.airBnbApp.entity.User;
import com.ashirbada.airbnbproject.airBnbApp.exception.ResourceNotFoundException;
import com.ashirbada.airbnbproject.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.ashirbada.airbnbproject.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not Found"+id));
    }

    @Override
    public void updateProfile(UserProfileUpdateRequestDTO userProfileUpdateRequestDTO) {
        User user = getCurrentUser();
        if(userProfileUpdateRequestDTO.getDateOfBirth() != null){
            user.setDateOfBirth(userProfileUpdateRequestDTO.getDateOfBirth());
        }
        if(userProfileUpdateRequestDTO.getGender()!= null){
            user.setGender(userProfileUpdateRequestDTO.getGender());
        }
        if(userProfileUpdateRequestDTO.getName()!=null){
            user.setName(userProfileUpdateRequestDTO.getName());
        }
        userRepository.save(user);
    }
    @Override
    public UserDTO getMyProfile() {
        User user = getCurrentUser();
        log.info("getting  the profile for user with id : {}",user.getId());
        return modelMapper.map(user,UserDTO.class);
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElse(null);
    }

}
