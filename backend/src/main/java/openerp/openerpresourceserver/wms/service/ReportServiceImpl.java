package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.report.*;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import openerp.openerpresourceserver.wms.entity.OrderItem;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final OrderHeaderRepo orderHeaderRepo;
    private final OrderItemRepo orderItemRepo;
    private final OrderItemBillingRepo orderItemBillingRepo;
    private final DeliveryBillRepo deliveryBillRepo;
    private final DeliveryPlanRepo deliveryPlanRepo;
    private final DeliveryRouteRepo deliveryRouteRepo;
    
    @Override
    public ApiResponse<PurchaseOrderReportDto> getMonthlyPurchaseReport(String orderTypeId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDateTime startDateTime = LocalDateTime.of(startOfMonth, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.now();

        // Get all purchase orders from first day of month until now
        List<OrderHeader> purchaseOrders = orderHeaderRepo.findAllByCreatedStampBetweenAndOrderTypeId(startDateTime, endDateTime, orderTypeId);

        int totalOrders = purchaseOrders.size();
        int totalApprovedOrders = 0;
        int totalCanceledOrders = 0;
        int totalWaitingOrders = 0;
        List<String> approvedOrderIds = new ArrayList<>();
        Map<LocalDate, Integer> dailyCountMap = new java.util.HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Chỉ 1 vòng lặp để tính hết các số liệu
        for (OrderHeader order : purchaseOrders) {
            String status = order.getStatusId();
            LocalDate date = order.getCreatedStamp().toLocalDate();

            dailyCountMap.put(date, dailyCountMap.getOrDefault(date, 0) + 1);

            if (OrderStatus.APPROVED.name().equals(status)) {
                totalApprovedOrders++;
                approvedOrderIds.add(order.getId());
            } else if (OrderStatus.CANCELLED.name().equals(status)) {
                totalCanceledOrders++;
            } else if (OrderStatus.CREATED.name().equals(status)) {
                totalWaitingOrders++;
            }
        }

        // Tính profit chỉ 1 lần sau khi đã gom được approvedOrderIds
        BigDecimal totalProfit = BigDecimal.ZERO;
        if (!approvedOrderIds.isEmpty()) {
            List<OrderItem> orderItems = orderItemRepo.findAllByOrderIdIn(approvedOrderIds);
            for (OrderItem item : orderItems) {
                totalProfit = totalProfit.add(item.getAmount());
            }
        }

        // Chuẩn hóa daily counts từ map thành list theo thứ tự ngày
        List<DailyOrderCountDto> dailyOrderCounts = new ArrayList<>();
        LocalDate currentDate = startOfMonth;
        LocalDate today = LocalDate.now();
        while (!currentDate.isAfter(today)) {
            int count = dailyCountMap.getOrDefault(currentDate, 0);
            dailyOrderCounts.add(new DailyOrderCountDto(
                    currentDate,
                    count,
                    currentDate.format(formatter)
            ));
            currentDate = currentDate.plusDays(1);
        }

        // Build DTO
        PurchaseOrderReportDto reportDto = PurchaseOrderReportDto.builder()
                .totalOrders(totalOrders)
                .totalApprovedOrders(totalApprovedOrders)
                .totalWaitingOrders(totalWaitingOrders)
                .totalCanceledOrders(totalCanceledOrders)
                .totalProfit(totalProfit)
                .dailyOrderCounts(dailyOrderCounts)
                .build();

        return ApiResponse.<PurchaseOrderReportDto>builder()
                .code(200)
                .message("Purchase order report generated successfully")
                .data(reportDto)
                .build();
    }

    @Override
    public ApiResponse<ImportExportReportDto> getMonthlyInventoryReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // Include the end date fully

        // Query raw inventory movement data - no filtering by sign, let the DB return all data
        List<Object[]> movementData = orderItemBillingRepo.getInventoryMovements(startDateTime, endDateTime);

        // Create the report data
        ImportExportReportDto reportDto = processMovementData(movementData, startDate, endDate);

        // Add facility summary data
        List<Object[]> facilitySummaryData = orderItemBillingRepo.getFacilitySummary(startDateTime, endDateTime);
        List<FacilityMovementDto> facilityMovements = processFacilityData(facilitySummaryData);
        reportDto.setFacilityMovements(facilityMovements);

        return ApiResponse.<ImportExportReportDto>builder()
                .code(200)
                .message("Inventory report generated successfully")
                .data(reportDto)
                .build();
    }

    @Override
    public ApiResponse<ImportExportReportDto> getFacilityInventoryReport(
            String facilityId, LocalDate startDate, LocalDate endDate) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // Include the end date fully

        // Query movement data for the specific facility - no filtering by sign
        List<Object[]> movementData = orderItemBillingRepo.getFacilityMovements(
                facilityId, startDateTime, endDateTime);

        // Process the data
        ImportExportReportDto reportDto = processMovementData(movementData, startDate, endDate);

        return ApiResponse.<ImportExportReportDto>builder()
                .code(200)
                .message("Facility inventory report generated successfully")
                .data(reportDto)
                .build();
    }

    // Shared helper method to process movement data
    private ImportExportReportDto processMovementData(
            List<Object[]> movementData, LocalDate startDate, LocalDate endDate) {

        // Initialize daily data map for all dates in range
        Map<LocalDate, DailyMovementDto> dailyMap = initializeDailyMap(startDate, endDate);

        // Process movement data, split by sign
        int totalImport = 0;
        int totalExport = 0;
        Map<String, Integer> productImports = new HashMap<>();
        Map<String, String> productNames = new HashMap<>();
        Map<String, Integer> productExports = new HashMap<>();

        // Process all movements
        for (Object[] row : movementData) {
            LocalDate date = ((LocalDateTime) row[0]).toLocalDate();
            int quantity = ((Number) row[1]).intValue();
            String productId = (String) row[2];
            String productName = (String) row[3];

            // Store product name for later use
            productNames.put(productId, productName);

            // Update daily movement records
            DailyMovementDto daily = dailyMap.getOrDefault(date,
                    new DailyMovementDto(date.format(DateTimeFormatter.ISO_LOCAL_DATE), 0, 0));

            if (quantity > 0) { // Import
                daily.setImportQuantity(daily.getImportQuantity() + quantity);
                totalImport += quantity;

                // Update product import count
                productImports.put(productId, productImports.getOrDefault(productId, 0) + quantity);
            } else if (quantity < 0) { // Export
                int absQuantity = Math.abs(quantity);
                daily.setExportQuantity(daily.getExportQuantity() + absQuantity);
                totalExport += absQuantity;

                // Update product export count
                productExports.put(productId, productExports.getOrDefault(productId, 0) + absQuantity);
            }

            dailyMap.put(date, daily);
        }

        // Convert to sorted daily movement list
        List<DailyMovementDto> dailyMovements = new ArrayList<>(dailyMap.values());
        dailyMovements.sort(Comparator.comparing(d -> LocalDate.parse(d.getDate())));

        // Get top imported products
        List<ProductMovementDto> topImports = productImports.entrySet().stream()
                .map(entry -> new ProductMovementDto(
                        entry.getKey(),
                        productNames.getOrDefault(entry.getKey(), "Unknown"),
                        entry.getValue()))
                .sorted(Comparator.comparing(ProductMovementDto::getQuantity).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Get top exported products
        List<ProductMovementDto> topExports = productExports.entrySet().stream()
                .map(entry -> new ProductMovementDto(
                        entry.getKey(),
                        productNames.getOrDefault(entry.getKey(), "Unknown"),
                        entry.getValue()))
                .sorted(Comparator.comparing(ProductMovementDto::getQuantity).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Build the final report
        return ImportExportReportDto.builder()
                .dailyMovements(dailyMovements)
                .totalImportQuantity(totalImport)
                .totalExportQuantity(totalExport)
                .topImportedProducts(topImports)
                .topExportedProducts(topExports)
                .build();
    }

    // Process facility summary data
    private List<FacilityMovementDto> processFacilityData(List<Object[]> facilitySummaryData) {
        Map<String, FacilityMovementDto> facilityMap = new HashMap<>();

        for (Object[] row : facilitySummaryData) {
            String facilityId = (String) row[0];
            String facilityName = (String) row[1];
            int quantity = ((Number) row[2]).intValue();

            // Get or create facility record
            FacilityMovementDto dto = facilityMap.getOrDefault(facilityId,
                    new FacilityMovementDto(facilityId, facilityName, 0, 0));

            if (quantity > 0) { // Import
                dto.setImportQuantity(dto.getImportQuantity() + quantity);
            } else if (quantity < 0) { // Export
                dto.setExportQuantity(dto.getExportQuantity() + Math.abs(quantity));
            }

            facilityMap.put(facilityId, dto);
        }

        // Sort by total movement in descending order
        return facilityMap.values().stream()
                .sorted(Comparator.comparing(
                        f -> -(f.getImportQuantity() + f.getExportQuantity())))
                .collect(Collectors.toList());
    }

    // Initialize daily map with all dates in the range
    private Map<LocalDate, DailyMovementDto> initializeDailyMap(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        return startDate.datesUntil(endDate.plusDays(1))
                .collect(Collectors.toMap(
                        date -> date,
                        date -> new DailyMovementDto(date.format(formatter), 0, 0)
                ));
    }

    @Override
    public ApiResponse<DeliveryDashboardDto> getDeliveryDashboard(LocalDate startDate, LocalDate endDate) {
        // Convert dates to LocalDateTime for repository queries
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // 1. Get total counts
        int totalDeliveryBills = deliveryBillRepo.countByCreatedStampBetween(startDateTime, endDateTime);
        int totalDeliveryPlans = deliveryPlanRepo.countByCreatedStampBetween(startDateTime, endDateTime);
        int totalDeliveryRoutes = deliveryRouteRepo.countByCreatedStampBetween(startDateTime, endDateTime);
        
        // 2. Get total delivery weight
        BigDecimal totalWeight = deliveryPlanRepo.sumTotalWeightBetween(startDateTime, endDateTime);
        if (totalWeight == null) {
            totalWeight = BigDecimal.ZERO;
        }
        
        // 3. Get status counts as maps
        Map<String, Integer> billStatusCounts = new HashMap<>();
        deliveryBillRepo.countByStatusBetween(startDateTime, endDateTime).forEach(row -> 
            billStatusCounts.put((String) row[0], ((Number) row[1]).intValue())
        );
        
        Map<String, Integer> planStatusCounts = new HashMap<>();
        deliveryPlanRepo.countByStatusBetween(startDateTime, endDateTime).forEach(row -> 
            planStatusCounts.put((String) row[0], ((Number) row[1]).intValue())
        );
        
        Map<String, Integer> routeStatusCounts = new HashMap<>();
        deliveryRouteRepo.countByStatusBetween(startDateTime, endDateTime).forEach(row -> 
            routeStatusCounts.put((String) row[0], ((Number) row[1]).intValue())
        );
        
        // 4. Get daily counts and merge them
        Map<LocalDate, DailyDeliveryCountDto> dailyMap = initializeDailyDeliveryMap(startDate, endDate);
        
        // Process bill daily counts
        deliveryBillRepo.countDailyBetween(startDateTime, endDateTime).forEach(row -> {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            int count = ((Number) row[1]).intValue();
            
            DailyDeliveryCountDto daily = dailyMap.get(date);
            if (daily != null) {
                daily.setBillsCount(count);
            }
        });
        
        // Process plan daily counts
        deliveryPlanRepo.countDailyBetween(startDateTime, endDateTime).forEach(row -> {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            int count = ((Number) row[1]).intValue();
            
            DailyDeliveryCountDto daily = dailyMap.get(date);
            if (daily != null) {
                daily.setPlansCount(count);
            }
        });
        
        // Process route daily counts
        deliveryRouteRepo.countDailyBetween(startDateTime, endDateTime).forEach(row -> {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            int count = ((Number) row[1]).intValue();
            
            DailyDeliveryCountDto daily = dailyMap.get(date);
            if (daily != null) {
                daily.setRoutesCount(count);
            }
        });
        
        // Convert map to sorted list
        List<DailyDeliveryCountDto> dailyDeliveryCounts = new ArrayList<>(dailyMap.values());
        dailyDeliveryCounts.sort(Comparator.comparing(DailyDeliveryCountDto::getDate));
        
        // 5. Get top customers (limit to 5)
        List<TopCustomerDto> topCustomers = new ArrayList<>();
        deliveryBillRepo.findTopCustomersBetween(startDateTime, endDateTime, CommonUtil.getPageRequest(0,5)).forEach(row -> {
            String customerId = (String) row[0];
            String customerName = (String) row[1];
            int count = ((Number) row[2]).intValue();
            BigDecimal weight = (BigDecimal) row[3];
            
            if (weight == null) {
                weight = BigDecimal.ZERO;
            }
            
            topCustomers.add(new TopCustomerDto(customerId, customerName, count, weight));
        });
        
        // 6. Get shipper performance
        List<ShipperPerformanceDto> shipperPerformances = new ArrayList<>();
        deliveryRouteRepo.findShipperPerformanceBetween(startDateTime, endDateTime).forEach(row -> {
            String shipperId = (String) row[0];
            String shipperName = (String) row[1] + (String) row[2];
            int assignedRoutes = ((Number) row[3]).intValue();
            int completedRoutes = ((Number) row[4]).intValue();
            int inProgressRoutes = ((Number) row[5]).intValue();
            
            shipperPerformances.add(new ShipperPerformanceDto(
                shipperId, shipperName, assignedRoutes, completedRoutes, inProgressRoutes
            ));
        });
        
        // Build the dashboard DTO
        DeliveryDashboardDto dashboardDto = DeliveryDashboardDto.builder()
                .totalDeliveryBills(totalDeliveryBills)
                .totalDeliveryPlans(totalDeliveryPlans)
                .totalDeliveryRoutes(totalDeliveryRoutes)
                .billStatusCounts(billStatusCounts)
                .planStatusCounts(planStatusCounts)
                .routeStatusCounts(routeStatusCounts)
                .totalDeliveryWeight(totalWeight)
                .dailyDeliveryCounts(dailyDeliveryCounts)
                .topCustomers(topCustomers)
                .shipperPerformances(shipperPerformances)
                .build();
        
        return ApiResponse.<DeliveryDashboardDto>builder()
                .code(200)
                .message("Delivery dashboard data retrieved successfully")
                .data(dashboardDto)
                .build();
    }
    
    // Helper method to initialize daily map
    private Map<LocalDate, DailyDeliveryCountDto> initializeDailyDeliveryMap(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        Map<LocalDate, DailyDeliveryCountDto> map = new HashMap<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            DailyDeliveryCountDto dto = new DailyDeliveryCountDto(
                currentDate,
                currentDate.format(formatter),
                0, 0, 0
            );
            map.put(currentDate, dto);
            currentDate = currentDate.plusDays(1);
        }
        
        return map;
    }
}
