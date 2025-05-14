package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.report.DailyOrderCountDto;
import openerp.openerpresourceserver.wms.dto.report.PurchaseOrderReportDto;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import openerp.openerpresourceserver.wms.entity.OrderItem;
import openerp.openerpresourceserver.wms.repository.OrderHeaderRepo;
import openerp.openerpresourceserver.wms.repository.OrderItemRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService
{
    private final OrderHeaderRepo orderHeaderRepo;
    private final OrderItemRepo orderItemRepo;
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
            String status = order.getStatus();
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

}
