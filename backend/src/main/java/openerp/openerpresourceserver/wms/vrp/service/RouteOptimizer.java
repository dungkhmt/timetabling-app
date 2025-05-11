package openerp.openerpresourceserver.wms.vrp.service;

import openerp.openerpresourceserver.wms.constant.enumrator.EntityType;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.repository.AddressRepo;
import openerp.openerpresourceserver.wms.vrp.Node;
import openerp.openerpresourceserver.wms.vrp.TimeDistance;
import openerp.openerpresourceserver.wms.vrp.Vehicle;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPInput;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPParams;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPSolution;
import openerp.openerpresourceserver.wms.vrp.cvrp.CVRPSolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for handling route optimization
 */
@Service
public class RouteOptimizer {
    private final DistanceCalculator distanceCalculator;
    private final CVRPSolverFactory solverFactory;
    private final AddressRepo addressRepo;

    public RouteOptimizer(
            @Qualifier("graphHopperDistanceCalculator") DistanceCalculator distanceCalculator,
            CVRPSolverFactory solverFactory,
            AddressRepo addressRepo) {
        this.distanceCalculator = distanceCalculator;
        this.solverFactory = solverFactory;
        this.addressRepo = addressRepo;
    }

    /**
     * Build the VRP input model from delivery data
     */
    public CVRPInput setupVRPInput(
            Facility facility,
            List<DeliveryBill> deliveryBills,
            List<Shipper> shippers, List<openerp.openerpresourceserver.wms.entity.Vehicle> availableVehicles) {
        var facilityAddress = addressRepo.findByEntityIdAndEntityType(facility.getId(), EntityType.FACILITY.name());
        // Create nodes list
        List<Node> nodes = new ArrayList<>();
        
        // Add depot (facility) as the first node (index 0)
        Node depot = new Node(
                0,
                "Depot - " + facility.getName(),
                facilityAddress.getLatitude(),
                facilityAddress.getLongitude()
        );
        nodes.add(depot);
        
        // Add delivery locations as nodes
        Map<Integer, DeliveryBill> nodeToDeliveryBill = new HashMap<>();
        int nodeId = 1;

        List<String> customerIds = deliveryBills.stream().map(
            deliveryBill -> deliveryBill.getToCustomer().getId()
        ).toList();

        List<Address> addresses = addressRepo.findAllByEntityIdInAndEntityType(customerIds, EntityType.CUSTOMER.name());

        Map<String, Address> customerToAddress = addresses.stream().collect(Collectors.toMap(
            Address::getEntityId,
            Function.identity()
        ));


        for (DeliveryBill bill : deliveryBills) {
            var customer = bill.getToCustomer();
            // Skip bills with missing coordinates
            var longitude = customerToAddress.get(customer.getId()).getLongitude();
            var latitude = customerToAddress.get(customer.getId()).getLatitude();

            if (longitude == null || latitude == null) {
                continue;
            }

            Node node = new Node(
                nodeId,
                "Delivery to " + customer.getName(),
                latitude,
                longitude,
                bill.getTotalWeight() != null ? bill.getTotalWeight().doubleValue() : 1.0
            );
            
            nodes.add(node);
            nodeToDeliveryBill.put(nodeId, bill);
            nodeId++;
        }
        
        // Create vehicles from available shippers
        List<Vehicle> vehicles = new ArrayList<>();
        int vehicleId = 0;
        
        for (Shipper shipper : shippers) {

            var availableVehicle  = availableVehicles.get(vehicleId);
            Vehicle vehicle = Vehicle.fromEntity(availableVehicle, vehicleId++
                    ,shipper.getUserLoginId(),
                    shipper.getFullName());
            
            vehicles.add(vehicle);
        }
        
        // Calculate distance matrix
        List<TimeDistance> distances =
            distanceCalculator.calculateDistanceMatrix(nodes);
        
        // Create input object
        CVRPInput input = new CVRPInput();
        input.setNodes(nodes);
        input.setVehicles(vehicles);
        input.setDistances(distances);
        input.setData(nodeToDeliveryBill);
        
        return input;
    }
    
    /**
     * Solve the VRP problem
     */
    public CVRPSolution optimizeRoutes(CVRPInput input) {
        return optimizeRoutes(input, "greedy");
    }
    
    /**
     * Solve the VRP problem with a specific solver
     */
    public CVRPSolution optimizeRoutes(CVRPInput input, String solverName) {
        CVRPSolver solver = solverFactory.getSolver(solverName);
        CVRPParams params = CVRPParams.getDefaultParams();
        
        return solver.solve(input, params);
    }
}