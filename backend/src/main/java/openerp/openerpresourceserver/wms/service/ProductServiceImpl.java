package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.entity.Product;
import openerp.openerpresourceserver.wms.repository.ProductRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepo productRepo;
    @Override
    public ApiResponse<Pagination<Product>> getProducts(Integer page, Integer limit) {
        PageRequest pageRequest = PageRequest.of(page, limit);
        var pages = productRepo.findAll(pageRequest);
        return ApiResponse.<Pagination<Product>>builder()
                .code(200)
                .message("Success")
                .data(Pagination.<Product>builder()
                        .data(pages.getContent())
                        .page(pages.getNumber())
                        .size(pages.getSize())
                        .totalElements(pages.getTotalElements())
                        .totalPages(pages.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public ApiResponse<Pagination<Product>> searchProducts(String query, Integer page, Integer limit) {
        PageRequest pageRequest = PageRequest.of(page, limit);
        var pages = productRepo.findByNameContaining(query, pageRequest);
        return ApiResponse.<Pagination<Product>>builder()
                .code(200)
                .message("Success")
                .data(Pagination.<Product>builder()
                        .data(pages.getContent())
                        .page(pages.getNumber())
                        .size(pages.getSize())
                        .totalElements(pages.getTotalElements())
                        .totalPages(pages.getTotalPages())
                        .build())
                .build();
    }
}
