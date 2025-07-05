package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;
import openerp.openerpresourceserver.wms.constant.enumrator.ProductPriceStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.product.CreateProductPriceReq;
import openerp.openerpresourceserver.wms.entity.Product;
import openerp.openerpresourceserver.wms.entity.ProductPrice;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.ProductPriceRepo;
import openerp.openerpresourceserver.wms.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static openerp.openerpresourceserver.wms.constant.Constants.PRODUCT_PRICE_ID_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPriceServiceImpl implements ProductPriceService {
    private final ProductPriceRepo productPriceRepo;
    private final ProductRepo productRepo;
    private final GeneralMapper generalMapper;

    @Override
    @Transactional
    public ApiResponse<Void> createProductPrice(CreateProductPriceReq req) {
        // check if product exists
        var productId = req.getProductId();
        var startDate = req.getStartDate();
        var endDate = req.getEndDate();
        var now = LocalDateTime.now();

        if(Objects.nonNull(endDate) && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        if(Objects.nonNull(endDate) && endDate.isBefore(now)) {
            throw new IllegalArgumentException("End date cannot be in the past");
        }

        productRepo.findById(productId).orElseThrow(
                () -> new DataNotFoundException("Product not found with id: " + productId)
        );
        var newProductPrice = generalMapper.convertToEntity(req, ProductPrice.class);

        List<ProductPrice> existingPrices = productPriceRepo.findAllByProductId(req.getProductId());

        // get List of current product prices , list when some mistake manually added
        for (ProductPrice price : existingPrices) {
            if (Objects.equals(price.getStatusId(), ProductPriceStatus.ACTIVE.name())) {
                // set the status of existing prices to INACTIVE and update the end date
                price.setStatusId(ProductPriceStatus.INACTIVE.name());
                price.setEndDate(startDate);
            }
        }

        newProductPrice.setId(SnowFlakeIdGenerator.getInstance().nextId(PRODUCT_PRICE_ID_PREFIX));
        newProductPrice.setStatusId(ProductPriceStatus.ACTIVE.name());

        productPriceRepo.save(newProductPrice);

        return ApiResponse.<Void>builder()
                .code(201)
                .message("Product price created successfully")
                .build();
    }

    @Override
    public ApiResponse<List<ProductPrice>> getProductPrice(String productId) {
        List<ProductPrice> productPrice = productPriceRepo.findAllByProductId(productId);

        if (productPrice.isEmpty()) {
            log.warn("No product prices found for productId: {}", productId);
        }

        return ApiResponse.<List<ProductPrice>>builder()
                .code(200)
                .message("Product price retrieved successfully")
                .data(productPrice)
                .build();
    }

    public Map<String, BigDecimal> getAllCurrentProductPrices(List<Product> products) {
        if(products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Product list cannot be null or empty");
        }

        Map<String, BigDecimal> productPriceMap = new HashMap<>();

        List<String> productIds = products.stream()
                .map(Product::getId)
                .toList();

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Map<String ,ProductPrice> productPrices = productPriceRepo
                .findAllByProductIdInAndStatusId(productIds, ProductPriceStatus.ACTIVE.name())
                .stream()
                .collect(Collectors.toMap(ProductPrice::getProductId, Function.identity()));

        // validate the endate and now if the end date is not null to set to return data, set the price if validated, else set the wholeSalePrice
        LocalDateTime now = LocalDateTime.now();
        for(Product product : products) {
            var productId = product.getId();
            ProductPrice price = productPrices.get(productId);
            if (price != null) {
                if (price.getEndDate() != null && price.getEndDate().isBefore(now)) {
                    productPriceMap.put(productId, product.getWholeSalePrice());
                } else {
                    productPriceMap.put(productId, price.getPrice());
                }
            } else {
                productPriceMap.put(productId, product.getWholeSalePrice());
            }
        }

        return productPriceMap;
    }
}
