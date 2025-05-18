package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.ShipperGetListFilter;
import openerp.openerpresourceserver.wms.dto.shipper.ShipperGetListRes;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.ShipperRepo;
import openerp.openerpresourceserver.wms.repository.specification.ShipperSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService{
    private final ShipperRepo shipperRepo;
    private final GeneralMapper genaralMapper;
    @Override
    public ApiResponse<Pagination<ShipperGetListRes>> getAll(int page, int limit, ShipperGetListFilter filters) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var shipperSpec = new ShipperSpecification(filters);
        var shipperPage = shipperRepo.findAll(shipperSpec, pageReq);

        var shipperList = shipperPage.getContent().stream()
                .map(shipper ->
                {
                    var shipperRes = genaralMapper.convertToDto(shipper, ShipperGetListRes.class);
                    shipperRes.setEmail(shipper.getUserLogin().getEmail());
                    shipperRes.setFullName(shipper.getFullName());
                    return shipperRes;
                })
                .toList();

        var pagination = Pagination.<ShipperGetListRes>builder()
                .page(page)
                .size(limit)
                .totalElements(shipperPage.getTotalElements())
                .totalPages(shipperPage.getTotalPages())
                .data(shipperList)
                .build();

        return ApiResponse.<Pagination<ShipperGetListRes>>builder()
                .code(200)
                .message("Get all shippers successfully")
                .data(pagination)
                .build();
    }
}
