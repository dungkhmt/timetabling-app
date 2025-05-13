package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.SupplierGetListFilter;
import openerp.openerpresourceserver.wms.dto.supplier.CreateSupplierReq;
import openerp.openerpresourceserver.wms.dto.supplier.SupplierListRes;
import openerp.openerpresourceserver.wms.entity.Supplier;
import openerp.openerpresourceserver.wms.service.SupplierService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/supplier")
public class SupplierController {
    private final SupplierService supplierService;

     @GetMapping("/get-more")
    public ApiResponse<Pagination<SupplierListRes>> getSuppliers(@RequestParam int page, @RequestParam int limit) {
        return supplierService.getSuppliers(page, limit);
    }

    @PostMapping("/create")
    public ApiResponse<Void> createSupplier(@RequestBody CreateSupplierReq supplier) {
        return supplierService.createSupplier(supplier);
    }

    @PostMapping("get-all")
    public ApiResponse<Pagination<Supplier>> getSuppliers (@RequestParam int page, @RequestParam int limit,
                                                           @RequestBody SupplierGetListFilter filters) {
        return supplierService.getSuppliers(page, limit, filters);
    }

    @GetMapping("/details/{id}")
    public ApiResponse<Supplier> getSupplierDetails(@PathVariable String id) {
        return supplierService.getSupplierById(id);
    }
}
