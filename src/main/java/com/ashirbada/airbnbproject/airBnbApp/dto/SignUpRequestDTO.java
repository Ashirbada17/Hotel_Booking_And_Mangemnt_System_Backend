package com.ashirbada.airbnbproject.airBnbApp.dto;


import lombok.Data;

@Data
public class SignUpRequestDTO {
    private String email;
    private String password;
    private String name;
}
