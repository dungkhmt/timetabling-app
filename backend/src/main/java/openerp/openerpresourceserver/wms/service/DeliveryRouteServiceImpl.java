package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipperStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryRouteResponseDTO;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

            // Get delivery bills associated with this delivery plan
            List<String> deliveryBillIds = deliveryPlanOrderRepo.findByDeliveryPlanId(deliveryPlan.getId());
            List<String> shipperIds = deliveryPlanShipperRepo.findByDeliveryPlanId(deliveryPlan.getId());


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
                        .message("No available shippers found for this facility")
                        .build();
            }

            // Create a new delivery route for this plan
            DeliveryRoute deliveryRoute = new DeliveryRoute();
            deliveryRoute.setId(CommonUtil.getUUID());
            deliveryRoute.setDelivery(deliveryPlan);
            deliveryRoute.setStatusId("CREATED");
            deliveryRouteRepo.save(deliveryRoute);

            // Set up VRP problem
            CVRPInput vrpInput = routeOptimizer.setupVRPInput(facility, deliveryBills, shippers);

            // Solve the VRP problem
            CVRPSolution solution = routeOptimizer.optimizeRoutes(vrpInput);

            if (solution == null || solution.getRoutes().isEmpty()) {
                return ApiResponse.<DeliveryRouteResponseDTO>builder()
                        .code(400)
                        .message("Could not find a feasible routing solution")
                        .build();
            }

            // Save the routing solution to the database
            saveRoutingSolution(deliveryRoute, solution, vrpInput);

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
     */
    @SuppressWarnings("unchecked")
    private void saveRoutingSolution(DeliveryRoute deliveryRoute, CVRPSolution solution, CVRPInput input) {
        // Update delivery route status
        deliveryRoute.setStatusId("ASSIGNED");
        deliveryRouteRepo.save(deliveryRoute);

        Map<Integer, DeliveryBill> nodeToDeliveryBill = (Map<Integer, DeliveryBill>) input.getData();
        List<Node> nodes = input.getNodes();
        List<Vehicle> vehicles = input.getVehicles();

        // First, delete any existing route items for this route to avoid duplicates
        deliveryRouteItemRepo.deleteByDeliveryRouteId(deliveryRoute.getId());

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

            // Update shipper status and assign to route
            shipper.setStatusId("ASSIGNED");
            shipperRepo.save(shipper);

            // Update route with assigned shipper
            deliveryRoute.setAssignToShipper(shipper);
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
                deliveryBill.setStatusId("ASSIGNED_TO_ROUTE");
                deliveryBillRepo.save(deliveryBill);

                seq++;
            }

            // Save all route items
            if (!routeItems.isEmpty()) {
                deliveryRouteItemRepo.saveAll(routeItems);
            }
        }

        // Update delivery plan status
        DeliveryPlan deliveryPlan = deliveryRoute.getDelivery();
        deliveryPlan.setStatusId("ROUTING_COMPLETED");
        deliveryPlanRepo.save(deliveryPlan);
    }
}