package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;

public interface InvoiceService {
    ApiResponse<Void> exportShipment(String shipmentId, String name);
}
