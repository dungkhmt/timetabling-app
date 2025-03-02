package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.CreateSaleOrderReq;
import org.springframework.stereotype.Service;

@Service
public class SaleOrderServiceImpl implements SaleOrderService{

    @Override
    public ApiResponse<Void> createSaleOrder(CreateSaleOrderReq saleOrder) {
        return null;
    }
}
