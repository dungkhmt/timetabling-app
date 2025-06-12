package openerp.openerpresourceserver.wms.service;

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

    private static final int HISTORICAL_DAYS = 30; // Last 30 days of historical data
    private static final int FORECAST_DAYS = 7; // Forecast for the next 7 days
    private static final int LOW_STOCK_THRESHOLD = 20000; // Threshold for low stock products
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
        List<DailyProductForecastDTO> cachedForecast = null;
        
        try {
           cachedForecast = (List<DailyProductForecastDTO>) redisService.get(redisKey);
        } catch (Exception e) {
            log.error("Error retrieving forecast from Redis: {}", e.getMessage());
            cachedForecast = null;
        }
        
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
            DailyProductForecastDTO forecast = forecastProductWithDetailedData(product);
            if (forecast != null) {
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

    private DailyProductForecastDTO forecastProductWithDetailedData(Product product) {
        LocalDate today = LocalDate.now();
        LocalDate historicalStartDate = today.minusDays(HISTORICAL_DAYS);

        // Get historical outbound data for the last 30 days
        List<Object[]> historicalData = inventoryItemDetailRepo.getDailyQuantitiesByProductAndShipmentType(
                product.getId(),
                ShipmentType.OUTBOUND.name(),
                historicalStartDate.atStartOfDay(),
                today.atTime(23, 59, 59)
        );

        // Process historical data
        Map<LocalDate, Integer> rawHistoricalData = new HashMap<>();
        for (Object[] dataPoint : historicalData) {
            LocalDate date = ((java.sql.Date) dataPoint[0]).toLocalDate();
            Long quantity = ((Number) dataPoint[1]).longValue();
            rawHistoricalData.put(date, quantity.intValue());
        }

        // Fill missing dates with 0
        Map<String, Integer> historicalDataMap = new LinkedHashMap<>();
        LocalDate current = historicalStartDate;
        while (!current.isAfter(today)) {
            int quantity = rawHistoricalData.getOrDefault(current, 0);
            historicalDataMap.put(current.format(DATE_FORMATTER), quantity);
            current = current.plusDays(1);
        }

        // Prepare time series data for ARIMA
        double[] timeSeriesData = historicalDataMap.values().stream()
                .mapToDouble(Integer::doubleValue)
                .toArray();

        try {
            // Generate forecast using ARIMA
            ForecastResult forecastResult = Arima.auto_forecast_arima(timeSeriesData, FORECAST_DAYS);
            double[] forecast = forecastResult.getForecast();

            // Process forecast data
            Map<String, Integer> forecastDataMap = new LinkedHashMap<>();
            int totalPredictedQuantity = 0;
            
            for (int i = 0; i < FORECAST_DAYS; i++) {
                LocalDate forecastDate = today.plusDays(i + 1);
                int predictedQuantity = Math.max(0, (int) Math.round(forecast[i]));
                forecastDataMap.put(forecastDate.format(DATE_FORMATTER), predictedQuantity);
                totalPredictedQuantity += predictedQuantity;
            }

            // Calculate statistics
            List<Integer> historicalValues = new ArrayList<>(historicalDataMap.values());
            int averageDaily = (int) historicalValues.stream().mapToInt(Integer::intValue).average().orElse(0);
            int maxDaily = historicalValues.stream().mapToInt(Integer::intValue).max().orElse(0);
            int minDaily = historicalValues.stream().mapToInt(Integer::intValue).min().orElse(0);

            // Get current stock
//            InventoryItem currentInventory = inventoryItemRepo.countByProductId(product.getId());
            int currentStock = inventoryItemRepo.sumByProductId(product.getId());
//            currentStock = currentStock != null ? currentStock : 0;

            return DailyProductForecastDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .forecastDate(today.plusDays(1))
                    .totalPredictedQuantity(totalPredictedQuantity)
                    .price(product.getWholeSalePrice())
                    .unit(product.getUnit())
                    .discount(BigDecimal.ZERO)
                    .tax(BigDecimal.ZERO)
                    .historicalData(historicalDataMap)
                    .forecastData(forecastDataMap)
                    .confidenceLevel(0.95) // Standard confidence level
                    .modelInfo("Auto ARIMA")
                    .currentStock(currentStock)
                    .averageDailyOutbound(averageDaily)
                    .maxDailyOutbound(maxDaily)
                    .minDailyOutbound(minDaily)
                    .build();

        } catch (Exception e) {
            log.error("Error forecasting with ARIMA for product {}: {}", product.getId(), e.getMessage());
            return null;
        }
    }
}