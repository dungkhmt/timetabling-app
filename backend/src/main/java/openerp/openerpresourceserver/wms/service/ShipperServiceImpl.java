package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.ShipperGetListFilter;
import openerp.openerpresourceserver.wms.entity.Shipper;
import openerp.openerpresourceserver.wms.repository.ShipperRepo;
import openerp.openerpresourceserver.wms.repository.specification.ShipperSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService{
    private final ShipperRepo shipperRepo;
    @Override
    public ApiResponse<Pagination<Shipper>> getAll(int page, int limit, ShipperGetListFilter filters) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var shipperSpec = new ShipperSpecification(filters);
        var shipperPage = shipperRepo.findAll(shipperSpec, pageReq);

        return ApiResponse.<Pagination<Shipper>>builder()
                .code(200)
                .message("Get all shippers successfully")
                .data(Pagination.<Shipper>builder()
                        .page(page)
                        .size(limit)
                        .totalElements(shipperPage.getTotalElements())
                        .totalPages(shipperPage.getTotalPages())
                        .data(shipperPage.getContent())
                        .build())
                .build();
    }
}
