package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;
import openerp.openerpresourceserver.wms.constant.enumrator.*;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.*;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryPlanGetListFilter;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.repository.specification.DeliveryPlanSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static io.micrometer.common.util.StringUtils.isBlank;
import static openerp.openerpresourceserver.wms.constant.Constants.*;

@Service
@RequiredArgsConstructor
public class DeliveryPlanServiceImpl implements DeliveryPlanService {
    private final DeliveryPlanRepo deliveryPlanRepo;
    private final DeliveryPlanOrderRepo deliveryPlanOrderRepo;
    private final DeliveryPlanShipperRepo deliveryPlanShipperRepo;
    private final DeliveryPlanVehicleRepo deliveryPlanVehicleRepo;
    private final ShipperRepo shipperRepo;
    private final VehicleRepo vehicleRepo;
    private final DeliveryBillRepo deliveryBillRepo;
    private final GeneralMapper generalMapper;
    private final UserLoginRepo userLoginRepo;
    private final FacilityRepo facilityRepo;
    private final DeliveryRouteRepo deliveryRouteRepo;
    @Override
    public ApiResponse<Void> createDeliveryPlan(CreateDeliveryPlan req, Principal principal) {
        List<DeliveryPlanOrder> deliveryPlanOrders = new ArrayList<>();
        List<DeliveryPlanShipper> deliveryPlanShippers = new ArrayList<>();
        List<DeliveryPlanVehicle> deliveryPlanVehicles = new ArrayList<>();
        List<DeliveryBill> deliveryBills = deliveryBillRepo.findAllById(req.getDeliveryBillIds());
        List<Shipper> shippers = shipperRepo.findAllById(req.getShipperIds());
        List<Vehicle> vehicles = vehicleRepo.findAllById(req.getVehicleIds());
        var facility = facilityRepo.findById(req.getFacilityId()).orElseThrow(
                () -> new DataNotFoundException("Facility not found with id: " + req.getFacilityId())
        );
        var userLogin = userLoginRepo.findById(principal.getName()).orElseThrow(
                () -> new DataNotFoundException("User not found with id: " + principal.getName())
        );

        var deliveryPlan = generalMapper.convertToEntity(req, DeliveryPlan.class);

        if(isBlank(deliveryPlan.getId())) {
            deliveryPlan.setId(SnowFlakeIdGenerator.getInstance().nextId(DELIVERY_PLAN_ID_PREFIX));
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        var delveryPlanOrderSeq = 1;
        for (var deliveryBill: deliveryBills) {
//            deliveryBill.setStatusId(DeliveryBillStatus.IN_PROGRESS.name());
            deliveryBill.setDeliveryStatusId(DeliveryBillTripStatus.IN_PLAN.name());
            var deliveryPlanOrder = DeliveryPlanOrder.builder()
                    .id(SnowFlakeIdGenerator.getInstance().nextId(DELIVERY_PLAN_ORDER_ID_PREFIX))
                    .deliveryPlanId(deliveryPlan.getId())
                    .deliveryBillId(deliveryBill.getId())
                    .deliveryPlanOrderSeqId(delveryPlanOrderSeq++)
                    .build();
            totalWeight = totalWeight.add(deliveryBill.getTotalWeight());
            deliveryPlanOrders.add(deliveryPlanOrder);
        }

        var deliveryPlanShipperSeq = 1;
        for (var shipper: shippers) {
//            shipper.setStatusId(ShipperStatus.ASSIGNED.name());
            shipper.setDeliveryStatusId(ShipperTripStatus.IN_PLAN.name());
            var deliveryPlanShipper = DeliveryPlanShipper.builder()
                    .id(SnowFlakeIdGenerator.getInstance().nextId(DELIVERY_PLAN_SHIPPER_ID_PREFIX))
                    .deliveryPlanId(deliveryPlan.getId())
                    .shipperId(shipper.getUserLoginId())
                    .deliveryPlanShipperSeqId(deliveryPlanShipperSeq++)
                    .driverRoleId(DriverRole.DRIVER.name())
                    .build();
            deliveryPlanShippers.add(deliveryPlanShipper);
        }

        var deliveryPlanVehicleSeq = 1;
        for(var vehicle: vehicles) {
//            vehicle.setStatusId(VehicleStatus.ASSIGNED.name());
            vehicle.setDeliveryStatusId(VehicleTripStatus.IN_PLAN.name());
            var deliveryPlanVehicle = DeliveryPlanVehicle.builder()
                    .id(SnowFlakeIdGenerator.getInstance().nextId(DELIVERY_PLAN_VEHICLE_ID_PREFIX))
                    .deliveryPlanId(deliveryPlan.getId())
                    .vehicleId(vehicle.getId())
                    .deliveryPlanVehicleSeqId(deliveryPlanVehicleSeq++)
                    .build();
            deliveryPlanVehicles.add(deliveryPlanVehicle);
        }

        //Set Details for Delivery Plan
        deliveryPlan.setTotalWeight(totalWeight);
        deliveryPlan.setStatusId(DeliveryBillStatus.CREATED.name());
        deliveryPlan.setCreatedByUser(userLogin);
        deliveryPlan.setFacility(facility);

        deliveryPlanRepo.save(deliveryPlan);
        deliveryPlanOrderRepo.saveAll(deliveryPlanOrders);
        deliveryPlanShipperRepo.saveAll(deliveryPlanShippers);
        deliveryPlanVehicleRepo.saveAll(deliveryPlanVehicles);

        return ApiResponse.<Void>builder()
                .code(201)
                .message("Delivery Plan Created Successfully")
                .build();

    }

    @Override
    public ApiResponse<Pagination<DeliveryPlanPageRes>> getAllDeliveryPlans(int page, int limit, DeliveryPlanGetListFilter filters) {
        var pageable = CommonUtil.getPageRequest(page, limit);
        var deliveryPlanSpec = new DeliveryPlanSpecification(filters);
        var deliveryPlans = deliveryPlanRepo.findAll(deliveryPlanSpec, pageable);

        List<DeliveryPlanPageRes> deliveryPlanList = deliveryPlans.getContent().stream().map(deliveryPlan -> {
            var deliveryPlanPageRes = generalMapper.convertToDto(deliveryPlan, DeliveryPlanPageRes.class);
            deliveryPlanPageRes.setFacilityName(deliveryPlan.getFacility().getName());
            deliveryPlanPageRes.setCreatedByUserName(deliveryPlan.getCreatedByUser().getFullName());
            return deliveryPlanPageRes;
        }).toList();

        var pagination = Pagination.<DeliveryPlanPageRes>builder()
                .page(page)
                .size(limit)
                .data(deliveryPlanList)
                .totalElements(deliveryPlans.getTotalElements())
                .totalPages(deliveryPlans.getTotalPages())
                .build();

        return ApiResponse.<Pagination<DeliveryPlanPageRes>>builder()
                .code(200)
                .message("Delivery Plans Retrieved Successfully")
                .data(pagination)
                .build();
    }

    @Override
    public ApiResponse<DeliveryPlanDetailRes> getDeliveryPlanById(String deliveryPlanId) {
        var deliveryPlan = deliveryPlanRepo.findById(deliveryPlanId).orElseThrow(() ->
                new DataNotFoundException("Delivery Plan not found with id: " + deliveryPlanId));

        var deliveryPlanDetailRes = generalMapper.convertToDto(deliveryPlan, DeliveryPlanDetailRes.class);
        deliveryPlanDetailRes.setFacilityName(deliveryPlan.getFacility().getName());
        deliveryPlanDetailRes.setCreatedByUserName(deliveryPlan.getCreatedByUser().getFullName());

        List<String> deliveryBillIds = deliveryPlanOrderRepo.findByDeliveryPlanId(deliveryPlan.getId())
                .stream()
                .map(DeliveryPlanOrder::getDeliveryBillId)
                .toList();

        List<String> shipperIds = deliveryPlanShipperRepo.findByDeliveryPlanId(deliveryPlan.getId())
                .stream()
                .map(DeliveryPlanShipper::getShipperId)
                .toList();

        List<String> vehicleIds = deliveryPlanVehicleRepo.findByDeliveryPlanId(deliveryPlan.getId())
                .stream()
                .map(DeliveryPlanVehicle::getVehicleId)
                .toList();

        List<DeliveryBillPlanRes> deliveryBills = deliveryBillRepo.findAllById(deliveryBillIds)
                .stream()
                .map(deliveryBill -> {
                    var deliveryBillPlanRes = generalMapper.convertToDto(deliveryBill, DeliveryBillPlanRes.class);
                    deliveryBillPlanRes.setShipmentId(deliveryBill.getShipment().getId());
                    deliveryBillPlanRes.setShipmentName(deliveryBill.getShipment().getShipmentName());
                    deliveryBillPlanRes.setToCustomerName(deliveryBill.getToCustomer().getName());
                    return deliveryBillPlanRes;
                })
                .toList();
        List<ShipperDeliveryPlanRes> shippers = shipperRepo.findAllById(shipperIds)
                .stream()
                .map(shipper -> {
                    var shipperDeliveryPlanRes = generalMapper.convertToDto(shipper, ShipperDeliveryPlanRes.class);
                    shipperDeliveryPlanRes.setShipperName(shipper.getUserLogin().getFullName());
                    return shipperDeliveryPlanRes;
                })
                .toList();

        List<DeliveryRoutePlanRes> existingRoutes = deliveryRouteRepo.findByDeliveryPlanId(deliveryPlan.getId())
                .stream()
                .map(deliveryRoute -> {
                    var deliveryRoutePlanRes = generalMapper.convertToDto(deliveryRoute, DeliveryRoutePlanRes.class);
                    deliveryRoutePlanRes.setDeliveryPlanId(deliveryPlan.getId());
                    deliveryRoutePlanRes.setAssignToShipperName(deliveryRoute.getAssignToShipper().getUserLogin().getFullName());
                    deliveryRoutePlanRes.setAssignToVehicleName(deliveryRoute.getAssignToVehicle().getVehicleName());
                    deliveryRoutePlanRes.setAssignToVehicleId(deliveryRoute.getAssignToVehicle().getId());
                    deliveryRoutePlanRes.setStatusId(deliveryRoute.getStatusId());
                    return deliveryRoutePlanRes;
                })
                .toList();

        List<VehicleDeliveryPlanRes> vehicles = vehicleRepo.findAllById(vehicleIds)
                .stream()
                .map(vehicle -> {
                    var vehicleDeliveryPlanRes = generalMapper.convertToDto(vehicle, VehicleDeliveryPlanRes.class);
                    return vehicleDeliveryPlanRes;
                })
                .toList();

        deliveryPlanDetailRes.setDeliveryBills(deliveryBills);
        deliveryPlanDetailRes.setShippers(shippers);
        deliveryPlanDetailRes.setExistingRoutes(existingRoutes);
        deliveryPlanDetailRes.setVehicles(vehicles);

        return ApiResponse.<DeliveryPlanDetailRes>builder()
                .code(200)
                .message("Delivery Plan Retrieved Successfully")
                .data(deliveryPlanDetailRes)
                .build();
    }

}
