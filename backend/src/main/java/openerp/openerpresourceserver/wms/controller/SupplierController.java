package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.supplier.SupplierListRes;
import openerp.openerpresourceserver.wms.service.SupplierService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/supplier")
public class SupplierController {
    private final SupplierService supplierService;

     @GetMapping("/get-more")
    public ApiResponse<Pagination<SupplierListRes>> getSuppliers(@RequestParam int page, @RequestParam int limit) {
        return supplierService.getSuppliers(page, limit);
    }
}
