package openerp.openerpresourceserver.wms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.product.CreateProductPriceReq;
import openerp.openerpresourceserver.wms.entity.ProductPrice;
import openerp.openerpresourceserver.wms.service.ProductPriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product-price")
public class ProductPriceController {
    private final ProductPriceService service;

    @PostMapping
    public ApiResponse<Void> createProductPrice(@RequestBody @Valid CreateProductPriceReq req) {
        return service.createProductPrice(req);
    }

    @GetMapping("/{productId}")
    public ApiResponse<List<ProductPrice>> getProductPrice(@PathVariable String productId) {
        return service.getProductPrice(productId);
    }
}
