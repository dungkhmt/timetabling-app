package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;
import openerp.openerpresourceserver.wms.constant.enumrator.EntityType;
import openerp.openerpresourceserver.wms.constant.enumrator.SupplierStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.SupplierGetListFilter;
import openerp.openerpresourceserver.wms.dto.supplier.CreateSupplierReq;
import openerp.openerpresourceserver.wms.dto.supplier.SupplierListRes;
import openerp.openerpresourceserver.wms.entity.Address;
import openerp.openerpresourceserver.wms.entity.Supplier;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.AddressRepo;
import openerp.openerpresourceserver.wms.repository.SupplierRepository;
import openerp.openerpresourceserver.wms.repository.specification.SupplierSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static openerp.openerpresourceserver.wms.constant.Constants.ADDRESS_ID_PREFIX;
import static openerp.openerpresourceserver.wms.constant.Constants.SUPPLIER_ID_PREFIX;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final AddressRepo addressRepo;
    private final GeneralMapper generalMapper;
    @Override
    public ApiResponse<Pagination<SupplierListRes>> getSuppliers(int page, int limit) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var suppliers = supplierRepository.findAllByStatusId(pageReq, SupplierStatus.ACTIVE.name());
        var supplierListRes = suppliers.getContent().stream()
                .map(supplier -> SupplierListRes.builder()
                        .id(supplier.getId())
                        .name(supplier.getName())
                        .address(supplier.getCurrentAddressId())
                        .phone(supplier.getPhone())
                        .build())
                .toList();

        return ApiResponse.<Pagination<SupplierListRes>>builder()
                .code(200)
                .message("Success")
                .data(Pagination.<SupplierListRes>builder()
                        .page(suppliers.getNumber())
                        .size(suppliers.getSize())
                        .totalPages(suppliers.getTotalPages())
                        .totalElements(suppliers.getTotalElements())
                        .data(supplierListRes)
                        .build())
                .build();
    }

    @Override
    public ApiResponse<Void> createSupplier(CreateSupplierReq req) {
        var newSupplier = generalMapper.convertToEntity(req, Supplier.class);
        var newAddress = generalMapper.convertToEntity(req.getAddress(), Address.class);

        if(Objects.isNull(req.getId())) {
            newSupplier.setId(SnowFlakeIdGenerator.getInstance().nextId(SUPPLIER_ID_PREFIX));
        }
        newAddress.setId(SnowFlakeIdGenerator.getInstance().nextId(ADDRESS_ID_PREFIX));

        newSupplier.setStatusId(SupplierStatus.ACTIVE.name());
        newSupplier.setCurrentAddressId(newAddress.getId());

        newAddress.setEntityId(newSupplier.getId());
        newAddress.setEntityType(EntityType.SUPPLIER.name());

        supplierRepository.save(newSupplier);
        addressRepo.save(newAddress);

        return ApiResponse.<Void>builder()
                .code(201)
                .message("Create supplier successfully")
                .build();
    }

    @Override
    public ApiResponse<Pagination<Supplier>> getSuppliers(int page, int limit, SupplierGetListFilter filters) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var supplierSpec = new SupplierSpecification(filters);
        var supplierPage = supplierRepository.findAll(supplierSpec, pageReq);
        var addressIds = supplierPage.getContent().stream()
                .map(Supplier::getCurrentAddressId)
                .toList();

        var addresses = addressRepo.findAllByEntityIdInAndEntityType(addressIds, EntityType.SUPPLIER.name());

        var addressMap = addresses.stream()
                .collect(
                        java.util.stream.Collectors.toMap(Address::getId, address -> address)
                );

        supplierPage.getContent().forEach(supplier -> {
            if (addressMap.containsKey(supplier.getCurrentAddressId())) {
                supplier.setFullAddress(addressMap.get(supplier.getCurrentAddressId()).getFullAddress());
            }
                }
        );

        var pagination = Pagination.<Supplier>builder()
                .page(page)
                .size(limit)
                .totalPages(supplierPage.getTotalPages())
                .totalElements(supplierPage.getTotalElements())
                .data(supplierPage.getContent())
                .build();

        return ApiResponse.<Pagination<Supplier>>builder()
                .code(200)
                .message("Get suppliers successfully")
                .data(pagination)
                .build();

    }

    @Override
    public ApiResponse<Supplier> getSupplierById(String id) {
        return ApiResponse.<Supplier>builder()
                .code(200)
                .message("Get supplier successfully")
                .data(supplierRepository.findById(id).orElseThrow(
                        () -> new DataNotFoundException("Supplier not found with id: " + id)
                ))
                .build();
    }
}
