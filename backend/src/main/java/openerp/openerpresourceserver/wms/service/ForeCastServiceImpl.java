package openerp.openerpresourceserver.wms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.forecast.DailyProductForecastDTO;
import openerp.openerpresourceserver.wms.dto.forecast.WeeklyProductForecastDTO;
import openerp.openerpresourceserver.wms.entity.InventoryItem;
import openerp.openerpresourceserver.wms.entity.Product;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.timeseries.arima.Arima;
import openerp.openerpresourceserver.wms.timeseries.arima.struct.ForecastResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
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

    private static final int HISTORICAL_WEEKS = 104; // Last 24 weeks of historical data
    private static final int FORECAST_WEEKS = 4; // Forecast for the next 4 weeks
    private static final int MIN_WEEKS_FOR_FORECAST = 12; // Minimum weeks required for forecasting

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

    /**
     * Execute weekly forecast job automatically every Monday at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * MON")
    public void scheduledWeeklyForecast() {
        log.info("Starting scheduled weekly forecast job at {}", LocalDateTime.now());
        try {
            forecastWeeklyLowStockProducts();
            log.info("Completed scheduled weekly forecast job at {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error during scheduled weekly forecast: {}", e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<List<WeeklyProductForecastDTO>> forecastWeeklyLowStockProducts() {
        String redisKey = "WEEKLY_LOW_STOCK_FORECAST:" + getWeekIdentifier(LocalDate.now());
        List<WeeklyProductForecastDTO> cachedForecast = null;
        
        try {
            cachedForecast = (List<WeeklyProductForecastDTO>) redisService.get(redisKey);
        } catch (Exception e) {
            log.error("Error retrieving weekly forecast from Redis: {}", e.getMessage());
            cachedForecast = null;
        }
        
        if (cachedForecast != null && !cachedForecast.isEmpty()) {
            return ApiResponse.<List<WeeklyProductForecastDTO>>builder()
                    .code(200)
                    .message("Weekly low stock forecast retrieved from cache")
                    .data(cachedForecast)
                    .build();
        }

        // Get low stock products
        List<InventoryItem> lowStockItems = inventoryItemRepo.findByQuantityLessThan(LOW_STOCK_THRESHOLD);
        if (lowStockItems.isEmpty()) {
            return ApiResponse.<List<WeeklyProductForecastDTO>>builder()
                    .code(200)
                    .message("No low stock products found for weekly forecasting")
                    .data(Collections.emptyList())
                    .build();
        }

        Set<String> lowStockProductIds = lowStockItems.stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());

        List<Product> lowStockProducts = productRepo.findAllById(lowStockProductIds);
        List<WeeklyProductForecastDTO> forecasts = new ArrayList<>();

        for (Product product : lowStockProducts) {
            WeeklyProductForecastDTO forecast = forecastProductWeeklyWithArima(product);
            if (forecast != null) {
                // Add current inventory information
//                InventoryItem inventoryItem = lowStockItems.stream()
//                        .filter(item -> item.getProduct().getId().equals(product.getId()))
//                        .findFirst()
//                        .orElse(null);
                int currentStock = inventoryItemRepo.sumByProductId(product.getId());

                    forecast.setCurrentStock(currentStock);
                    // Calculate estimated weeks before stock-out
                    if (forecast.getAverageWeeklyQuantity() > 0) {
                        int weeksUntilStockout = (int) Math.floor((double) currentStock / forecast.getAverageWeeklyQuantity());
                        forecast.setWeeksUntilStockout(weeksUntilStockout);
                    }

                forecasts.add(forecast);
            }
        }

        // Sort by weeks until stock-out (ascending)
        forecasts.sort(Comparator.comparing(WeeklyProductForecastDTO::getWeeksUntilStockout, 
                                           Comparator.nullsLast(Comparator.naturalOrder())));

        // Cache the result
        redisService.save(redisKey, forecasts, REDIS_EXPIRATION_DAYS);
        
        return ApiResponse.<List<WeeklyProductForecastDTO>>builder()
                .code(200)
                .message("Weekly low stock forecast generated successfully")
                .data(forecasts)
                .build();
    }

    private WeeklyProductForecastDTO forecastProductWeeklyWithArima(Product product) {
        LocalDate today = LocalDate.now().with(DayOfWeek.MONDAY).minusDays(1); // Start from last Monday
        LocalDate historicalStartDate = today.minusWeeks(HISTORICAL_WEEKS);
        
        // Get weekly quantities for this product
        List<Object[]> weeklyData = inventoryItemDetailRepo.getWeeklyQuantitiesByProductAndShipmentType(
                product.getId(),
                ShipmentType.OUTBOUND.name(),
                historicalStartDate.atStartOfDay(),
                today.atTime(23, 59, 59));
        
        // Check if we have enough data points
        if (weeklyData.size() < MIN_WEEKS_FOR_FORECAST) {
            log.info("Not enough weekly data for product {}: {} weeks, need at least {}",
                    product.getId(), weeklyData.size(), MIN_WEEKS_FOR_FORECAST);
            return null;
        }
        
        // Convert to time series data for ARIMA
        Map<String, Integer> formattedWeeklyData = new LinkedHashMap<>();
        Map<String, Integer> rawWeeklyData = new HashMap<>();
        
        for (Object[] dataPoint : weeklyData) {
            Integer year = ((Number) dataPoint[0]).intValue();
            Integer week = ((Number) dataPoint[1]).intValue();
            Long quantity = ((Number) dataPoint[2]).longValue();
            
            String weekIdentifier = String.format("%d-W%02d", year, week);
            formattedWeeklyData.put(weekIdentifier, quantity.intValue());
            rawWeeklyData.put(weekIdentifier, quantity.intValue());
        }
        
        // Fill in any missing weeks with zeros
        LocalDate currentWeekStart = historicalStartDate;
        while (!currentWeekStart.isAfter(today)) {
            String weekId = getWeekIdentifier(currentWeekStart);
            if (!formattedWeeklyData.containsKey(weekId)) {
                formattedWeeklyData.put(weekId, 0);
                rawWeeklyData.put(weekId, 0);
            }
            currentWeekStart = currentWeekStart.plusWeeks(1);
        }
        
        // Sort by week identifier
        formattedWeeklyData = formattedWeeklyData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        
        // Convert to array for ARIMA
        double[] timeSeriesData = formattedWeeklyData.values().stream()
                .mapToDouble(Integer::doubleValue)
                .toArray();
        
        try {
            // Use ARIMA for weekly forecasting
            ForecastResult forecastResult = Arima.auto_forecast_arima(timeSeriesData, FORECAST_WEEKS);
            
            // Get the forecasted values
            double[] forecast = forecastResult.getForecast();
            double[] upperBounds = forecastResult.getForecastUpperConf();
            double[] lowerBounds = forecastResult.getForecastLowerConf();
            
            // Convert predictions to integers and build weekly forecast map
            Map<String, Integer> weeklyForecast = new LinkedHashMap<>();
            int totalPredictedQuantity = 0;
            
            for (int i = 0; i < forecast.length; i++) {
                int predictedQuantity = (int) Math.round(Math.max(0, forecast[i]));
                totalPredictedQuantity += predictedQuantity;
                
                LocalDate forecastWeekStart = today.plusWeeks(i + 1);
                String weekIdentifier = getWeekIdentifier(forecastWeekStart);
                weeklyForecast.put(weekIdentifier, predictedQuantity);
            }
            
            // Calculate the average weekly prediction
            double avgWeeklyPrediction = totalPredictedQuantity / (double) FORECAST_WEEKS;
            
            // Calculate trend percentage
            double recentAvg = 0;
            int weeksToAverage = Math.min(4, timeSeriesData.length);
            for (int i = timeSeriesData.length - weeksToAverage; i < timeSeriesData.length; i++) {
                recentAvg += timeSeriesData[i];
            }
            recentAvg /= weeksToAverage;
            
            double trendValue = 0;
            if (recentAvg > 0) {
                trendValue = ((avgWeeklyPrediction / recentAvg) - 1) * 100;
            }
            
            // Calculate statistics
            List<Integer> historicalValues = new ArrayList<>(formattedWeeklyData.values());
            int averageWeekly = (int) historicalValues.stream().mapToInt(Integer::intValue).average().orElse(0);
            int maxWeekly = historicalValues.stream().mapToInt(Integer::intValue).max().orElse(0);
            int minWeekly = historicalValues.stream().mapToInt(Integer::intValue).min().orElse(0);
            
            // Calculate predicted value
//            BigDecimal predictedValue = product.getWholeSalePrice()
//                    .multiply(BigDecimal.valueOf(totalPredictedQuantity));
            
            // Calculate upper and lower bounds
            int upperBoundTotal = 0;
            int lowerBoundTotal = 0;
            for (int i = 0; i < upperBounds.length; i++) {
                upperBoundTotal += (int) Math.round(Math.max(0, upperBounds[i]));
                lowerBoundTotal += (int) Math.round(Math.max(0, lowerBounds[i]));
            }

            //get 24 weeks of latest data
            var historicalData24Weeks = formattedWeeklyData.entrySet().stream()
                    .filter(entry -> entry.getKey().compareTo(getWeekIdentifier(today.minusWeeks(24))) >= 0)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            
            return WeeklyProductForecastDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .forecastDate(today)
                    .totalPredictedQuantity(totalPredictedQuantity)
                    .averageWeeklyQuantity((int) Math.round(avgWeeklyPrediction))
                    .price(product.getWholeSalePrice())
//                    .predictedValue(predictedValue)
                    .unit(product.getUnit())
                    .tax(product.getVatRate())
                    .historicalWeeklyData(historicalData24Weeks)
                    .weeklyForecastData(weeklyForecast)
                    .confidenceLevel(calculateConfidenceLevel(forecast, upperBounds, lowerBounds))
                    .modelInfo("ARIMA (Weekly)")
                    .rmse(forecastResult.getRMSE())
                    .trend(BigDecimal.valueOf(trendValue).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .averageWeeklyOutbound(averageWeekly)
                    .maxWeeklyOutbound(maxWeekly)
                    .minWeeklyOutbound(minWeekly)
                    .upperBoundTotal(upperBoundTotal)
                    .lowerBoundTotal(lowerBoundTotal)
                    .build();
            
        } catch (Exception e) {
            log.error("Error forecasting weekly with ARIMA for product {}: {}", product.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Generate week identifier in format "YYYY-WXX"
     */
    private String getWeekIdentifier(LocalDate date) {
        int year = date.getYear();
        int weekOfYear = date.get(java.time.temporal.WeekFields.ISO.weekOfYear());
        return String.format("%d-W%02d", year, weekOfYear);
    }

    private Double calculateConfidenceLevel(double[] forecast, double[] upperBounds, double[] lowerBounds) {
        if (forecast.length == 0 || upperBounds.length == 0 || lowerBounds.length == 0) {
            return 75.0; // Default confidence for weekly data
        }
        
        double totalRelativePrecision = 0;
        int validWeeks = 0;
        
        for (int i = 0; i < forecast.length; i++) {
            if (forecast[i] > 0) {
                double range = upperBounds[i] - lowerBounds[i];
                double relativePrecision = range / forecast[i];
                totalRelativePrecision += relativePrecision;
                validWeeks++;
            }
        }
        
        double avgRelativePrecision = validWeeks > 0 ? totalRelativePrecision / validWeeks : 1.0;
        
        // Transform to confidence value (60-95% for weekly data)
        double confidence = 95.0 - (avgRelativePrecision * 25.0);
        confidence = Math.max(Math.min(confidence, 95.0), 60.0);
        
        return confidence;
    }
}