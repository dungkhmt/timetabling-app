package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.entity.Shipper;
import openerp.openerpresourceserver.wms.repository.ShipperRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService{
    private final ShipperRepo shipperRepo;
    @Override
    public ApiResponse<List<Shipper>> getAll(String statusId) {
        return ApiResponse.<List<Shipper>>builder()
                .code(200)
                .message("Get all shippers successfully")
                .data(shipperRepo.findByStatusId(statusId))
                .build();
    }
}
