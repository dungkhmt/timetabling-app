package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.entity.Shipper;

import java.util.List;

public interface ShipperService {
    ApiResponse<List<Shipper>> getAll(String statusId);
}
