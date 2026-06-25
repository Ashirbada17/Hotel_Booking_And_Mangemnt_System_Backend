package com.ashirbada.airbnbproject.airBnbApp.strategy;

import com.ashirbada.airbnbproject.airBnbApp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(Inventory inventory);
}
