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
    private Integer quantity;
    private BigDecimal price;
    private String unit;
    private BigDecimal discount;
    private BigDecimal tax;
}