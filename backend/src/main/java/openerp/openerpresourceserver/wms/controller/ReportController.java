package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.report.PurchaseOrderReportDto;
import openerp.openerpresourceserver.wms.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/order/monthly")
    public ApiResponse<PurchaseOrderReportDto> getMonthlyPurchaseReport(@RequestParam String orderTypeId) {
        return reportService.getMonthlyPurchaseReport(orderTypeId);
    }

}
