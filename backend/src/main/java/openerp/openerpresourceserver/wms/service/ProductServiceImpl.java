package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.ProductGetListFilter;
import openerp.openerpresourceserver.wms.dto.product.CreateProductReq;
import openerp.openerpresourceserver.wms.dto.product.ProductGetListRes;
import openerp.openerpresourceserver.wms.entity.Product;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.ProductCategoryRepo;
import openerp.openerpresourceserver.wms.repository.ProductRepo;
import openerp.openerpresourceserver.wms.repository.specification.ProductSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepo productRepo;
    private final ProductCategoryRepo productCategoryRepo;
    private final GeneralMapper generalMapper;
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

    @Override
    public ApiResponse<Void> createProduct(CreateProductReq req) {
        var newProduct = generalMapper.convertToEntity(req, Product.class);
        if(Objects.nonNull(req.getProductCategoryId())) {
            var productCategory = productCategoryRepo.findById(req.getProductCategoryId()).orElseThrow(() -> new DataNotFoundException("Product category with id " + req.getProductCategoryId() + " not found"));
            newProduct.setCategory(productCategory);
        }

        productRepo.save(newProduct);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Product created successfully")
                .build();
    }

    @Override
    public ApiResponse<Pagination<ProductGetListRes>> getProducts(int page, int limit, ProductGetListFilter filters) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var productSpec = new ProductSpecification(filters);
        var productPage = productRepo.findAll(productSpec, pageReq);
        var productList = productPage.getContent().stream()
                .map(product -> generalMapper.convertToDto(product, ProductGetListRes.class))
                .toList();
        var pagination = Pagination.<ProductGetListRes>builder()
                .data(productList)
                .page(page)
                .size(limit)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .build();

        return ApiResponse.<Pagination<ProductGetListRes>>builder()
                .code(200)
                .message("Get product list successfully")
                .data(pagination)
                .build();
    }
}
