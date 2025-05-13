package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.SupplierGetListFilter;
import openerp.openerpresourceserver.wms.dto.supplier.CreateSupplierReq;
import openerp.openerpresourceserver.wms.dto.supplier.SupplierListRes;
import openerp.openerpresourceserver.wms.entity.Supplier;

public interface SupplierService {
    ApiResponse<Pagination<SupplierListRes>> getSuppliers(int page, int limit);

    ApiResponse<Void> createSupplier(CreateSupplierReq supplier);

    ApiResponse<Pagination<Supplier>> getSuppliers(int page, int limit, SupplierGetListFilter filters);
}
