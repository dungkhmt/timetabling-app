package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.ProductGetListFilter;
import openerp.openerpresourceserver.wms.dto.product.CreateProductReq;
import openerp.openerpresourceserver.wms.dto.product.ProductGetListRes;
import openerp.openerpresourceserver.wms.entity.Product;

public interface ProductService {
    ApiResponse<Pagination<Product>> getProducts(java.lang.Integer page, java.lang.Integer limit);

    ApiResponse<Pagination<Product>> searchProducts(String query, Integer page, Integer limit);

    ApiResponse<Void> createProduct(CreateProductReq req);

    ApiResponse<Pagination<ProductGetListRes>> getProducts(int page, int limit, ProductGetListFilter filters);
}
