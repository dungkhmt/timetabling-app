package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.entity.Vehicle;
import openerp.openerpresourceserver.wms.repository.VehicleRepo;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService{
    private final VehicleRepo vehicleRepo;
    @Override
    public ApiResponse<Pagination<Vehicle>> getVehicles(Integer page, Integer limit, String statusId) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var vehicles = vehicleRepo.findAllByStatusId(statusId, pageReq);

        var pagination = Pagination.<Vehicle>builder()
                .page(page)
                .size(limit)
                .totalPages(vehicles.getTotalPages())
                .totalElements(vehicles.getTotalElements())
                .data(vehicles.getContent())
                .build();

        return ApiResponse.<Pagination<Vehicle>>builder()
                .code(200)
                .message("Get vehicles successfully")
                .data(pagination)
                .build();
    }
}
