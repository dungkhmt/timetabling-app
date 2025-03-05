package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.entity.Facility;

public interface FacilityService {
    ApiResponse<Pagination<Facility>> getFacilities(java.lang.Integer page, java.lang.Integer limit);
}
