package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.SupplierStateId;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.supplier.SupplierListRes;
import openerp.openerpresourceserver.wms.repository.SupplierRepository;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    @Override
    public ApiResponse<Pagination<SupplierListRes>> getSuppliers(int page, int limit) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var suppliers = supplierRepository.findAllByStateId(pageReq, SupplierStateId.ACTIVE.name());
        var supplierListRes = suppliers.getContent().stream()
                .map(supplier -> SupplierListRes.builder()
                        .id(supplier.getId())
                        .name(supplier.getName())
                        .address(supplier.getAddress())
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
}
