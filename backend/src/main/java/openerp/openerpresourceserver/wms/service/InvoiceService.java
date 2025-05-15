package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;

public interface InvoiceService {
    ApiResponse<Void> exportOutBound(String shipmentId, String name);

    ApiResponse<Void> importInBound(String shipmentId, String name);
}
