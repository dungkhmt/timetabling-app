package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.report.ImportExportReportDto;
import openerp.openerpresourceserver.wms.dto.report.PurchaseOrderReportDto;

import java.time.LocalDate;

public interface ReportService {
    ApiResponse<PurchaseOrderReportDto> getMonthlyPurchaseReport(String orderTypeId);

    ApiResponse<ImportExportReportDto> getMonthlyInventoryReport(LocalDate start, LocalDate end);

    ApiResponse<ImportExportReportDto> getFacilityInventoryReport(String facilityId, LocalDate start, LocalDate end);
}
