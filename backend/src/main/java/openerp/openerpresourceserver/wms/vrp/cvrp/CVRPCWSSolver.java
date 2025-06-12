package openerp.openerpresourceserver.wms.vrp.cvrp;

import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.vrp.*;

import java.util.*;

@Slf4j
public class CVRPCWSSolver implements CVRPSolver {

    @Override
    public CVRPSolution solve(CVRPInput input, CVRPParams params) {
        long startTime = System.currentTimeMillis();

        List<Node> nodes = input.getNodes();
        List<Vehicle> vehicles = input.getVehicles();

        if (nodes.isEmpty() || vehicles.isEmpty()) {
            return CVRPSolution.createEmpty();
        }

        // Step 1: Calculate savings for all pairs of customers
        List<SavingsPair> savingsList = calculateSavings(input);

        // Step 2: Sort savings in descending order
        savingsList.sort(Comparator.comparingDouble(SavingsPair::getSavings).reversed());

        // Step 3: Initialize individual routes for each customer (depot -> customer -> depot)
        Map<Integer, Route> customerToRoute = new HashMap<>();
        List<Route> routes = new ArrayList<>();

        for (int i = 1; i < nodes.size(); i++) {
            Route route = new Route();
            route.customers.add(i);
            route.load = nodes.get(i).getDemand();
            route.distance = 2 * input.getDistance(0, i); // depot -> customer -> depot

            customerToRoute.put(i, route);
            routes.add(route);
        }

        // Step 4: Apply Clarke-Wright savings algorithm
        for (SavingsPair saving : savingsList) {
            int customer1 = saving.getCustomer1();
            int customer2 = saving.getCustomer2();

            Route route1 = customerToRoute.get(customer1);
            Route route2 = customerToRoute.get(customer2);

            // Skip if customers are already in the same route
            if (route1 == route2) {
                continue;
            }

            // Check if routes can be merged
            if (canMergeRoutes(route1, route2, customer1, customer2, input, params)) {
                Route mergedRoute = mergeRoutes(route1, route2, customer1, customer2, input);

                // Update customer-to-route mapping
                for (int customer : mergedRoute.customers) {
                    customerToRoute.put(customer, mergedRoute);
                }

                // Remove old routes and add merged route
                routes.remove(route1);
                routes.remove(route2);
                routes.add(mergedRoute);
            }
        }

        // Step 5: Convert routes to VRP solution format
        CVRPSolution solution = convertToVRPSolution(routes, input, vehicles);

        // Set solution properties
        solution.setSolverTime((System.currentTimeMillis() - startTime) / 1000.0);
        solution.setSolverIterations(savingsList.size());
        solution.calculateMetrics(input);

        return solution;
    }

    private List<SavingsPair> calculateSavings(CVRPInput input) {
        List<SavingsPair> savingsList = new ArrayList<>();
        List<Node> nodes = input.getNodes();

        for (int i = 1; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                double dist0i = input.getDistance(0, i);
                double dist0j = input.getDistance(0, j);
                double distij = input.getDistance(i, j);

                // Savings = distance(depot, i) + distance(depot, j) - distance(i, j)
                double savings = dist0i + dist0j - distij;

                if (savings > 0) {
                    savingsList.add(new SavingsPair(i, j, savings));
                }
            }
        }

