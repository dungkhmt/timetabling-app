package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.entity.Vehicle;
import openerp.openerpresourceserver.wms.service.VehicleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vehicle")
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping("/get-all")
    public ApiResponse<Pagination<Vehicle>> getVehicles(@RequestParam Integer page, @RequestParam Integer limit, @RequestParam String statusId) {
        return vehicleService.getVehicles(page, limit, statusId);
    }

}
