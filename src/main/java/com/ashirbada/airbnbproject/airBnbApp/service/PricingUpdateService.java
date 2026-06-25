package com.ashirbada.airbnbproject.airBnbApp.service;

import com.ashirbada.airbnbproject.airBnbApp.entity.Hotel;
import com.ashirbada.airbnbproject.airBnbApp.entity.HotelMinPrice;
import com.ashirbada.airbnbproject.airBnbApp.entity.Inventory;
import com.ashirbada.airbnbproject.airBnbApp.repository.HotelMinPriceRepository;
import com.ashirbada.airbnbproject.airBnbApp.repository.HotelRepository;
import com.ashirbada.airbnbproject.airBnbApp.repository.InventoryRepository;
import com.ashirbada.airbnbproject.airBnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

    @Scheduled(cron = "0 0 * * * *")
    public void updatePricing() {
        int page = 0;
        int batchSize = 100;
        while (true) {
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));
            if (hotelPage.isEmpty()) break;
            hotelPage.getContent().forEach(this::updateHotelPrices);
            page++;
        }
    }

    public void updateHotelPrices(Hotel hotel) {
        log.info("Updating prices for hotel with id : {}", hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusYears(1);
        List<Inventory> inventories = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);
        updateInventoryPrices(inventories);
        updateHotelMinPrice(hotel, inventories, startDate, endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> dailyMinPrice = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().orElse(BigDecimal.ZERO)));

        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrice.forEach((date, price) -> {
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel, date)
                    .orElse(new HotelMinPrice(hotel, date));
            hotelPrice.setPrice(price.setScale(2, RoundingMode.CEILING));
            hotelPrices.add(hotelPrice);
        });
        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList) {
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricingStrategy(inventory);
            inventory.setPrice(dynamicPrice.setScale(2, RoundingMode.CEILING));
        });
        inventoryRepository.saveAll(inventoryList);
    }
}
