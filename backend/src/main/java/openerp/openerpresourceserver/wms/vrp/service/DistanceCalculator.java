package openerp.openerpresourceserver.wms.vrp.service;

import openerp.openerpresourceserver.wms.vrp.GeoPoint;
import openerp.openerpresourceserver.wms.vrp.Node;
import openerp.openerpresourceserver.wms.vrp.TimeDistance;

import java.util.List;

/**
 * Interface for distance calculation strategies
 */
public interface DistanceCalculator {
    /**
     * Calculate the distance and travel time between two points
     */
    TimeDistance calculateDistance(Node from, Node to);
    
    /**
     * Get the detailed path between two points
     */
    List<GeoPoint> getRoutePath(Node from, Node to);
    
    /**
     * Calculate distance matrix for a set of nodes
     */
    List<TimeDistance> calculateDistanceMatrix(List<Node> nodes);
}