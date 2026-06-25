package com.ashirbada.airbnbproject.airBnbApp.security;

import com.ashirbada.airbnbproject.airBnbApp.dto.LoginDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.SignUpRequestDTO;
import com.ashirbada.airbnbproject.airBnbApp.dto.UserDTO;
import com.ashirbada.airbnbproject.airBnbApp.entity.User;
import com.ashirbada.airbnbproject.airBnbApp.entity.enums.Role;
import com.ashirbada.airbnbproject.airBnbApp.exception.ResourceNotFoundException;
import com.ashirbada.airbnbproject.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserDTO signUp(SignUpRequestDTO signUpRequestDTO){
        User user = userRepository.findByEmail(signUpRequestDTO.getEmail()).orElse(null);
        if(user != null){
            throw new RuntimeException("Usr is already registered");
        }
        User newUser = modelMapper.map(signUpRequestDTO,User.class);
        newUser.setRoles(Set.of(Role.GUEST,Role.HOTEL_MANAGER));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));
        newUser = userRepository.save(newUser);
        return modelMapper.map(newUser,UserDTO.class);
    }

    public String[] login(LoginDTO loginDTO){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword()));
        User user = (User) authentication.getPrincipal();
        String[] arr = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);
        return arr;
    }

    public String refreshToken(String refreshToken){
        Long id = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("user not found with this id"+id));
        return jwtService.generateAccessToken(user);
    }
}
