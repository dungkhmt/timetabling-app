package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.report.DeliveryDashboardDto;
import openerp.openerpresourceserver.wms.dto.report.ImportExportReportDto;
import openerp.openerpresourceserver.wms.dto.report.PurchaseOrderReportDto;
import openerp.openerpresourceserver.wms.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/order/monthly")
    public ApiResponse<PurchaseOrderReportDto> getMonthlyPurchaseReport(@RequestParam String orderTypeId) {
        return reportService.getMonthlyPurchaseReport(orderTypeId);
    }

    @GetMapping("/inventory/monthly")
    public ApiResponse<ImportExportReportDto> getMonthlyInventoryReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // If dates not provided, use first day of current month to today
        LocalDate end = Optional.ofNullable(endDate).orElse(LocalDate.now());
        LocalDate start = Optional.ofNullable(startDate)
                .orElse(LocalDate.now().withDayOfMonth(1)); // First day of current month

        return reportService.getMonthlyInventoryReport(start, end);
    }

    @GetMapping("/inventory/facility/{facilityId}")
    public ApiResponse<ImportExportReportDto> getFacilityInventoryReport(
            @PathVariable String facilityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // If dates not provided, use first day of current month to today
        LocalDate end = Optional.ofNullable(endDate).orElse(LocalDate.now());
        LocalDate start = Optional.ofNullable(startDate)
                .orElse(LocalDate.now().withDayOfMonth(1)); // First day of current month

        return reportService.getFacilityInventoryReport(facilityId, start, end);
    }

    @GetMapping("/delivery/dashboard")
    public ApiResponse<DeliveryDashboardDto> getDeliveryDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // If dates not provided, use first day of current month to today
        LocalDate end = Optional.ofNullable(endDate).orElse(LocalDate.now());
        LocalDate start = Optional.ofNullable(startDate)
                .orElse(LocalDate.now().withDayOfMonth(1)); // First day of current month

        return reportService.getDeliveryDashboard(start, end);
    }
}
