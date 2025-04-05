package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;

public interface InvoiceService {
    ApiResponse<Void> exportOunBound(String shipmentId, String name);

    ApiResponse<Void> exportInBound(String shipmentId, String name);
}
