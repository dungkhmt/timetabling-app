package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryBill;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryListPageRes;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryBillGetListFilter;
import openerp.openerpresourceserver.wms.entity.Shipment;
import openerp.openerpresourceserver.wms.entity.UserLogin;

import java.security.Principal;

public interface DeliveryBillService {
    ApiResponse<Void> createDeliveryBill(CreateDeliveryBill req, Principal principal);

    ApiResponse<Pagination<DeliveryListPageRes>> getDeliveryBills(int page, int limit, DeliveryBillGetListFilter filters);

    void simulateDeliveryBill(Shipment shipment, UserLogin userLogin);
}
