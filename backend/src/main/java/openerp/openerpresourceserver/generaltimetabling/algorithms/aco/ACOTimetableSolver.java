package openerp.openerpresourceserver.generaltimetabling.algorithms.aco;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Solver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;

import java.util.*;

@Log4j2
public class ACOTimetableSolver implements Solver {

    MapDataScheduleTimeSlotRoom I;
    Map<Integer, Integer> solutionSlot = new HashMap<>();
    Map<Integer, Integer> solutionRoom = new HashMap<>();
    int timeLimit;
    boolean foundSolution = false;
    Random rand = new Random();

    public ACOTimetableSolver(MapDataScheduleTimeSlotRoom I) {
        this.I = I;
    }

    @Override
    public void solve() {
        int numAnts = 10;
        int maxIterations = 50;
        double alpha = 1.0; // Importance of pheromone trail.
        double beta = 2.0;  // Importance of heuristic information.
        double evaporation = 0.1;
        double pheromoneDeposit = 10.0;

        List<ClassSegment> classSegments = I.getClassSegments();
        Map<Integer, Map<Integer, Map<Integer, Double>>> pheromones = initializePheromoneMatrix(classSegments);

        Map<Integer, Integer> bestSlot = new HashMap<>();
        Map<Integer, Integer> bestRoom = new HashMap<>();
        int bestScore = Integer.MAX_VALUE;

        Set<Integer> usedRooms = new HashSet<>();

        for (int iter = 0; iter < maxIterations; iter++) {
            for (int a = 0; a < numAnts; a++) {
                Map<Integer, Integer> antSlot = new HashMap<>();
                Map<Integer, Integer> antRoom = new HashMap<>();

                for (ClassSegment cs : classSegments) {
                    int s = selectTimeSlot(cs, pheromones, alpha, beta, antSlot);
                    int r = selectRoom(cs, s, pheromones, alpha, beta, usedRooms);

                    if (s != -1 && r != -1 && isAssignable(cs, s, r, antSlot, antRoom)) {
                        antSlot.put(cs.getId(), s);
                        antRoom.put(cs.getId(), r);
                        usedRooms.add(r);
                    }
                }

                int score = evaluate(antSlot, antRoom);
                log.info(iter + "th iteration " + a + "th ant: " + score);
                if (score < bestScore && antSlot.size() == classSegments.size()) {
                    bestScore = score;
                    bestSlot = new HashMap<>(antSlot);
                    bestRoom = new HashMap<>(antRoom);
                    foundSolution = true;
                }
            }

            evaporatePheromones(pheromones, evaporation);
            depositPheromones(pheromones, bestSlot, bestRoom, pheromoneDeposit);
        }

        solutionSlot = bestSlot;
        solutionRoom = bestRoom;
    }

    private Map<Integer, Map<Integer, Map<Integer, Double>>> initializePheromoneMatrix(List<ClassSegment> classSegments) {
        Map<Integer, Map<Integer, Map<Integer, Double>>> matrix = new HashMap<>();
        for (ClassSegment cs : classSegments) {
            matrix.putIfAbsent(cs.getId(), new HashMap<>());
            for (int slot : cs.getDomainTimeSlots()) {
                matrix.get(cs.getId()).putIfAbsent(slot, new HashMap<>());
                for (int room : cs.getDomainRooms()) {
                    matrix.get(cs.getId()).get(slot).put(room, 1.0);
                }
            }
        }
        return matrix;
    }

    private int selectTimeSlot(ClassSegment cs, Map<Integer, Map<Integer, Map<Integer, Double>>> pheromones, double alpha, double beta, Map<Integer, Integer> currentSlotAssignments) {
        List<Integer> domain = cs.getDomainTimeSlots();
        List<int[]> assignedSlots = new ArrayList<>(); // [start, duration]

        // Collect assigned slots and the segments' duration from the same course
        for (Map.Entry<Integer, Integer> entry : currentSlotAssignments.entrySet()) {
            ClassSegment other = I.getClassSegments().stream()
                    .filter(seg -> seg.getId() == entry.getKey())
                    .findFirst()
                    .orElse(null);
            if (other != null && other.getCourseIndex() == cs.getCourseIndex()) {
                assignedSlots.add(new int[]{entry.getValue(), other.getDuration()});
            }
        }

        Map<Integer, Double> probs = new HashMap<>();
        double sum = 0.0;

        for (int s : domain) {
            boolean overlaps = false;

            for (int[] assigned : assignedSlots) {
                int aStart = assigned[0];
                int aDur = assigned[1];

                if (Util.overLap(s, cs.getDuration(), aStart, aDur)) {
                    overlaps = true;
                    break;
                }
            }

            if (overlaps) continue;

            // Compute min distance to any assigned slot in same course
            int minDistance = assignedSlots.isEmpty()
                    ? 1
                    : assignedSlots.stream()
                    .mapToInt(a -> {
                        int aStart = a[0];
                        int aEnd = aStart + a[1] - 1;
                        int csEnd = s + cs.getDuration() - 1;
                        return Math.min(Math.abs(s - aEnd), Math.abs(csEnd - aStart));
                    })
                    .min().orElse(1);

            double pheromone = 0;
            // How attractive is it to schedule this segment in time slot s regardless of room, based on prior ant experiences?
            for (int r : cs.getDomainRooms()) {
                pheromone += pheromones.get(cs.getId()).getOrDefault(s, new HashMap<>()).getOrDefault(r, 0.1);
            }
            double heuristic = 1.0 / (1.0 + minDistance);
            double value = Math.pow(pheromone, alpha) * Math.pow(heuristic, beta);
            probs.put(s, value);
            sum += value;
        }

        if (probs.isEmpty()) return domain.get(0); // fallback

        double r = rand.nextDouble() * sum;
        double cum = 0.0;
        for (Map.Entry<Integer, Double> entry : probs.entrySet()) {
            cum += entry.getValue();
            if (r <= cum) return entry.getKey();
        }

        return domain.get(0); // fallback
    }

