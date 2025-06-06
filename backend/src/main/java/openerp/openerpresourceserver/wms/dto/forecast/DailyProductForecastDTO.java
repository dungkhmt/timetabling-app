package openerp.openerpresourceserver.wms.dto.forecast;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyProductForecastDTO {
    private String productId;
    private String productName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate forecastDate;
    
    private Integer totalPredictedQuantity;
    private BigDecimal price;
    private String unit;
    private BigDecimal discount;
    private BigDecimal tax;
    
    // Historical data for the last 30 days
    private Map<String, Integer> historicalData; // date -> quantity
    
    // Forecast data for the next 7 days
    private Map<String, Integer> forecastData; // date -> predicted quantity
    
    // ARIMA model parameters and statistics
    private Double confidenceLevel;
    private String modelInfo;
    private Double mse; // Mean Squared Error
    private Double rmse; // Root Mean Squared Error
    
    // Additional statistics
    private Integer currentStock;
    private Integer averageDailyOutbound;
    private Integer maxDailyOutbound;
    private Integer minDailyOutbound;
}