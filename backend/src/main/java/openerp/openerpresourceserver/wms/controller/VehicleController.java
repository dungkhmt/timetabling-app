package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.VehicleGetListFilter;
import openerp.openerpresourceserver.wms.entity.Vehicle;
import openerp.openerpresourceserver.wms.service.VehicleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vehicle")
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping("/get-all")
    public ApiResponse<Pagination<Vehicle>> getVehicles(@RequestParam Integer page, @RequestParam Integer limit, @RequestBody VehicleGetListFilter filters) {
        return vehicleService.getVehicles(page, limit, filters);
    }

}
