package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.constant.enumrator.DeliveryBillStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.DeliveryPlanStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipperStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.VehicleStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryRouteGetListRes;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryRouteResponseDTO;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryRouteGetListFilter;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.repository.specification.DeliveryRouteSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import openerp.openerpresourceserver.wms.vrp.Node;
import openerp.openerpresourceserver.wms.vrp.VRPRoute;
import openerp.openerpresourceserver.wms.vrp.Vehicle;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPInput;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPSolution;
import openerp.openerpresourceserver.wms.vrp.service.RouteOptimizer;
import openerp.openerpresourceserver.wms.vrp.service.RouteVisualizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryRouteServiceImpl implements DeliveryRouteService {

    private final DeliveryRouteRepo deliveryRouteRepo;
    private final DeliveryRouteItemRepo deliveryRouteItemRepo;
    private final DeliveryPlanRepo deliveryPlanRepo;
    private final DeliveryBillRepo deliveryBillRepo;
    private final DeliveryPlanOrderRepo deliveryPlanOrderRepo;
    private final ShipperRepo shipperRepo;
    private final FacilityRepo facilityRepo;
    private final DeliveryPlanShipperRepo deliveryPlanShipperRepo;
    private final DeliveryPlanVehicleRepo deliveryPlanVehicleRepo;
    private final VehicleRepo vehicleRepo;
    private final GeneralMapper genaralMapper;

    // Services
    private final RouteOptimizer routeOptimizer;
    private final RouteVisualizationService visualizationService;

    @Override
    @Transactional
    public ApiResponse<DeliveryRouteResponseDTO> autoAssignDeliveryRoutesForPlan(String deliveryPlanId) {
        try {
            // Fetch delivery plan
            DeliveryPlan deliveryPlan = deliveryPlanRepo.findById(deliveryPlanId)
                    .orElseThrow(() -> new DataNotFoundException("Delivery plan not found with id: " + deliveryPlanId));

            // Get facility (depot) location
            Facility facility = deliveryPlan.getFacility();
            if (facility == null) {
                return ApiResponse.<DeliveryRouteResponseDTO>builder()
                        .code(400)
                        .message("Delivery plan does not have a facility assigned")
                        .build();
            }

            // Get delivery bills associated with this delivery plan
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

            List<DeliveryBill> deliveryBills = deliveryBillRepo.findAllById(deliveryBillIds);
            if (deliveryBills.isEmpty()) {
                return ApiResponse.<DeliveryRouteResponseDTO>builder()
                        .code(400)
                        .message("No delivery bills found for this delivery plan")
                        .build();
            }

            // Get available shippers for this facility
            List<Shipper> shippers = shipperRepo.findAllById(shipperIds);
            if (shippers.isEmpty()) {
                return ApiResponse.<DeliveryRouteResponseDTO>builder()
                        .code(400)
                        .message("No available shippers found for this delivery plan")
                        .build();
            }

            List<openerp.openerpresourceserver.wms.entity.Vehicle> vehicles = vehicleRepo.findAllById(vehicleIds);
            if (vehicles.isEmpty()) {
                return ApiResponse.<DeliveryRouteResponseDTO>builder()
                        .code(400)
                        .message("No available vehicles found for this delivery plan")
                        .build();
            }

            // First, delete any existing routes for this delivery plan to avoid duplicates
            List<DeliveryRoute> existingRoutes = deliveryRouteRepo.findByDeliveryPlanId(deliveryPlan.getId());
            for (DeliveryRoute route : existingRoutes) {
                deliveryRouteItemRepo.deleteByDeliveryRouteId(route.getId());
            }
            deliveryRouteRepo.deleteAll(existingRoutes);

            // Set up VRP problem
            CVRPInput vrpInput = routeOptimizer.setupVRPInput(facility, deliveryBills, shippers, vehicles);

            // Solve the VRP problem
            CVRPSolution solution = routeOptimizer.optimizeRoutes(vrpInput);

            if (solution == null || solution.getRoutes().isEmpty()) {
                return ApiResponse.<DeliveryRouteResponseDTO>builder()
                        .code(400)
                        .message("Could not find a feasible routing solution")
                        .build();
            }

            // Save the routing solution to the database
            saveRoutingSolution(deliveryPlan, solution, vrpInput, vehicles);

            // Prepare response with detailed route paths
            DeliveryRouteResponseDTO responseDTO = visualizationService.prepareRouteVisualization(solution, vrpInput);

            return ApiResponse.<DeliveryRouteResponseDTO>builder()
                    .code(200)
                    .message("Delivery routes created and assigned successfully")
                    .data(responseDTO)
                    .build();

        } catch (Exception e) {
            log.error("Error auto-assigning delivery routes for plan", e);
            return ApiResponse.<DeliveryRouteResponseDTO>builder()
                    .code(500)
                    .message("Error auto-assigning delivery routes: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Save the routing solution to the database
     * Creates one delivery route per shipper with assigned deliveries
     */
    @SuppressWarnings("unchecked")
    private void saveRoutingSolution(
            DeliveryPlan deliveryPlan,
            CVRPSolution solution,
            CVRPInput input,
            List<openerp.openerpresourceserver.wms.entity.Vehicle> availableVehicles) {

        Map<Integer, DeliveryBill> nodeToDeliveryBill = (Map<Integer, DeliveryBill>) input.getData();
        List<Node> nodes = input.getNodes();
        List<Vehicle> vehicles = input.getVehicles();

        // Track assigned bills to avoid duplicates
        Map<String, Boolean> assignedBills = new HashMap<>();
        Map<String, openerp.openerpresourceserver.wms.entity.Vehicle> vehicleMap = availableVehicles.stream().collect(
                Collectors.toMap(openerp.openerpresourceserver.wms.entity.Vehicle::getId, Function.identity())
        );

        // Track vehicle assignments
        int vehicleIndex = 0;

        // Process each route (one per shipper)
        for (VRPRoute route : solution.getRoutes()) {
            int vehicleId = route.getVehicleId();
            List<Integer> nodeSequence = route.getNodeSequence();

            if (nodeSequence.isEmpty() || nodeSequence.size() <= 1) {
                // Skip empty routes or routes with only depot
                continue;
            }

            // Get shipper for this route
            String shipperId = vehicles.get(vehicleId).getDriverId();
            Shipper shipper = shipperRepo.findById(shipperId)
                    .orElseThrow(() -> new RuntimeException("Shipper not found: " + shipperId));

            // Update shipper status
            shipper.setStatusId(ShipperStatus.IN_TRIP.name());
            shipperRepo.save(shipper);

            // Create a new delivery route for this shipper
            DeliveryRoute deliveryRoute = new DeliveryRoute();
            deliveryRoute.setId(CommonUtil.getUUID());
            deliveryRoute.setDeliveryPlan(deliveryPlan);
            deliveryRoute.setStatusId("ASSIGNED");
            deliveryRoute.setAssignToShipper(shipper);

            // Assign a vehicle if available
//            if (vehicleIndex < availableVehicles.size()) {
//                openerp.openerpresourceserver.wms.entity.Vehicle vehicle = availableVehicles.get(vehicleIndex++);
//                deliveryRoute.setAssignToVehicle(vehicle);
//
//                // Update vehicle status
//                vehicle.setStatusId(VehicleStatus.IN_USE.name());
//                vehicleRepo.save(vehicle);
//            } else {
//                log.warn("No vehicle available for shipper: {}", shipper.getUserLoginId());
//            }

            // Assign vehicle from the map
            openerp.openerpresourceserver.wms.entity.Vehicle vehicleEntity = vehicleMap.get(vehicles.get(vehicleId).getVehicleId());
            if (vehicleEntity != null) {
                deliveryRoute.setAssignToVehicle(vehicleEntity);
                vehicleEntity.setStatusId(VehicleStatus.IN_USE.name());
                vehicleRepo.save(vehicleEntity);
            } else {
                log.warn("No vehicle found for vehicle ID: {}", vehicleId);
            }

            deliveryRouteRepo.save(deliveryRoute);

            // Create delivery route items for each node in sequence
            int seq = 1;
            List<DeliveryRouteItem> routeItems = new ArrayList<>();

            for (Integer nodeId : nodeSequence) {
                // Skip depot (node 0)
                if (nodeId == 0) {
                    continue;
                }

                DeliveryBill deliveryBill = nodeToDeliveryBill.get(nodeId);
                if (deliveryBill == null) {
                    log.warn("No delivery bill found for node {}", nodeId);
                    continue;
                }

                // Skip if already assigned to avoid duplicates
                if (assignedBills.containsKey(deliveryBill.getId())) {
                    log.warn("Delivery bill {} already assigned to another route", deliveryBill.getId());
                    continue;
                }

                // Create route item
                DeliveryRouteItem routeItem = new DeliveryRouteItem();
                routeItem.setId(CommonUtil.getUUID());
                routeItem.setDeliveryRoute(deliveryRoute);
                routeItem.setDeliveryRouteSeqId(String.format("DRS%05d", seq));
                routeItem.setSeq(seq);
                routeItem.setDeliveryBill(deliveryBill);
                routeItem.setStatusId("ASSIGNED");

                routeItems.add(routeItem);

                // Update delivery bill status
                deliveryBill.setStatusId(DeliveryBillStatus.ASSIGNED.name());
                deliveryBillRepo.save(deliveryBill);

                // Mark as assigned
                assignedBills.put(deliveryBill.getId(), true);

                seq++;
            }

            // Save all route items
            if (!routeItems.isEmpty()) {
                deliveryRouteItemRepo.saveAll(routeItems);
            } else {
                // If no items were assigned to this route, delete it and free up the vehicle
                if (deliveryRoute.getAssignToVehicle() != null) {
                    openerp.openerpresourceserver.wms.entity.Vehicle vehicle = deliveryRoute.getAssignToVehicle();
                    vehicle.setStatusId(VehicleStatus.AVAILABLE.name());
                    vehicleRepo.save(vehicle);
                }
                deliveryRouteRepo.delete(deliveryRoute);
            }
        }

        // Handle unassigned delivery bills
        for (DeliveryBill bill : deliveryBillRepo.findAllById(nodeToDeliveryBill.values().stream()
                .map(DeliveryBill::getId)
                .filter(id -> !assignedBills.containsKey(id))
                .toList())) {
            // Mark unassigned bills
            bill.setStatusId(DeliveryBillStatus.UNASSIGNED.name());
            deliveryBillRepo.save(bill);
        }

        // Update delivery plan status
        deliveryPlan.setStatusId(DeliveryPlanStatus.IN_PROGRESS.name());
        deliveryPlanRepo.save(deliveryPlan);
    }


    @Override
    public ApiResponse<Pagination<DeliveryRouteGetListRes>> getAlls(int page, int limit, DeliveryRouteGetListFilter filters) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var deliveryRouteSpec = new DeliveryRouteSpecification(filters);
        var deliveryRoutes = deliveryRouteRepo.findAll(deliveryRouteSpec, pageReq);

        var deliveryRouteList = deliveryRoutes.getContent().stream()
                .map(deliveryRoute -> {
                    var deliveryRouteGetListRes = genaralMapper.convertToDto(deliveryRoute, DeliveryRouteGetListRes.class);
                    if(Objects.nonNull(deliveryRoute.getAssignToVehicle())) {
                        deliveryRouteGetListRes.setAssignToVehicleId(deliveryRoute.getAssignToVehicle().getId());
                        deliveryRouteGetListRes.setAssignToVehicleName(deliveryRoute.getAssignToVehicle().getVehicleName());
                    }

                    if(Objects.nonNull(deliveryRoute.getAssignToShipper())) {
                        deliveryRouteGetListRes.setAssignToShipperId(deliveryRoute.getAssignToShipper().getUserLoginId());
                        deliveryRouteGetListRes.setAssignToShipperName(deliveryRoute.getAssignToShipper().getFullName());
                    }

                    deliveryRouteGetListRes.setDeliveryPlanId(deliveryRoute.getDeliveryPlan().getId());
                    deliveryRouteGetListRes.setDeliveryPlanName(deliveryRoute.getDeliveryPlan().getDeliveryPlanName());
                    return deliveryRouteGetListRes;
                }).<DeliveryRouteGetListRes>toList();

        var pagination = Pagination.<DeliveryRouteGetListRes>builder()
                .page(page)
                .size(limit)
                .totalElements(deliveryRoutes.getTotalElements())
                .totalPages(deliveryRoutes.getTotalPages())
                .data(deliveryRouteList)
                .build();

        return ApiResponse.<Pagination<DeliveryRouteGetListRes>>builder()
                .code(200)
                .message("Get delivery routes successfully")
                .data(pagination)
                .build();

    }
}