package openerp.openerpresourceserver.wms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.forecast.DailyProductForecastDTO;
import openerp.openerpresourceserver.wms.entity.InventoryItem;
import openerp.openerpresourceserver.wms.entity.Product;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.timeseries.arima.Arima;
import openerp.openerpresourceserver.wms.timeseries.arima.struct.ForecastResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForeCastServiceImpl implements ForecastService {

    private final ShipmentRepo shipmentRepo;
    private final OrderItemBillingRepo orderItemBillingRepo;
    private final ProductRepo productRepo;
    private final InventoryItemRepo inventoryItemRepo;
    private final InventoryItemDetailRepo inventoryItemDetailRepo;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    private static final int MAX_DAYS_HISTORY = 360; // Maximum 30 days of historical data
    private static final int FORECAST_DAYS = 7; // Forecast for the next day
    private static final int LOW_STOCK_THRESHOLD = 1000; // Threshold for low stock products
    private static final int REDIS_EXPIRATION_DAYS = 7 * 24 * 60 * 60; // Cache expiration in days
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledDailyForecast() {
        log.info("Starting scheduled daily forecast job at {}", LocalDateTime.now());
        try {
            forecastDailyLowStockProducts();
            log.info("Completed scheduled daily forecast job at {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error during scheduled daily forecast: {}", e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<List<DailyProductForecastDTO>> forecastDailyLowStockProducts() {
        String redisKey = "LOW_STOCK_FORECAST:" + LocalDate.now().format(DATE_FORMATTER);
        List<DailyProductForecastDTO> cachedForecast = (List<DailyProductForecastDTO>) redisService.get(redisKey);

        if (cachedForecast != null && !cachedForecast.isEmpty()) {
            return ApiResponse.<List<DailyProductForecastDTO>>builder()
                    .code(200)
                    .message("Daily low stock forecast retrieved from cache")
                    .data(cachedForecast)
                    .build();
        }

        List<InventoryItem> lowStockItems = inventoryItemRepo.findByQuantityLessThan(LOW_STOCK_THRESHOLD);
        if (lowStockItems.isEmpty()) {
            return ApiResponse.<List<DailyProductForecastDTO>>builder()
                    .code(200)
                    .message("No low stock products found for forecasting")
                    .data(Collections.emptyList())
                    .build();
        }

        Set<String> lowStockProductIds = lowStockItems.stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());

        List<Product> lowStockProducts = productRepo.findAllById(lowStockProductIds);
        List<DailyProductForecastDTO> forecasts = new ArrayList<>();

        for (Product product : lowStockProducts) {
            DailyProductForecastDTO forecast = forecastProductDailyWithArima(product);
            if (forecast != null && forecast.getQuantity() > 0) {
                forecasts.add(forecast);
            }
        }
        redisService.save(redisKey, forecasts, REDIS_EXPIRATION_DAYS);
        return ApiResponse.<List<DailyProductForecastDTO>>builder()
                .code(200)
                .message("Daily low stock forecast generated successfully")
                .data(forecasts)
                .build();
    }

    private DailyProductForecastDTO forecastProductDailyWithArima(Product product) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(MAX_DAYS_HISTORY);

        List<Object[]> dailyData = inventoryItemDetailRepo.getDailyQuantitiesByProductAndShipmentType(
                product.getId(),
                ShipmentType.OUTBOUND.name(),
                startDate.atStartOfDay(),
                today.atTime(23, 59, 59)
        );

        Map<LocalDate, Integer> rawDailyData = new HashMap<>();
        for (Object[] dataPoint : dailyData) {
            LocalDate date = ((java.sql.Date) dataPoint[0]).toLocalDate();
            Long quantity = ((Number) dataPoint[1]).longValue();
            rawDailyData.put(date, quantity.intValue());
        }

        LocalDate current = startDate;
        while (!current.isAfter(today)) {
            rawDailyData.putIfAbsent(current, 0);
            current = current.plusDays(1);
        }

        double[] timeSeriesData = rawDailyData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .mapToDouble(Map.Entry::getValue)
                .toArray();

        try {
            ForecastResult forecastResult = Arima.auto_forecast_arima(timeSeriesData, FORECAST_DAYS);
            double[] forecast = forecastResult.getForecast();
            int predictedQuantity = 0;
            for(int i = 0; i < FORECAST_DAYS; i++) {
                predictedQuantity += Math.round(forecast[i]);
            }

            if (predictedQuantity < 0) {
                predictedQuantity = 0;
            }

            return DailyProductForecastDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .forecastDate(today.plusDays(1)) // Forecast for tomorrow
                    .quantity(predictedQuantity)
                    .price(product.getWholeSalePrice()) // Assuming this comes from Product entity
                    .unit(product.getUnit()) // Default to "Thùng" if null
                    .discount(BigDecimal.ZERO) // Default value, adjust if available
                    .tax(BigDecimal.ZERO) // Default value, adjust if available
                    .build();

        } catch (Exception e) {
            log.error("Error forecasting with ARIMA for product {}: {}", product.getId(), e.getMessage());
            return null;
        }
    }
}