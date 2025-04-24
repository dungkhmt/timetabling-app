package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryBill;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryBillService {
    @Override
    public ApiResponse<Void> createDeliveryBill(CreateDeliveryBill req) {
        return null;
    }
}
