package openerp.openerpresourceserver.wms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.facility.CreateFacilityReq;
import openerp.openerpresourceserver.wms.dto.facility.FacilityGetListRes;
import openerp.openerpresourceserver.wms.dto.filter.FacilityGetListFilter;
import openerp.openerpresourceserver.wms.entity.Facility;
import openerp.openerpresourceserver.wms.service.FacilityService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/facility")
@RestController
public class FacilityController {
    private final FacilityService facilityService;

    @GetMapping("/get-more")
    public ApiResponse<Pagination<Facility>> getFacilities(@RequestParam Integer page, @RequestParam Integer limit) {
        return facilityService.getFacilities(page, limit);
    }

    @PostMapping("create")
    public ApiResponse<Void> createFacility(@RequestBody @Valid CreateFacilityReq req) {
        return facilityService.createFacility(req);
    }

    @PostMapping("/get-all")
    public ApiResponse<Pagination<FacilityGetListRes>> getFacilities(@RequestParam Integer page, @RequestParam Integer limit,
                                                                     @RequestBody FacilityGetListFilter filters) {
        return facilityService.getFacilities(page, limit, filters);
    }

    @GetMapping("/details/{id}")
    public ApiResponse<Facility> getFacilityDetails(@PathVariable String id) {
        return facilityService.getFacilityById(id);
    }
}
