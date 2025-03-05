package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.entity.Facility;
import openerp.openerpresourceserver.wms.service.FacilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/facility")
@RestController
public class FacilityController {
    private final FacilityService facilityService;

    @GetMapping("/get-more")
    public ApiResponse<Pagination<Facility>> getFacilities(@RequestParam Integer page, @RequestParam Integer limit) {
        return facilityService.getFacilities(page, limit);
    }
}
