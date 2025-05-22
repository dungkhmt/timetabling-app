package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.VehicleGetListFilter;
import openerp.openerpresourceserver.wms.entity.Vehicle;

public interface VehicleService {
    ApiResponse<Pagination<Vehicle>> getVehicles(Integer page, Integer limit, VehicleGetListFilter statusId);
}
