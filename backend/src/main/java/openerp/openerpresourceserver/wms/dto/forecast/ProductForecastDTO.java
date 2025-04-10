package openerp.openerpresourceserver.wms.dto.forecast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductForecastDTO {
    private String productId;
    private String productName;
    private int predictedQuantity;
    private BigDecimal predictedValue;
}
