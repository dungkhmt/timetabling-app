package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.entity.Product;
import openerp.openerpresourceserver.wms.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
