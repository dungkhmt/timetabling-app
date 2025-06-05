package openerp.openerpresourceserver.wms.service;

import jakarta.validation.Valid;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.facility.CreateFacilityReq;
import openerp.openerpresourceserver.wms.dto.facility.FacilityGetListRes;
import openerp.openerpresourceserver.wms.dto.filter.FacilityGetListFilter;
import openerp.openerpresourceserver.wms.entity.Facility;

public interface FacilityService {
    ApiResponse<Pagination<Facility>> getFacilities(java.lang.Integer page, java.lang.Integer limit);

    ApiResponse<Void> createFacility(@Valid CreateFacilityReq req);

    ApiResponse<Pagination<FacilityGetListRes>> getFacilities(Integer page, Integer limit, FacilityGetListFilter filters);

    ApiResponse<Facility> getFacilityById(String id);
}
