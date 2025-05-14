package openerp.openerpresourceserver.wms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderReportDto {
    private int totalOrders;
    private int totalApprovedOrders;
    private int totalCanceledOrders;
    private int totalWaitingOrders;
    private BigDecimal totalProfit;
    private List<DailyOrderCountDto> dailyOrderCounts;
}
