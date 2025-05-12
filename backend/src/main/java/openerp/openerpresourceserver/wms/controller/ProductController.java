package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.ProductGetListFilter;
import openerp.openerpresourceserver.wms.dto.product.CreateProductReq;
import openerp.openerpresourceserver.wms.dto.product.ProductGetListRes;
import openerp.openerpresourceserver.wms.entity.Product;
import openerp.openerpresourceserver.wms.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/get-more")
    public ApiResponse<Pagination<Product>> getProducts(@RequestParam Integer page, @RequestParam Integer limit) {
        return productService.getProducts(page, limit);
    }

    @GetMapping("/search")
    public ApiResponse<Pagination<Product>> searchProducts(@RequestParam String query, @RequestParam Integer page, @RequestParam Integer limit) {
        return productService.searchProducts(query, page, limit);
    }

    @PostMapping("/create")
    public ApiResponse<Void> createProduct(@RequestBody CreateProductReq req) {
        return productService.createProduct(req);
    }

    @PostMapping("/get-all")
    public ApiResponse<Pagination<ProductGetListRes>> getProducts(@RequestParam int page, @RequestParam int limit, @RequestBody
    ProductGetListFilter filters) {
        return productService.getProducts(page, limit, filters);
    }
}
