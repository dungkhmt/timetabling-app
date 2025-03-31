package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.supplier.SupplierListRes;

public interface SupplierService {
    ApiResponse<Pagination<SupplierListRes>> getSuppliers(int page, int limit);
}
