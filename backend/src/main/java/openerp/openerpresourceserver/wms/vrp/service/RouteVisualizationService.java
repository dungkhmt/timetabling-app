package openerp.openerpresourceserver.wms.vrp.service;

import openerp.openerpresourceserver.wms.dto.delivery.CoordinateDTO;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryPointDTO;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryRouteResponseDTO;
import openerp.openerpresourceserver.wms.dto.delivery.ShipperRouteDTO;
import openerp.openerpresourceserver.wms.entity.DeliveryBill;
import openerp.openerpresourceserver.wms.vrp.GeoPoint;
import openerp.openerpresourceserver.wms.vrp.Node;
import openerp.openerpresourceserver.wms.vrp.VRPRoute;
import openerp.openerpresourceserver.wms.vrp.Vehicle;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPInput;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPSolution;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for creating visualization data from VRP solutions
 */
@Service
public class RouteVisualizationService {
    
    /**
     * Convert the VRP solution to visualization-friendly DTO
     */
    @SuppressWarnings("unchecked")
    public DeliveryRouteResponseDTO prepareRouteVisualization(CVRPSolution solution, CVRPInput input) {
        if (solution == null) {
            return new DeliveryRouteResponseDTO();
        }
        
        List<Node> nodes = input.getNodes();
        List<Vehicle> vehicles = input.getVehicles();
        Map<Integer, DeliveryBill> nodeToDeliveryBill = (Map<Integer, DeliveryBill>) input.getData();
        
        // Create response DTO
        DeliveryRouteResponseDTO responseDTO = new DeliveryRouteResponseDTO();
        List<ShipperRouteDTO> shipperRoutes = new ArrayList<>();
        
        int totalDeliveries = 0;
        
        // Process each route
        for (VRPRoute route : solution.getRoutes()) {
            int vehicleId = route.getVehicleId();
            List<Integer> nodeSequence = route.getNodeSequence();
            
            if (nodeSequence.isEmpty() || nodeSequence.size() <= 1) {
                continue; // Skip empty or depot-only routes
            }
            
            // Get vehicle/shipper details
            Vehicle vehicle = vehicles.get(vehicleId);
            
            // Create shipper route DTO
            ShipperRouteDTO shipperRoute = new ShipperRouteDTO();
            shipperRoute.setShipperId(vehicle.getDriverId());
            shipperRoute.setShipperName(vehicle.getDriverName());
            shipperRoute.setTotalDistance(route.getDistance());
            shipperRoute.setTotalLoad(route.getLoad());
            
            // Convert path points
            List<CoordinateDTO> pathCoordinates = new ArrayList<>();
            if (route.getPathPoints() != null) {
                for (GeoPoint point : route.getPathPoints()) {
                    pathCoordinates.add(new CoordinateDTO(point.getLatitude(), point.getLongitude()));
                }
            }
            shipperRoute.setPath(pathCoordinates);
            
            // Create delivery points
            List<DeliveryPointDTO> deliveryPoints = new ArrayList<>();
            int seq = 1;
            
            for (Integer nodeId : nodeSequence) {
                // Skip depot (node 0)
                if (nodeId == 0) {
                    continue;
                }
                
                Node node = nodes.get(nodeId);
                DeliveryBill bill = nodeToDeliveryBill.get(nodeId);
                
                if (bill != null) {
                    DeliveryPointDTO point = new DeliveryPointDTO(
                        bill.getId(),
                        bill.getToCustomer().getName(),
                        bill.getToCustomer().getCurrentAddressId(),
                        node.getLatitude(),
                        node.getLongitude(),
                        node.getDemand(),
                        seq++
                    );
                    
                    deliveryPoints.add(point);
                    totalDeliveries++;
                }
            }
            
            shipperRoute.setDeliveryPoints(deliveryPoints);
            shipperRoutes.add(shipperRoute);
        }
        
        // Set response properties
        responseDTO.setShipperRoutes(shipperRoutes);
        responseDTO.setTotalDistance(solution.getTotalDistance());
        responseDTO.setTotalDeliveries(totalDeliveries);
        responseDTO.setUnassignedDeliveries(solution.getUnscheduledNodes().size());
        
        return responseDTO;
    }
}