    private int selectRoom(ClassSegment cs, int s, Map<Integer, Map<Integer, Map<Integer, Double>>> pheromones, double alpha, double beta, Set<Integer> usedRooms) {
        List<Integer> rooms = cs.getDomainRooms();
        double sum = 0.0;
        Map<Integer, Double> probs = new HashMap<>();

        for (int r : rooms) {
            double pheromone = pheromones.get(cs.getId()).getOrDefault(s, new HashMap<>()).getOrDefault(r, 0.1);
            double cap = I.getRoomCapacity()[r];
            double capMatch = (cap >= cs.getNbStudents() && cap <= cs.getNbStudents() * 1.5) ? 1.0 : 0.1;
            double reuseBoost = 1.0 + (usedRooms.contains(r) ? 1.0 : 0.0);

            double heuristic = capMatch * reuseBoost;
            double val = Math.pow(pheromone, alpha) * Math.pow(heuristic, beta);
            probs.put(r, val);
            sum += val;
        }

        double rr = rand.nextDouble() * sum;
        double cum = 0.0;
        for (int r : rooms) {
            cum += probs.get(r);
            if (rr <= cum) return r;
        }
        return rooms.get(0);
    }

    private boolean isAssignable(ClassSegment cs, int s, int r, Map<Integer, Integer> slots, Map<Integer, Integer> rooms) {
        // 1. Check room availability at the desired time slots
        for (int i = 0; i < cs.getDuration(); i++) {
            if (I.getRoomOccupations()[r].contains(s + i)) return false;
        }

        for (ClassSegment other : I.getClassSegments()) {
            if (!slots.containsKey(other.getId())) continue;

            int so = slots.get(other.getId());

            // 2. Conflict list check
            if (Util.overLap(s, cs.getDuration(), so, other.getDuration()) &&
                    I.getConflict().stream().anyMatch(pair ->
                            (pair[0] == cs.getId() && pair[1] == other.getId()) ||
                                    (pair[1] == cs.getId() && pair[0] == other.getId()))) {
                return false;
            }

            // 3. Same class segment cannot overlap (e.g., Lecture and Lab of same group)
            if (cs.getClassId().equals(other.getClassId()) &&
                    Util.overLap(s, cs.getDuration(), so, other.getDuration())) {
                return false;
            }

            // 4. Segments from the same course must not overlap
            if (cs.getCourseIndex() == other.getCourseIndex() &&
                    Util.overLap(s, cs.getDuration(), so, other.getDuration())) {
                return false;
            }
        }

        return true;
    }

    private void evaporatePheromones(Map<Integer, Map<Integer, Map<Integer, Double>>> pheromones, double evaporation) {
        for (Map<Integer, Map<Integer, Double>> m1 : pheromones.values()) {
            for (Map<Integer, Double> m2 : m1.values()) {
                for (Integer k : m2.keySet()) {
                    m2.put(k, m2.get(k) * (1 - evaporation));
                }
            }
        }
    }

    private void depositPheromones(Map<Integer, Map<Integer, Map<Integer, Double>>> pheromones, Map<Integer, Integer> slotMap, Map<Integer, Integer> roomMap, double amount) {
        for (Integer segId : slotMap.keySet()) {
            int s = slotMap.get(segId);
            int r = roomMap.get(segId);
            pheromones.get(segId).get(s).put(r, pheromones.get(segId).get(s).get(r) + amount);
        }
    }

    private int evaluate(Map<Integer, Integer> slotMap, Map<Integer, Integer> roomMap) {
        int totalSegments = I.getClassSegments().size();
        int assignedSegments = slotMap.size();
        int unassignedPenalty = totalSegments - assignedSegments;

        // Count how many distinct rooms were used
        Set<Integer> usedRooms = new HashSet<>(roomMap.values());
        int roomPenalty = usedRooms.size();

        // Total score = unassigned penalty + room penalty (weight can be tuned)
        return unassignedPenalty * 100 + roomPenalty;
    }

    @Override
    public boolean hasSolution() {
        return foundSolution;
    }

    @Override
    public Map<Integer, Integer> getMapSolutionSlot() {
        return solutionSlot;
    }

    @Override
    public Map<Integer, Integer> getMapSolutionRoom() {
        return solutionRoom;
    }

    @Override
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public void printSolution() {
        for (Integer id : solutionSlot.keySet()) {
            log.info("Segment " + id + " -> slot " + solutionSlot.get(id) + ", room " + solutionRoom.get(id));
        }
    }

    @Override
    public String name() {
        return "ACOTimetableSolver";
    }
}
