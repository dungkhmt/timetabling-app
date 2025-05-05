package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.entity.Shipper;
import openerp.openerpresourceserver.wms.service.ShipperService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shipper")
public class ShipperController {
    private final ShipperService shipperService;

    @GetMapping("/get-all")
    public ApiResponse<List<Shipper>> getAll(@RequestParam String statusId) {
        return shipperService.getAll(statusId);
    }
}
