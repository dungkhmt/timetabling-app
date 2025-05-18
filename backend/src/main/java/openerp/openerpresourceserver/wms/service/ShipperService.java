package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.ShipperGetListFilter;
import openerp.openerpresourceserver.wms.dto.shipper.ShipperGetListRes;

public interface ShipperService {
    ApiResponse<Pagination<ShipperGetListRes>> getAll(int page, int limit, ShipperGetListFilter filters);
}
