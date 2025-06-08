package openerp.openerpresourceserver.generaltimetabling.algorithms.ga;

import openerp.openerpresourceserver.generaltimetabling.algorithms.util.*;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class GeneticAlgorithmSolver {
    // GA Parameters
    private final int populationSize;
    private final int maxGenerations;
    private final double mutationRate;
    private final double crossoverRate;
    private static final double PENALTY_RATE = 10;

    // Problem Data
    private int nbSlotPerSession;
    private int nbSessions;
    private List<AClass> classes;
    private Map<String, List<AClass>> mCourse2Classes;
    private List<String> courses;
    private int totalClasses;

    // Best Solution Found
    private Map<Integer, Integer> bestSolutionSlot = new HashMap<>();
    private boolean foundSolution = false;

    // Metrics tracking
    private int generationsUntilBest;
    private double bestFitness;

    public GeneticAlgorithmSolver() {
        this(500, 750, 0.4, 0.75); // Default parameters
    }

    public GeneticAlgorithmSolver(int populationSize, int maxGenerations, double mutationRate, double crossoverRate) {
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;

        this.classes = new ArrayList<>();
        this.mCourse2Classes = new HashMap<>();
        this.courses = new ArrayList<>();
        this.generationsUntilBest = -1;
        this.bestFitness = Double.NEGATIVE_INFINITY;
    }

    public int getGenerationsUntilBest() {
        return generationsUntilBest;
    }

    public void inputFile(String filename) {
        System.out.println("Loading input file...");
        try {
            java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(new java.io.FileInputStream(filename)));

            String[] s = in.readLine().split(" ");
            nbSlotPerSession = Integer.parseInt(s[0]);
            nbSessions = Integer.parseInt(s[1]);

            int nbClasses = Integer.parseInt(in.readLine());
            int id = 0;

            for (int i = 0; i < nbClasses; i++) {
                s = in.readLine().split(" ");
                int classId = Integer.parseInt(s[0]);
                String course = s[1];
                int nbSeg = Integer.parseInt(s[2]);
                List<AClassSegment> segs = new ArrayList<>();

                for (int j = 1; j <= nbSeg; j++) {
                    int dur = Integer.parseInt(s[2 + j]);
                    AClassSegment seg = new AClassSegment(++id, course, dur);
                    segs.add(seg);
                }

                AClass cls = new AClass(classId, course, segs);
                classes.add(cls);
                mCourse2Classes.computeIfAbsent(course, k -> new ArrayList<>()).add(cls);
            }

            courses = new ArrayList<>(mCourse2Classes.keySet());
            totalClasses = classes.size();
            in.close();
        } catch (Exception e) {
            System.out.println("Error reading input file");
        }
    }

    public void solve() {
        // Initialize population
        List<Chromosome> population = initializePopulation();
        Chromosome bestChromosome = null;
        int generation = 0;

        while (generation < maxGenerations) {
            // Evaluate fitness
            evaluatePopulation(population);

            // Sort by fitness
            population.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));

            // Update the best solution if needed
            if (bestChromosome == null || population.get(0).getFitness() > bestChromosome.getFitness()) {
                bestChromosome = new Chromosome(population.get(0));
                bestFitness = bestChromosome.getFitness();
                generationsUntilBest = generation;
                System.out.println("Generation " + generation + ": New best fitness = " + bestChromosome.getFitness());
            }

            // Create new population
            List<Chromosome> newPopulation = new ArrayList<>();
            // Elitism - keep best 10% of population
            int eliteSize = populationSize / 10;
            for (int i = 0; i < eliteSize; i++) {
                newPopulation.add(new Chromosome(population.get(i)));
            }

            // Crossover and Mutation
            while (newPopulation.size() < populationSize) {
                // Tournament Selection
                Chromosome parent1 = tournamentSelection(population);
                Chromosome parent2 = tournamentSelection(population);

                // Crossover
                Chromosome child1 = new Chromosome(parent1);
                Chromosome child2 = new Chromosome(parent2);

                if (Math.random() < crossoverRate) {
                    crossover(child1, child2);
                }

                // Mutation
                if (Math.random() < mutationRate) {
                    mutate(child1);
                }
                if (Math.random() < mutationRate) {
                    mutate(child2);
                }

                newPopulation.add(child1);
                if (newPopulation.size() < populationSize) {
                    newPopulation.add(child2);
                }
            }

            population = newPopulation;
            generation++;
        }

        // Set best solution found
        if (bestChromosome != null) {
            bestSolutionSlot = bestChromosome.getSlotAssignments();
            foundSolution = true;
        }
    }

    private List<Chromosome> initializePopulation() {
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(generateRandomChromosome());
        }
        return population;
    }

    private Chromosome generateRandomChromosome() {
        Map<Integer, Integer> slotAssignments = new HashMap<>();
        Random rand = new Random();

        for (AClass cls : classes) {
            for (AClassSegment seg : cls.classSegments) {
                // Find valid slot for segment
                List<Integer> validSlots = new ArrayList<>();
                for (int session = 0; session < nbSessions; session++) {
                    for (int slot = 1; slot <= nbSlotPerSession - seg.duration + 1; slot++) {
                        boolean valid = true;
                        // Check if slots are available
                        for (int d = 0; d < seg.duration; d++) {
                            if (isSlotOccupied(session, slot + d, slotAssignments, seg)) {
                                valid = false;
                                break;
                            }
                        }
                        if (valid) {
                            validSlots.add(session * nbSlotPerSession + slot);
                        }
                    }
                }

                if (!validSlots.isEmpty()) {
                    int selectedSlot = validSlots.get(rand.nextInt(validSlots.size()));
                    slotAssignments.put(seg.id, selectedSlot);
                }
            }
        }

        return new Chromosome(slotAssignments);
    }

    private boolean isSlotOccupied(int session, int slot, Map<Integer, Integer> assignments, AClassSegment currentSegment) {
        // assignments = current assignment
        // Find which class the current segment belongs to
        AClass currentClass = findClassForSegment(currentSegment);
        if (currentClass == null) return false;
        // Only check segments from the same class
        for (Map.Entry<Integer, Integer> entry : assignments.entrySet()) {
            AClassSegment seg = findSegmentById(entry.getKey());
            // Skip if not from the same class
            if (!findClassForSegment(seg).equals(currentClass)) continue;

            int assignedSlot = entry.getValue();
            int assignedSession = assignedSlot / nbSlotPerSession;
            int assignedSlotInSession = assignedSlot % nbSlotPerSession;

            if (session == assignedSession) {
                if (slot >= assignedSlotInSession && slot < assignedSlotInSession + seg.duration) {
                    return true;
                }
            }
        }
        return false;
    }

    private AClass findClassForSegment(AClassSegment segment) {
        for (AClass cls : classes) {
            if (cls.classSegments.contains(segment)) {
                return cls;
            }
        }
        return null;
    }

    private void evaluatePopulation(List<Chromosome> population) {
        for (Chromosome chromosome : population) {
            if (!chromosome.isEvaluated()) {
                evaluateChromosome(chromosome);
            }
        }
    }

    private void evaluateChromosome(Chromosome chromosome) {
        int totalSegments = 0;
        for (AClass cls : classes) {
            totalSegments += cls.classSegments.size();
        }
        double fitness = 0.0;

        // 1. Check if all segments are assigned
        int assignedSegments = chromosome.getSlotAssignments().size();
        double completeness = (double) assignedSegments / totalSegments;
        fitness += completeness;

        // 2. Check combination validity (HARD CONSTRAINT)
        int numClassesNotInCombination = cntClassesNotInCombination(chromosome);
        fitness -= numClassesNotInCombination * PENALTY_RATE;

        // 3. Calculate teacher count using MaxClique only if combination is valid
        double teacherFitness = 0.0;
        if (numClassesNotInCombination == 0) {  // Only if all classes are in valid combinations
            int teacherCount = calculateTeacherCount(chromosome);
            teacherFitness = 1.0 / (1.0 + teacherCount);
            fitness += teacherFitness;
        }

        // 4. Calculate course compactness and overlap penalty
        double compactness = calculateCompactness(chromosome);
        fitness += compactness;

        System.out.println("Fitness calculation: completeness=" + completeness +
                ", numClassesNotInCombination=" + numClassesNotInCombination + ", teacherFitness=" + teacherFitness +
                ", compactness=" + compactness + ", totalFitness=" + fitness);

        chromosome.setFitness(fitness);
        chromosome.setEvaluated(true);
    }


    private double calculateCompactness(Chromosome chromosome) {
        double totalCompactness = 0.0;
        // Group segments by course and session
        Map<String, Map<Integer, List<Integer>>> courseSessionSlots = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : chromosome.getSlotAssignments().entrySet()) {
            AClassSegment seg = findSegmentById(entry.getKey());
            int slot = entry.getValue();
            int session = slot / nbSlotPerSession;

            courseSessionSlots.computeIfAbsent(seg.getCourse(), k -> new HashMap<>())
                    .computeIfAbsent(session, k -> new ArrayList<>())
                    .add(slot % nbSlotPerSession);
        }

        for (Map.Entry<String, Map<Integer, List<Integer>>> courseEntry : courseSessionSlots.entrySet()) {
            // For each session in this course
            for (Map.Entry<Integer, List<Integer>> sessionEntry : courseEntry.getValue().entrySet()) {
                int currentSession = sessionEntry.getKey();
                List<Integer> slotsInSession = sessionEntry.getValue();
                if (slotsInSession.size() <= 1) continue;
                // Sort slots within session
                Collections.sort(slotsInSession);
                // Check for consecutive slots
                for (int i = 0; i < slotsInSession.size(); i++) {
                    // Find the segment at this slot
                    AClassSegment currentSeg = findSegmentBySlotInCourse(
                            slotsInSession.get(i),
                            currentSession,
                            courseEntry.getKey(),
                            chromosome
                    );
                    if (currentSeg == null) continue;
                    // Check with next slot for consecutiveness
                    if (i < slotsInSession.size() - 1) {
                        AClassSegment nextSeg = findSegmentBySlotInCourse(
                                slotsInSession.get(i + 1),
                                currentSession,
                                courseEntry.getKey(),
                                chromosome
                        );
                        if (nextSeg != null) {
                            // Check if consecutive
                            if (slotsInSession.get(i) + currentSeg.duration == slotsInSession.get(i + 1)) {
                                totalCompactness += 1; // Bonus for consecutive slots
                            }
                        }
                    }
                }
            }
        }

        return totalCompactness;
    }

    private AClassSegment findSegmentBySlotInCourse(int slotInSession, int session, String course, Chromosome chromosome) {
        for (Map.Entry<Integer, Integer> entry : chromosome.getSlotAssignments().entrySet()) {
            AClassSegment seg = findSegmentById(entry.getKey());
            int absoluteSlot = entry.getValue();
            int segmentSession = absoluteSlot / nbSlotPerSession;
            int relativeSlot = absoluteSlot % nbSlotPerSession;

            if (seg.getCourse().equals(course) &&
                    relativeSlot == slotInSession &&
                    segmentSession == session) {
                return seg;
            }
        }
        return null;
    }

    public int calculateTeacherCount(Chromosome chromosome) {
        int totalTeachers = 0;
        Map<String, Map<AClass, SolutionClass>> courseSolutions = new HashMap<>();

        // Group solutions by course
        for (Map.Entry<Integer, Integer> entry : chromosome.getSlotAssignments().entrySet()) {
            AClassSegment seg = findSegmentById(entry.getKey());
            String course = seg.getCourse();
            // Find the AClass this segment belongs to
            for (AClass cls : classes) {
                if (cls.course.equals(course)) {
                    for (AClassSegment classSeg : cls.classSegments) {
                        if (classSeg.id == seg.id) {
                            // Create SolutionClass for this assignment
                            List<int[]> periods = new ArrayList<>();
                            int slot = entry.getValue();
                            periods.add(new int[]{slot % nbSlotPerSession,
                                    slot % nbSlotPerSession + seg.duration - 1,
                                    slot / nbSlotPerSession});

                            SolutionClass sc = new SolutionClass(cls, periods);
                            courseSolutions.computeIfAbsent(course, k -> new HashMap<>())
                                    .put(cls, sc);
                            break;
                        }
                    }
                }
            }
        }
        // Calculate max clique for each course
        for (String course : courses) {
            Map<AClass, SolutionClass> courseSolution = courseSolutions.get(course);
            if (courseSolution != null && !courseSolution.isEmpty()) {
                MaxClique maxClique = new MaxClique();
                int teachersNeeded = maxClique.computeMaxClique(courseSolution);
                totalTeachers += teachersNeeded;
            }
        }
        return totalTeachers;
    }

    private int cntClassesNotInCombination(Chromosome chromosome) {
        int numClassesNotInCombination = 0;
        // Convert chromosome assignments to util.SolutionClass array
        SolutionClass[] solutionClasses = new SolutionClass[classes.size()];
        // Create solution classes from chromosome assignments
        for (int i = 0; i < classes.size(); i++) {
            AClass cls = classes.get(i);
            List<int[]> periods = new ArrayList<>();
            // Get all assigned slots for this class's segments
            for (AClassSegment seg : cls.classSegments) {
                Integer slot = chromosome.getSlotAssignments().get(seg.id);
                if (slot != null) {
                    int session = slot / nbSlotPerSession;
                    int slotInSession = slot % nbSlotPerSession;
                    periods.add(new int[]{slotInSession, slotInSession + seg.duration - 1, session});
                }
            }
            solutionClasses[i] = new SolutionClass(cls, periods);
        }
        // Create classIndicesOfCourse
        @SuppressWarnings("unchecked")
        List<Integer>[] classIndicesOfCourse = new List[courses.size()];
        for (int i = 0; i < courses.size(); i++) {
            classIndicesOfCourse[i] = new ArrayList<>();
        }
        // Populate classIndicesOfCourse
        for (int i = 0; i < classes.size(); i++) {
            AClass cls = classes.get(i);
            int courseIndex = courses.indexOf(cls.course);
            classIndicesOfCourse[courseIndex].add(i);
        }
        // Create and use util.CombinationChecker
        CombinationChecker checker = new CombinationChecker(classIndicesOfCourse, classes, solutionClasses);
        // Check each class
        for (int i = 0; i < classes.size(); i++) {
            if (!checker.checkInCombination(i)) {
                numClassesNotInCombination++;
            }
        }
        return numClassesNotInCombination;
    }

    public boolean isValidCombination(Chromosome chromosome) {
        // Convert chromosome assignments to util.SolutionClass array
        SolutionClass[] solutionClasses = new SolutionClass[classes.size()];
        // Create solution classes from chromosome assignments
        for (int i = 0; i < classes.size(); i++) {
            AClass cls = classes.get(i);
            List<int[]> periods = new ArrayList<>();
            // Get all assigned slots for this class's segments
            for (AClassSegment seg : cls.classSegments) {
                Integer slot = chromosome.getSlotAssignments().get(seg.id);
                if (slot != null) {
                    int session = slot / nbSlotPerSession;
                    int slotInSession = slot % nbSlotPerSession;
                    periods.add(new int[]{slotInSession, slotInSession + seg.duration - 1, session});
                }
            }
            solutionClasses[i] = new SolutionClass(cls, periods);
        }
        // Create classIndicesOfCourse
        @SuppressWarnings("unchecked")
        List<Integer>[] classIndicesOfCourse = new List[courses.size()];
        for (int i = 0; i < courses.size(); i++) {
            classIndicesOfCourse[i] = new ArrayList<>();
        }
        // Populate classIndicesOfCourse
        for (int i = 0; i < classes.size(); i++) {
            AClass cls = classes.get(i);
            int courseIndex = courses.indexOf(cls.course);
            classIndicesOfCourse[courseIndex].add(i);
        }
        // Create and use util.CombinationChecker
        CombinationChecker checker = new CombinationChecker(classIndicesOfCourse, classes, solutionClasses);
        // Check each class
        for (int i = 0; i < classes.size(); i++) {
            if (!checker.checkInCombination(i)) {
                return false;
            }
        }
        return true;
    }
    private Chromosome tournamentSelection(List<Chromosome> population) {
        int tournamentSize = 5;
        List<Chromosome> tournament = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(population.get(rand.nextInt(population.size())));
        }

        tournament.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));
        return tournament.get(0);
    }

    private void crossover(Chromosome c1, Chromosome c2) {
        // Implement order crossover (OX)
        int totalSegments = 0;
        for (AClass cls : classes) {
            totalSegments += cls.classSegments.size();
        }
        Random rand = new Random();
        int point1 = rand.nextInt(totalSegments);
        int point2 = rand.nextInt(totalSegments - point1) + point1;

        Map<Integer, Integer> temp1 = new HashMap<>(c1.getSlotAssignments());
        Map<Integer, Integer> temp2 = new HashMap<>(c2.getSlotAssignments());

        // Swap assignments between crossover points
        for (int i = point1; i <= point2; i++) {
            int segId = i + 1; // Segment IDs start from 1
            Integer slot1 = temp1.get(segId);
            Integer slot2 = temp2.get(segId);

            if (slot1 != null && slot2 != null) {
                c1.getSlotAssignments().put(segId, slot2);
                c2.getSlotAssignments().put(segId, slot1);
            }
        }
        c1.setEvaluated(false);
        c2.setEvaluated(false);
    }

    private void mutate(Chromosome chromosome) {
        Random rand = new Random();
        // Randomly select a segment and assign it to a new valid slot
        List<Integer> segmentIds = new ArrayList<>(chromosome.getSlotAssignments().keySet());
        if (!segmentIds.isEmpty()) {
            int segId = segmentIds.get(rand.nextInt(segmentIds.size()));
            AClassSegment seg = findSegmentById(segId);

            List<Integer> validSlots = new ArrayList<>();
            Map<Integer, Integer> tempAssignments = new HashMap<>(chromosome.getSlotAssignments());
            tempAssignments.remove(segId);

            for (int session = 0; session < nbSessions; session++) {
                for (int slot = 1; slot <= nbSlotPerSession - seg.duration + 1; slot++) {
                    boolean valid = true;
                    for (int d = 0; d < seg.duration; d++) {
                        if (isSlotOccupied(session, slot + d, tempAssignments, seg)) {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) {
                        validSlots.add(session * nbSlotPerSession + slot);
                    }
                }
            }

            if (!validSlots.isEmpty()) {
                int newSlot = validSlots.get(rand.nextInt(validSlots.size()));
                chromosome.getSlotAssignments().put(segId, newSlot);
                chromosome.setEvaluated(false);
            }
        }
    }

    public Map<Integer, Integer> getSolutionSlot() {
        return bestSolutionSlot;
    }

    public boolean hasSolution() {
        return foundSolution;
    }

    public void printSolution() {
        if (!foundSolution) {
            System.out.println("No solution found");
            return;
        }

        for (Map.Entry<Integer, Integer> entry : bestSolutionSlot.entrySet()) {
            int segId = entry.getKey();
            int slot = entry.getValue();
            int session = slot / nbSlotPerSession;
            int slotInSession = slot % nbSlotPerSession;
            AClassSegment seg = findSegmentById(segId);

            System.out.println("Segment " + segId + " (Course " + seg.getCourse() +") " + "-> Session " + session + " Slot " + slotInSession + " Duration " + seg.duration);
        }
    }
    public void printSolutionPrefixFormat(){
        if (!foundSolution) {
            System.out.println("No solution found");
            return;
        }
        // Create a map of class ID to its segments' session and slot info
        Map<Integer, List<int[]>> classSchedules = new HashMap<>();

        // First, organize segments by their class
        for (AClass cls : classes) {
            List<int[]> sessionAndSlots = new ArrayList<>();
            // For each segment of this class
            for (AClassSegment seg : cls.classSegments) {
                Integer absoluteSlot = bestSolutionSlot.get(seg.id);
                if (absoluteSlot != null) {
                    int session = absoluteSlot / nbSlotPerSession;
                    int slotInSession = absoluteSlot % nbSlotPerSession;
                    sessionAndSlots.add(new int[]{session, slotInSession});
                }
            }
            // Sort by session, then by slot for consistent output
            sessionAndSlots.sort((a, b) -> {
                if (a[0] != b[0]) return Integer.compare(a[0], b[0]);
                return Integer.compare(a[1], b[1]);
            });
            classSchedules.put(cls.id, sessionAndSlots);
        }

        // Print in required format
        for (AClass cls : classes) {
            List<int[]> sessionAndSlots = classSchedules.get(cls.id);
            StringBuilder sb = new StringBuilder();
            // Add class ID
            sb.append(cls.id);
            // Add number of segments
            sb.append(" ").append(cls.classSegments.size());
            // Add session and start slot for each segment
            for (int[] slot : sessionAndSlots) {
                sb.append(" ").append(slot[0]).append(" ").append(slot[1]);
            }
            System.out.println(sb.toString());
        }
    }
    public void printInput(){
        System.out.println("Loaded " + this.totalClasses + "  classes across " + this.courses.size() + " courses");
        System.out.println("Sessions: " + this.nbSessions + ", Slots per session: " + this.nbSlotPerSession);

        // Print class details
        for (AClass cls : this.classes) {
            System.out.println("Class " + cls.id + " (Course " + cls.course + "): " + cls.classSegments.size() + " segments");
            for (AClassSegment seg : cls.classSegments) {
                System.out.println("Segment " + seg.id + ": duration= " + seg.duration);
            }
        }
    }
    public static void main(String[] args) {
        // Test file path
        String testFile = "/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/TimetablingAlgorithmPackage/data/et1-3th-s.txt"; // Update this path as needed

        System.out.println("Starting Genetic Algorithm Solver test with file" + testFile);

        GeneticAlgorithmSolver solver = new GeneticAlgorithmSolver();

        // Load input
        solver.inputFile(testFile);
        solver.printInput();

        // Run solver
        System.out.println("Starting solver with parameters:");
        System.out.println("Population size: " + solver.populationSize);
        System.out.println("Max generations: " + solver.maxGenerations);
        System.out.println("Mutation rate: " + solver.mutationRate);
        System.out.println("Crossover rate: " + solver.crossoverRate);

        long startTime = System.currentTimeMillis();
        solver.solve();
        long endTime = System.currentTimeMillis();

        // Print results
        System.out.println("Solver finished in" + (endTime - startTime) + " ms");

        if (solver.hasSolution()) {
            System.out.println("Solution found!");
            solver.printSolutionPrefixFormat();
            // Calculate statistics
            Map<Integer, Integer> solution = solver.getSolutionSlot();
            int totalSegments = 0;
            for (AClass cls : solver.classes) {
                totalSegments += cls.classSegments.size();
            }
            System.out.println("Solution Statistics:");
            System.out.println("Assigned segments: " + solution.size() + "/" + totalSegments);

            Chromosome finalChromosome = new Chromosome(solution);
            // Check combination validity
            boolean isValid = solver.isValidCombination(finalChromosome);
            System.out.println("Valid combinations: " + isValid);

            // Calculate teacher count for final solution
            if (solver.isValidCombination(finalChromosome))
            {
                int teacherCount = solver.calculateTeacherCount(finalChromosome);
                System.out.println("Total teachers needed: " + teacherCount);
            }
            // Check session-consecutive pairs
            System.out.println("Number of pairs of same session consecutive classes: " + solver.calculateCompactness(finalChromosome) );

        } else {
            System.out.println("No solution found!");
        }
    }

    private AClassSegment findSegmentById(int id) {
        for (AClass cls : classes) {
            for (AClassSegment seg : cls.classSegments) {
                if (seg.id == id) return seg;
            }
        }
        return null;
    }
}

// Chromosome class to represent a solution
class Chromosome {
    private Map<Integer, Integer> slotAssignments; // segmentId -> slot
    private double fitness;
    private boolean evaluated;

    public Chromosome(Map<Integer, Integer> slotAssignments) {
        this.slotAssignments = new HashMap<>(slotAssignments);
        this.evaluated = false;
    }

    public Chromosome(Chromosome other) {
        this.slotAssignments = new HashMap<>(other.slotAssignments);
        this.fitness = other.fitness;
        this.evaluated = other.evaluated;
    }

    public Map<Integer, Integer> getSlotAssignments() {
        return slotAssignments;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }
}
