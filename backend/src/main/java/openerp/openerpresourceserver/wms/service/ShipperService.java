package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.ShipperGetListFilter;
import openerp.openerpresourceserver.wms.entity.Shipper;

public interface ShipperService {
    ApiResponse<Pagination<Shipper>> getAll(int page, int limit, ShipperGetListFilter filters);
}
