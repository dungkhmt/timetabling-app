package openerp.openerpresourceserver.wms.vrp.cvrp;

import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.vrp.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class CVRPGreedySolver implements CVRPSolver {
    
    @Override
    public CVRPSolution solve(CVRPInput input, CVRPParams params) {
        long startTime = System.currentTimeMillis();
        
        List<Node> nodes = input.getNodes();
        List<Vehicle> vehicles = input.getVehicles();
        
        // Create empty solution
        CVRPSolution solution = CVRPSolution.createEmpty();
        List<VRPRoute> routes = new ArrayList<>();
        
        // Create a set to track assigned nodes (excluding depot which is node 0)
        Set<Integer> assignedNodes = new HashSet<>();
        assignedNodes.add(0); // Depot is always assigned
        
        // For each vehicle
        for (int v = 0; v < vehicles.size(); v++) {
            Vehicle vehicle = vehicles.get(v);
            
            // Create a new route for this vehicle
            VRPRoute route = VRPRoute.createEmpty(v);
            
            // Start at depot
            List<Integer> nodeSequence = new ArrayList<>();
            nodeSequence.add(0); // Depot
            
            double remainingCapacity = vehicle.getCapacity();
            double routeDistance = 0.0;
            double routeDuration = 0.0;
            int currentNodeIndex = 0;
            
            // Track nodes to visit in this route (for path visualization)
            List<GeoPoint> pathPoints = new ArrayList<>();
            
            // Keep adding nodes until we can't add any more
            boolean addedNode;
            do {
                addedNode = false;
                double bestDistance = Double.MAX_VALUE;
                int bestNodeIndex = -1;
                
                // Find the closest unassigned node that fits in our capacity
                for (int j = 1; j < nodes.size(); j++) {
                    if (assignedNodes.contains(j)) continue;
                    
                    Node candidate = nodes.get(j);
                    if (params.isUseCapacityConstraints() && candidate.getDemand() > remainingCapacity) continue;
                    
                    // Get distance from current node to candidate
                    double distance = input.getDistance(currentNodeIndex, j);
                    
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestNodeIndex = j;
                    }
                }
                
                // If we found a suitable node, add it to the route
                if (bestNodeIndex != -1) {
                    TimeDistance td = input.findTimeDistance(currentNodeIndex, bestNodeIndex);
                    
                    nodeSequence.add(bestNodeIndex);
                    remainingCapacity -= nodes.get(bestNodeIndex).getDemand();
                    routeDistance += bestDistance;
                    routeDuration += td.getTravelTime() + nodes.get(bestNodeIndex).getServiceTime();
                    
                    assignedNodes.add(bestNodeIndex);
                    currentNodeIndex = bestNodeIndex;
                    addedNode = true;
                    
                    // Add path segment to full path
                    if (td.getPath() != null) {
                        pathPoints.addAll(td.getPath());
                    }
                }
                
            } while (addedNode);
            
            // If we have added nodes to the route, finalize it
            if (nodeSequence.size() > 1) {
//                 Return to depot
                TimeDistance returnTd = input.findTimeDistance(currentNodeIndex, 0);
                double returnDistance = returnTd.getDistance();
                double returnDuration = returnTd.getTravelTime();

                routeDistance += returnDistance;
                routeDuration += returnDuration;
                nodeSequence.add(0); // Return to depot
                
                // Add return path to full path
//                if (returnTd.getPath() != null) {
//                    pathPoints.addAll(returnTd.getPath());
//                }
                
                route.setNodeSequence(nodeSequence);
                route.setDistance(routeDistance);
                route.setDuration(routeDuration);
                route.setLoad(vehicle.getCapacity() - remainingCapacity);
                route.setPathPoints(pathPoints);
                
                routes.add(route);
            }
        }
        
        // Handle unassigned nodes
        List<Node> unscheduledNodes = new ArrayList<>();
        for (int i = 1; i < nodes.size(); i++) {
            if (!assignedNodes.contains(i)) {
                unscheduledNodes.add(nodes.get(i));
            }
        }
        
        // Set solution properties
        solution.setRoutes(routes);
        solution.setUnscheduledNodes(unscheduledNodes);
        solution.setSolverTime((System.currentTimeMillis() - startTime) / 1000.0);
        solution.setSolverIterations(1);
        solution.calculateMetrics(input);
        
        return solution;
    }

    @Override
    public String getName() {
        return "Greedy Best Insertion";
    }
}
