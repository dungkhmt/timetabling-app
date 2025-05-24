package openerp.openerpresourceserver.wms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDashboardDto {
    private int totalDeliveryBills;
    private int totalDeliveryPlans;
    private int totalDeliveryRoutes;
    
    private Map<String, Integer> billStatusCounts;
    private Map<String, Integer> planStatusCounts;
    private Map<String, Integer> routeStatusCounts;
    
    private BigDecimal totalDeliveryWeight;
    private List<DailyDeliveryCountDto> dailyDeliveryCounts;
    private List<TopCustomerDto> topCustomers;
    private List<ShipperPerformanceDto> shipperPerformances;
}

