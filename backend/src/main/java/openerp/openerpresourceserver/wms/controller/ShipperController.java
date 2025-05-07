package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.ShipperGetListFilter;
import openerp.openerpresourceserver.wms.entity.Shipper;
import openerp.openerpresourceserver.wms.service.ShipperService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shipper")
public class ShipperController {
    private final ShipperService shipperService;

    @PostMapping("/get-all")
    public ApiResponse<Pagination<Shipper>> getAll(@RequestParam int page, @RequestParam int limit, @RequestBody ShipperGetListFilter filters) {
        return shipperService.getAll(page, limit, filters);
    }
}
