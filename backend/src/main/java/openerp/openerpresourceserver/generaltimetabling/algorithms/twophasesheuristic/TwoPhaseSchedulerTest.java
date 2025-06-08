package openerp.openerpresourceserver.generaltimetabling.algorithms.twophasesheuristic;

import openerp.openerpresourceserver.generaltimetabling.algorithms.util.*;

import java.io.*;
import java.util.*;

public class TwoPhaseSchedulerTest {
    public Map<String, List<AClass>> mCourse2Classes; // Input
    public Map<String, List<AClass>> SC = new HashMap<>(); // SC[i] - Set of scheduled classes in course j
    public Map<String, List<AClass>> USC = new HashMap<>(); // USC[i] - Set of unscheduled classes in course j
    public Set<String> Alpha = new HashSet<>(); // Set of courses that have scheduled classes
    public List<AClass> candidates = new ArrayList<>(); // Set of remaining unscheduled classes
    public Map<AClass, SolutionClass>[] best_x; // Final schedule for classes
    public List<String> courses;
    public int nbSlotPerSession;
    public int nbSessions;
    public int totalClasses;

    @SuppressWarnings("unchecked")
    public void inputFile(String filename) {
        // Read input file
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)))) {
            String[] s = in.readLine().split(" ");
            nbSlotPerSession = Integer.parseInt(s[0]);
            nbSessions = Integer.parseInt(s[1]);

            int nbClasses = Integer.parseInt(in.readLine());
            List<AClass> classes = new ArrayList<>();
            mCourse2Classes = new HashMap<>();
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
                mCourse2Classes.computeIfAbsent(course, k -> new ArrayList<>()).add(cls);
                classes.add(cls);
            }

            courses = new ArrayList<>(mCourse2Classes.keySet());
            best_x = new HashMap[courses.size()];
            for (int i = 0; i < courses.size(); i++) best_x[i] = new HashMap<>();

            for (String course : courses) {
                SC.put(course, new ArrayList<>());
                USC.put(course, new ArrayList<>(mCourse2Classes.get(course)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String course : courses) {
            totalClasses += mCourse2Classes.get(course).size();
        }
    }
    public void printInputSummary() {
        System.out.println("Number of sessions per day: " + nbSessions);
        System.out.println("Number of slots per session: " + nbSlotPerSession);
        System.out.println("Total courses: " + courses.size());

        for (String course : courses) {
            System.out.println("Course: " + course);
            for (AClass cls : mCourse2Classes.get(course)) {
                System.out.print("  Class ID: " + cls.id + " → Segments: ");
                for (AClassSegment seg : cls.classSegments) {
                    System.out.print("[" + seg.id + ", duration=" + seg.duration + "] ");
                }
                System.out.println();
            }
        }
    }


    public void runPhase1Manual() {
        System.out.println("-- Starting Phase 1 --");
        int session = 0;
        List<SolutionClass> initialAssignments = new ArrayList<>();

        for (int i = 0; i < courses.size(); i++) {
            String course = courses.get(i);
            AClass cls = USC.get(course).remove(0);

            List<int[]> periods = new ArrayList<>();
            boolean assigned = false;
            // Assign first 5 courses to the first slot of each new day
            if (i < nbSessions) {
                // Assign to new session
                int slot = 1;
                int currentSession = session++;
                for (AClassSegment seg : cls.classSegments) {
                    int start = slot;
                    int end = slot + seg.duration - 1;
                    periods.add(new int[]{start, end, currentSession});
                    slot += seg.duration;
                }
                assigned = true;
            }
            else {
                // Try all possible non-overlapping slots with existing assignments
                outer:
                for (int s = 0; s < nbSessions; s++) {
                    for (int sl = 1; sl <= nbSlotPerSession; sl++) {
                        periods.clear();
                        int currSlot = sl;
                        boolean fits = true;

                        for (AClassSegment seg : cls.classSegments) {
                            int end = currSlot + seg.duration - 1;
                            if (end > nbSlotPerSession) {
                                fits = false;
                                break;
                            }
                            periods.add(new int[]{currSlot, end, s});
                            currSlot = end + 1;
                        }
                        if (!fits) continue;

                        // Check for overlap with already assigned initial classes
                        boolean conflict = false;
                        for (SolutionClass assignedSc : initialAssignments) {
                            for (int[] p1 : periods) {
                                for (int[] p2 : assignedSc.periods) {
                                    if (p1[2] == p2[2] && !(p1[1] < p2[0] || p2[1] < p1[0])) {
                                        conflict = true;
                                        break;
                                    }
                                }
                            }
                            if (conflict) break;
                        }
                        if (!conflict) {
                            assigned = true;
                            break outer;
                        }
                    }
                }
            }
            if (!assigned) {
                System.out.println("Failed to assign class " + cls.id + " in Phase 1");
                continue;
            }
            SolutionClass sc = new SolutionClass(cls, periods);
            best_x[i].put(cls, sc);
            SC.get(course).add(cls);
            Alpha.add(course);
            initialAssignments.add(sc);
            candidates.addAll(USC.get(course));
        }
        System.out.println("-- Phase 1 Assignments --");
        for (int i = 0; i < courses.size(); i++) {
            for (Map.Entry<AClass, SolutionClass> entry : best_x[i].entrySet()) {
                AClass cls = entry.getKey();
                SolutionClass sc = entry.getValue();
                System.out.print("Class ID " + cls.id + " (" + cls.course + "): ");
                for (int[] p : sc.periods) {
                    System.out.print("[S=" + p[0] + ",E=" + p[1] + ",T=" + p[2] + "] ");
                }
                System.out.println();
            }
        }
    }

    public void runPhase2() {
        System.out.println("-- Starting Phase 2 --");
        int m = courses.size();
        int ic = 0;

        while (!candidates.isEmpty()) {
            String course = courses.get(ic);
            List<AClass> unscheduled = USC.get(course);

            System.out.println("Checking course: " + course + ", index: " + ic);
            System.out.println("Remaining unscheduled classes for course: " + unscheduled.size());

            if (!unscheduled.isEmpty()) {
                AClass k = unscheduled.get(0);
                System.out.println("  Trying to schedule Class ID: " + k.id + " (" + k.course + ")");

                List<int[]> fullySameSession = null;
                List<int[]> disjointFallback = null;
                List<int[]> overlapFallback = null;
                int maxPartialMatches = -1;
                int minOverlap = Integer.MAX_VALUE; // number of overlapping classes in a course

                List<List<int[]>> possibleSchedules = getAllPossibleSchedules(k); // get every possible schedules for class k
                System.out.println("  Found " + possibleSchedules.size() + " possible schedules.");

                for (List<int[]> schedule : possibleSchedules) {
                    System.out.println("Testing schedule: " + formatSchedule(schedule));
                    if (!isCompatible(k, schedule)) {
                        System.out.println("Not compatible (fails global constraint).");
                        continue;
                    }
                    if (isFullySameSessionConsecutive(schedule, k) && isDisjointWithSameCourse(schedule, course)) {
                        System.out.println("Fully same-session consecutive and disjoint — selecting immediately.");
                        fullySameSession = schedule;
                        break;
                    }
                    if (isDisjointWithSameCourse(schedule, course)) {
                        int matchedSegments = countMatchedSegments(schedule, k);
                        System.out.println("Compatible & disjoint with same course");
                        if (matchedSegments > maxPartialMatches) {
                            maxPartialMatches = matchedSegments;
                            disjointFallback = schedule;
                        }
                    } else {
                        int overlap = countOverlapsInCourse(schedule, course);
                        System.out.println("Compatible with overlap. Overlap count: " + overlap);
                        if (overlap < minOverlap) {
                            minOverlap = overlap;
                            overlapFallback = schedule;
                        }
                    }
                }

                List<int[]> bestSchedule = fullySameSession != null ? fullySameSession :
                        disjointFallback != null ? disjointFallback :
                                overlapFallback;

                if (bestSchedule != null) {
                    SolutionClass sc = new SolutionClass(k, bestSchedule);
                    best_x[courses.indexOf(course)].put(k, sc);
                    SC.get(course).add(k);
                    Alpha.add(course);
                    System.out.println("Scheduled Class ID: " + k.id + " with " + bestSchedule.size() + " segments: " + formatSchedule(bestSchedule));
                } else {
                    System.out.println("No valid schedule found for Class ID: " + k.id);
                }

                candidates.removeIf(c -> c.id == k.id && c.course.equals(k.course));
                USC.get(course).removeIf(c -> c.id == k.id && c.course.equals(k.course));
            }

            ic = (ic + 1) % m;
        }

        System.out.println("\n-- Phase 2 Finished --");
    }


    private boolean isCompatible(AClass candidate, List<int[]> candidateSchedule) {
        List<AClass> allClasses = new ArrayList<>();
        List<Integer>[] classIndicesOfCourse = new ArrayList[courses.size()];
        SolutionClass[] xArray = new SolutionClass[totalClasses];

        int idx = 0;
        for (int i = 0; i < courses.size(); i++) {
            classIndicesOfCourse[i] = new ArrayList<>();
            for (AClass ac : SC.get(courses.get(i))) {
                allClasses.add(ac);
                classIndicesOfCourse[i].add(idx);
                xArray[idx++] = best_x[i].get(ac);
            }
        }

        allClasses.add(candidate);
        xArray[idx] = new SolutionClass(candidate, candidateSchedule);
        classIndicesOfCourse[courses.indexOf(candidate.course)].add(idx);

        CombinationChecker checker = new CombinationChecker(classIndicesOfCourse, allClasses, xArray);
        for (int i = 0; i < allClasses.size(); i++) {
            if (!checker.checkInCombination(i)) return false;
        }
        return true;
    }

    private boolean isFullySameSessionConsecutive(List<int[]> sched, AClass cls) {
        // Check whether every segment in the proposed schedule (sched) for a class (cls)
        // is same-session-consecutive to some already scheduled segment of the same course
        int matched = 0;
        for (int[] seg : sched) {
            if (isMatched(seg, cls)) matched++;
        }
        return matched == sched.size();
    }

    private boolean isMatched(int[] seg, AClass owner) {
        // Check whether a single segment seg is same-session-consecutive
        // to any already scheduled segment of the same course.
        int start = seg[0], end = seg[1], session = seg[2];
        for (Map<AClass, SolutionClass> courseSol : best_x) {
            for (SolutionClass sc : courseSol.values()) {
                if (!sc.cls.course.equals(owner.course)) continue; // skip different courses
                for (int[] other : sc.periods) {
                    if (session == other[2] && (end + 1 == other[0] || other[1] + 1 == start)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private boolean isDisjointWithSameCourse(List<int[]> sched, String course) {
        for (AClass ac : SC.get(course)) {
            SolutionClass sc = best_x[courses.indexOf(course)].get(ac);
            for (int[] p1 : sched) {
                for (int[] p2 : sc.periods) {
                    if (!disjoint(p1, p2)) return false;
                }
            }
        }
        return true;
    }


    private boolean disjoint(int[] a, int[] b) { // check if two schedules are disjoint
        return a[2] != b[2] || a[1] < b[0] || b[1] < a[0];
    }
    private int countMatchedSegments(List<int[]> sched, AClass cls) {
        int matched = 0;
        for (int[] seg : sched) {
            if (isMatched(seg, cls)) matched++;
        }
        return matched;
    }
    private int countOverlapsInCourse(List<int[]> schedule, String course) {
        int count = 0;
        for (AClass cls : SC.get(course)) {
            SolutionClass sc = best_x[courses.indexOf(course)].get(cls);
            for (int[] p1 : schedule) {
                for (int[] p2 : sc.periods) {
                    if (!disjoint(p1, p2)) count++;
                }
            }
        }
        return count;
    }

    private List<List<int[]>> getAllPossibleSchedules(AClass cls) {
        // Generate all possible assignments for the segment(s) of class cls
        List<List<int[]>> results = new ArrayList<>();
        backtrackSegmentSchedules(cls.classSegments, 0, new ArrayList<>(), results);
        return results;
    }
    private void backtrackSegmentSchedules(List<AClassSegment> segments, int idx, List<int[]> current, List<List<int[]>> results) {
        // segments - the list of a class's segments
        // idx - index of the current segment being scheduled
        // current[i] - schedule of segment i in the class
        // results - every possible assignment
        if (idx == segments.size()) {
            results.add(new ArrayList<>(current));
            return;
        }
        AClassSegment seg = segments.get(idx);
        for (int session = 0; session < nbSessions; session++) {
            // iterate through every session
            for (int slot = 1; slot <= nbSlotPerSession - seg.duration + 1; slot++) {
                // iterate through every slot (as long as it does not exceed the max number of slots per day)
                int start = slot;
                int end = slot + seg.duration - 1;
                int[] newSeg = new int[]{start, end, session}; // an assignment for the segment
                boolean overlap = false;
                for (int[] assigned : current) { // check overlap with assigned segment(s) in the same class
                    if (!disjoint(assigned, newSeg)) {
                        overlap = true;
                        break;
                    }
                }
                if (!overlap) {
                    current.add(newSeg);
                    backtrackSegmentSchedules(segments, idx + 1, current, results);
                    current.remove(current.size() - 1);
                }
            }
        }
    }
    private String formatSchedule(List<int[]> periods) {
        StringBuilder sb = new StringBuilder();
        for (int[] p : periods) {
            sb.append("(").append(p[0]).append("-").append(p[1])
                    .append("-").append(p[2]).append("), ");
        }
        return sb.toString();
    }

    public void printFinalSolution() {
        for (int i = 0; i < courses.size(); i++) {
            for (AClass c : best_x[i].keySet()) {
                SolutionClass sc = best_x[i].get(c);
                System.out.print(c.id + " " + sc.periods.size() + " ");
                for (int[] p : sc.periods) {
                    System.out.print(p[2] + " " + p[0] + " ");
                }
                System.out.println();
            }
        }
    }
    public void saveFinalSolutionToFile(String inputFilePath) {
        try {
            // Extract the filename (e.g., "1.txt") from the full input path
            File inputFile = new File(inputFilePath);
            String fileName = inputFile.getName(); // e.g., "1.txt"

            // Build the output path in the "experiment" package
            String outputPath = "/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/TimetablingAlgorithmPackage/src/experiment/twophase" + fileName;
            PrintWriter out = new PrintWriter(new FileWriter(outputPath));

            for (int i = 0; i < courses.size(); i++) {
                for (AClass c : best_x[i].keySet()) {
                    SolutionClass sc = best_x[i].get(c);
                    out.print(c.id + " " + sc.periods.size() + " ");
                    for (int[] p : sc.periods) {
                        out.print(p[2] + " " + p[0] + " ");
                    }
                    out.println();
                }
            }

            out.close();
            System.out.println("✅ Final solution saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("❌ Error saving final solution: " + e.getMessage());
        }
    }
    public int computeTotalTeachers() {
        int totalTeachers = 0;

        for (int i = 0; i < courses.size(); i++) {
            Map<AClass, SolutionClass> courseSol = best_x[i];
            if (courseSol.isEmpty()) continue;

            MaxClique maxClique = new MaxClique();
            int maxOverlap = maxClique.computeMaxClique(courseSol);

            totalTeachers += maxOverlap;
        }

        System.out.println("🔎 Objective function (Total minimum number of teachers): " + totalTeachers);
        return totalTeachers;
    }

//    public static void main(String[] args) {
//        TwoPhaseHeuristicScheduler scheduler = new TwoPhaseHeuristicScheduler();
//        long startTime = System.currentTimeMillis();
//        String inputPath = "/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/TimetablingAlgorithmPackage/data/te2-3th-s.txt";
//        scheduler.inputFile(inputPath);
////        scheduler.printInputSummary();
//        scheduler.runPhase1Manual();
//        scheduler.runPhase2();
//        long endTime = System.currentTimeMillis();
//        long duration = endTime - startTime;
//        scheduler.printFinalSolution();
//        scheduler.saveFinalSolutionToFile(inputPath);
//        int scheduledCount = 0;
//        for (int i = 0; i < scheduler.courses.size(); i++) {
//            scheduledCount += scheduler.best_x[i].size();
//        }
//
//        if (scheduledCount == scheduler.totalClasses) {
//            scheduler.computeTotalTeachers();
//        } else {
//            System.out.println("⚠️ Not all classes have been scheduled. Skipping teacher count.");
//        }
//        System.out.println("⏱️ Total execution time: " + duration + " ms");
//    }

    public static void main(String[] args) {
        File dataFolder = new File("/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/TimetablingAlgorithmPackage/data");
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".txt"));

        String resultLog = "/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/TimetablingAlgorithmPackage/src/experiment/twophase/result.txt";

        try (PrintWriter resultWriter = new PrintWriter(new FileWriter(resultLog))) {
            resultWriter.println("Filename\tExecutionTime(ms)\tTeachersCount");

            for (File file : files) {
                String inputPath = file.getAbsolutePath();
                System.out.println("📄 Processing: " + file.getName());

                TwoPhaseSchedulerTest scheduler = new TwoPhaseSchedulerTest();
                long startTime = System.currentTimeMillis();
                scheduler.inputFile(inputPath);
                scheduler.runPhase1Manual();
                scheduler.runPhase2();
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                int scheduledCount = 0;
                for (int i = 0; i < scheduler.courses.size(); i++) {
                    scheduledCount += scheduler.best_x[i].size();
                }

                int teacherCount = -1;
                if (scheduledCount == scheduler.totalClasses) {
                    teacherCount = scheduler.computeTotalTeachers();
                } else {
                    System.out.println("⚠️ Not all classes scheduled in " + file.getName());
                }

                resultWriter.println(file.getName() + "\t" + duration + "\t" + (teacherCount >= 0 ? teacherCount : "Not complete"));
            }

            System.out.println("✅ Summary saved to: " + resultLog);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}