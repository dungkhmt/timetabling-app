package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.entity.ProductCategory;
import openerp.openerpresourceserver.wms.repository.ProductCategoryRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService{
    private final ProductCategoryRepo productCategoryRepo;

    @Override
    public ApiResponse<List<ProductCategory>> getProductCategories() {
        return ApiResponse.<List<ProductCategory>>builder()
                .code(200)
                .message("Success")
                .data(productCategoryRepo.findAll())
                .build();
    }
}
