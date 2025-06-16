package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.invoice.InvoiceDTO;

public interface InvoiceService {
    ApiResponse<Void> exportOutBound(String shipmentId, String name);

    ApiResponse<Void> importInBound(String shipmentId, String name);
    
    ApiResponse<InvoiceDTO> getInvoiceByShipmentId(String shipmentId);
    ApiResponse<InvoiceDTO> getInvoiceById(String invoiceId);
}
