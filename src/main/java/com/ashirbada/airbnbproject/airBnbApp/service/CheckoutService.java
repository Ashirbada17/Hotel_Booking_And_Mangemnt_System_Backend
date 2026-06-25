package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
