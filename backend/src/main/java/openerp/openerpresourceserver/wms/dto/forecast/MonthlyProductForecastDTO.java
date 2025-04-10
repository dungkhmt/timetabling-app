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
public class MonthlyProductForecastDTO {
    private String productId;
    private String productName;
    private int predictedQuantity;
    private BigDecimal predictedValue;
    private int currentInventory;
    private int daysUntilStockout;
    private Map<String, Integer> historicalData;
    private BigDecimal trend;
    private BigDecimal confidenceLevel;
    private int upperBoundValue;
    private int lowerBoundValue;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate forecastDate;

    private int nextMonthPrediction;
    private int twoMonthPrediction;
    private int threeMonthPrediction;
}