        return savingsList;
    }

    private boolean canMergeRoutes(Route route1, Route route2, int customer1, int customer2,
                                   CVRPInput input, CVRPParams params) {
        // Check capacity constraint
        if (params.isUseCapacityConstraints() &&
                route1.load + route2.load > getMaxVehicleCapacity(input.getVehicles())) {
            return false;
        }

        // Check if customers are at the ends of their respective routes
        boolean customer1AtEnd = (route1.customers.get(0) == customer1 ||
                route1.customers.get(route1.customers.size() - 1) == customer1);
        boolean customer2AtEnd = (route2.customers.get(0) == customer2 ||
                route2.customers.get(route2.customers.size() - 1) == customer2);

        return customer1AtEnd && customer2AtEnd;
    }

    private Route mergeRoutes(Route route1, Route route2, int customer1, int customer2, CVRPInput input) {
        Route mergedRoute = new Route();

        // Determine the order of merging based on customer positions
        List<Integer> newCustomers = new ArrayList<>();

        if (route1.customers.get(route1.customers.size() - 1) == customer1 &&
                route2.customers.get(0) == customer2) {
            // route1 -> route2
            newCustomers.addAll(route1.customers);
            newCustomers.addAll(route2.customers);
        } else if (route1.customers.get(0) == customer1 &&
                route2.customers.get(route2.customers.size() - 1) == customer2) {
            // route2 -> route1
            newCustomers.addAll(route2.customers);
            newCustomers.addAll(route1.customers);
        } else if (route1.customers.get(route1.customers.size() - 1) == customer1 &&
                route2.customers.get(route2.customers.size() - 1) == customer2) {
            // route1 -> reverse(route2)
            newCustomers.addAll(route1.customers);
            List<Integer> reversedRoute2 = new ArrayList<>(route2.customers);
            Collections.reverse(reversedRoute2);
            newCustomers.addAll(reversedRoute2);
        } else if (route1.customers.get(0) == customer1 &&
                route2.customers.get(0) == customer2) {
            // reverse(route1) -> route2
            List<Integer> reversedRoute1 = new ArrayList<>(route1.customers);
            Collections.reverse(reversedRoute1);
            newCustomers.addAll(reversedRoute1);
            newCustomers.addAll(route2.customers);
        }

        mergedRoute.customers = newCustomers;
        mergedRoute.load = route1.load + route2.load;
        mergedRoute.distance = calculateRouteDistance(newCustomers, input);

        return mergedRoute;
    }

    private double calculateRouteDistance(List<Integer> customers, CVRPInput input) {
        if (customers.isEmpty()) {
            return 0.0;
        }

        double distance = 0.0;

        // Depot to first customer
        distance += input.getDistance(0, customers.get(0));

        // Between customers
        for (int i = 0; i < customers.size() - 1; i++) {
            distance += input.getDistance(customers.get(i), customers.get(i + 1));
        }

        // Last customer to depot
        distance += input.getDistance(customers.get(customers.size() - 1), 0);

        return distance;
    }

    private double getMaxVehicleCapacity(List<Vehicle> vehicles) {
        return vehicles.stream()
                .mapToDouble(Vehicle::getCapacity)
                .max()
                .orElse(Double.MAX_VALUE);
    }

    private CVRPSolution convertToVRPSolution(List<Route> routes, CVRPInput input, List<Vehicle> vehicles) {
        CVRPSolution solution = CVRPSolution.createEmpty();
        List<VRPRoute> vrpRoutes = new ArrayList<>();
        List<Node> unscheduledNodes = new ArrayList<>();

        int vehicleIndex = 0;

        for (Route route : routes) {
            if (route.customers.isEmpty()) {
                continue;
            }

            if (vehicleIndex >= vehicles.size()) {
                // No more vehicles available, mark remaining customers as unscheduled
                for (int customer : route.customers) {
                    unscheduledNodes.add(input.getNodes().get(customer));
                }
                continue;
            }

            Vehicle vehicle = vehicles.get(vehicleIndex);

            // Check capacity constraint
            if (route.load > vehicle.getCapacity()) {
                // Route exceeds vehicle capacity, mark customers as unscheduled
                for (int customer : route.customers) {
                    unscheduledNodes.add(input.getNodes().get(customer));
                }
                continue;
            }

            VRPRoute vrpRoute = VRPRoute.createEmpty(vehicleIndex);

            // Build node sequence: depot -> customers -> depot
            List<Integer> nodeSequence = new ArrayList<>();
            nodeSequence.add(0); // Start at depot
            nodeSequence.addAll(route.customers);
            nodeSequence.add(0); // Return to depot

            vrpRoute.setNodeSequence(nodeSequence);
            vrpRoute.setDistance(route.distance);
            vrpRoute.setLoad(route.load);

            // Calculate duration and build path points
            double duration = calculateRouteDuration(nodeSequence, input);
            vrpRoute.setDuration(duration);

            // **FIX: Calculate and set path points like in GreedySolver**
            List<GeoPoint> pathPoints = calculateRoutePathPoints(nodeSequence, input);
            vrpRoute.setPathPoints(pathPoints);

            vrpRoutes.add(vrpRoute);
            vehicleIndex++;
        }

        solution.setRoutes(vrpRoutes);
        solution.setUnscheduledNodes(unscheduledNodes);

        return solution;
    }

    /**
     * Calculate path points for the entire route
     */
    private List<GeoPoint> calculateRoutePathPoints(List<Integer> nodeSequence, CVRPInput input) {
        List<GeoPoint> pathPoints = new ArrayList<>();

        for (int i = 0; i < nodeSequence.size() - 1; i++) {
            int fromNodeId = nodeSequence.get(i);
            int toNodeId = nodeSequence.get(i + 1);

            boolean isReturnToDepot = (toNodeId == 0 && i == nodeSequence.size() - 2);
            if (isReturnToDepot) {
                continue;
            }

            TimeDistance td = input.findTimeDistance(fromNodeId, toNodeId);
            if (td != null && td.getPath() != null) {
                // Add all path points for this segment
                if (i == 0) {
                    // For first segment, add all points
                    pathPoints.addAll(td.getPath());
                } else {
                    // For subsequent segments, skip the first point to avoid duplicates
                    List<GeoPoint> segmentPath = td.getPath();
                    if (segmentPath.size() > 1) {
                        pathPoints.addAll(segmentPath.subList(1, segmentPath.size()));
                    }
                }
            } else {
                // Fallback: add direct connection if no path data available
                Node fromNode = input.getNodes().get(fromNodeId);
                Node toNode = input.getNodes().get(toNodeId);

                if (i == 0 || pathPoints.isEmpty()) {
                    pathPoints.add(new GeoPoint(fromNode.getLatitude(), fromNode.getLongitude()));
                }
                pathPoints.add(new GeoPoint(toNode.getLatitude(), toNode.getLongitude()));
            }
        }

        return pathPoints;
    }

    private double calculateRouteDuration(List<Integer> nodeSequence, CVRPInput input) {
        double duration = 0.0;

        for (int i = 0; i < nodeSequence.size() - 1; i++) {
            TimeDistance td = input.findTimeDistance(nodeSequence.get(i), nodeSequence.get(i + 1));
            if (td != null) {
                duration += td.getTravelTime();
            }

            // Add service time for non-depot nodes
            if (nodeSequence.get(i + 1) != 0) {
                duration += input.getNodes().get(nodeSequence.get(i + 1)).getServiceTime();
            }
        }

        return duration;
    }

    @Override
    public String getName() {
        return "Clarke-Wright Savings";
    }

    // Helper classes
    private static class SavingsPair {
        private final int customer1;
        private final int customer2;
        private final double savings;

        public SavingsPair(int customer1, int customer2, double savings) {
            this.customer1 = customer1;
            this.customer2 = customer2;
            this.savings = savings;
        }

        public int getCustomer1() { return customer1; }
        public int getCustomer2() { return customer2; }
        public double getSavings() { return savings; }
    }

    private static class Route {
        List<Integer> customers = new ArrayList<>();
        double load = 0.0;
        double distance = 0.0;
    }
}