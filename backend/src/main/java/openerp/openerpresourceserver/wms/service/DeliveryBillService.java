package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryBill;

public interface DeliveryBillService {
    ApiResponse<Void> createDeliveryBill(CreateDeliveryBill req);
}
