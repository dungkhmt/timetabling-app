package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.report.PurchaseOrderReportDto;

public interface ReportService {
    ApiResponse<PurchaseOrderReportDto> getMonthlyPurchaseReport(String orderTypeId);
}
