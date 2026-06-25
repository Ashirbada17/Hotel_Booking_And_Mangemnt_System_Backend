package com.ashirbada.airbnbproject.airBnbApp.strategy;

import com.ashirbada.airbnbproject.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PricingService {

    public BigDecimal calculateDynamicPricing(Inventory inventory) {
        PricingStrategy pricingStrategy = new BasePricingStrategy();
        // apply the additional strategies
        pricingStrategy = new SurgePriceStrategy(pricingStrategy);
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
        pricingStrategy = new HolidayPricingStrategy(pricingStrategy);
        return pricingStrategy.calculatePrice(inventory).setScale(2, RoundingMode.CEILING);
    }

    // kept for backward compatibility
    public BigDecimal calculateDynamicPricingStrategy(Inventory inventory) {
        return calculateDynamicPricing(inventory);
    }

    // return the sum of price of this inventory list
    public BigDecimal calculateTotalPricing(List<Inventory> inventoryList) {
        return inventoryList.stream()
                .map(this::calculateDynamicPricing)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.CEILING);
    }
}
