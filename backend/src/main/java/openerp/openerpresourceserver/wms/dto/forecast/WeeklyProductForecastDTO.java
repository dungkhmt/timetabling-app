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
public class WeeklyProductForecastDTO {
    private String productId;
    private String productName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate forecastDate;
    
    private Integer totalPredictedQuantity; // Tổng dự báo cho 4 tuần tới
    private Integer averageWeeklyQuantity; // Trung bình mỗi tuần
    private BigDecimal price;
    private String unit;
    private BigDecimal tax;
    
    // Historical data for the last 12-24 weeks
    private Map<String, Integer> historicalWeeklyData; // "2024-W01" -> quantity
    
    // Forecast data for the next 4 weeks
    private Map<String, Integer> weeklyForecastData; // "2024-W13" -> predicted quantity
    private Map<String, Integer> weeklyUpperBounds;  // "2024-W13" -> upper bound
    private Map<String, Integer> weeklyLowerBounds;  // "2024-W13" -> lower bound
    
    // ARIMA model parameters and statistics
    private Double confidenceLevel;
    private String modelInfo;
    private Double mse; // Mean Squared Error
    private Double rmse; // Root Mean Squared Error
    private BigDecimal trend; // Xu hướng tăng/giảm (%)
    
    // Current inventory status
    private Integer currentStock;
    private Integer averageWeeklyOutbound;
    private Integer maxWeeklyOutbound;
    private Integer minWeeklyOutbound;
    private Integer weeksUntilStockout; // Số tuần có thể hết hàng
    
    // Confidence intervals
    private Integer upperBoundTotal;
    private Integer lowerBoundTotal;
}