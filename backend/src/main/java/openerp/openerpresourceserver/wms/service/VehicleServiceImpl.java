package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.VehicleGetListFilter;
import openerp.openerpresourceserver.wms.entity.Vehicle;
import openerp.openerpresourceserver.wms.repository.VehicleRepo;
import openerp.openerpresourceserver.wms.repository.specification.VehicleSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService{
    private final VehicleRepo vehicleRepo;
    @Override
    public ApiResponse<Pagination<Vehicle>> getVehicles(Integer page, Integer limit, VehicleGetListFilter filter) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var vehicleSpec = new VehicleSpecification(filter);
        var vehiclePage = vehicleRepo.findAll(vehicleSpec, pageReq);

        var pagination = Pagination.<Vehicle>builder()
                .page(page)
                .size(limit)
                .totalPages(vehiclePage.getTotalPages())
                .totalElements(vehiclePage.getTotalElements())
                .data(vehiclePage.getContent())
                .build();

        return ApiResponse.<Pagination<Vehicle>>builder()
                .code(200)
                .message("Get vehicles successfully")
                .data(pagination)
                .build();
    }
}
