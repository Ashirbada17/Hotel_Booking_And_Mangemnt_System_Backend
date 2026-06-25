package com.ashirbada.airbnbproject.airBnbApp.advice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private HttpStatus status;
    private String message;
    private List<String> subErrors;

    public ApiError(String message, HttpStatus httpStatus) {
        this.message = message;
        this.status =  httpStatus;
    }
}
