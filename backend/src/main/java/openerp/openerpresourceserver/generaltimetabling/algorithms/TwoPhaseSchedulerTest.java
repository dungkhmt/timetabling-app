package openerp.openerpresourceserver.generaltimetabling.algorithms;

import openerp.openerpresourceserver.generaltimetabling.algorithms.util.AClass;
import openerp.openerpresourceserver.generaltimetabling.algorithms.util.AClassSegment;
import openerp.openerpresourceserver.generaltimetabling.algorithms.util.CombinationChecker;
import openerp.openerpresourceserver.generaltimetabling.algorithms.util.SolutionClass;

import java.io.*;
import java.util.*;

public class TwoPhaseSchedulerTest {
    Map<String, List<AClass>> mCourse2Classes; // Input
    Map<String, List<AClass>> SC = new HashMap<>(); // SC[i] - Set of scheduled classes in course j
    Map<String, List<AClass>> USC = new HashMap<>(); // USC[i] - Set of unscheduled classes in course j
    Set<String> Alpha = new HashSet<>(); // Set of courses that have scheduled classes
    List<AClass> candidates = new ArrayList<>(); // Set of remaining unscheduled classes
    Map<AClass, SolutionClass>[] best_x; // Final schedule for classes
    List<String> courses;
    int nbSlotPerSession;
    int nbSessions;
    int totalClasses;

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
        // Manually assign every first class of each course to the first slot of each session
        // Can only assign up to 5 courses
        System.out.println("-- Starting Phase 1 --");
        int session = 0;

        for (int i = 0; i < courses.size(); i++) {
            String course = courses.get(i);
//            System.out.println(course + "\n");
            AClass cls = USC.get(course).remove(0);
//            System.out.println(cls + "\n");
            List<int[]> periods = new ArrayList<>();

            int slot = 1;
            for (AClassSegment seg : cls.classSegments) {
                int start = slot;
                int end = slot + seg.duration - 1;
                periods.add(new int[]{start, end, session});
                slot += seg.duration;
                if (slot > nbSlotPerSession) {
                    slot = 1;
                    session++;
                }
            }

            SolutionClass sc = new SolutionClass(cls, periods);
            best_x[i].put(cls, sc);
            SC.get(course).add(cls);
            Alpha.add(course);
            candidates.addAll(USC.get(course));
            session++;
        }
        System.out.println("\n-- Phase 1 Assignments --");
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
//
//        System.out.println("\n-- Alpha (Courses with Scheduled Classes) --");
//        for (String course : Alpha) {
//            System.out.println(course);
//        }
//
//        System.out.println("\n-- Candidates (Remaining Classes to Schedule) --");
//        for (AClass c : candidates) {
//            System.out.println("Class ID " + c.id + " (" + c.course + ")");
//        }
    }

    public void runPhase1Smart() {
        System.out.println("-- Starting Phase 1 (Smart Backtracking) --");
        long startTime = System.currentTimeMillis(); // Start time tracking
        List<String> selectedCourses = new ArrayList<>(courses);
        List<SolutionClass> bestSolution = new ArrayList<>();
        int[] bestSessionCount = { -1 };
        int[] bestSlot1Count = { -1 };

        backtrackPhase1Smart(0, selectedCourses, new ArrayList<>(), bestSolution, bestSessionCount, bestSlot1Count);
        long endTime = System.currentTimeMillis(); // End time tracking
        long duration = endTime - startTime;
        for (SolutionClass sc : bestSolution) {
            String course = sc.cls.course;
            int i = courses.indexOf(course);

            best_x[i].put(sc.cls, sc);
            SC.get(course).add(sc.cls);
            Alpha.add(course);

            USC.get(course).remove(sc.cls);
            candidates.addAll(USC.get(course));
        }

        System.out.println("\n-- Phase 1 Assignments --");
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
        System.out.println("Phase 1 completed in " + duration + " ms");
    }

    private void backtrackPhase1Smart(int idx, List<String> courses, List<SolutionClass> current,
                                      List<SolutionClass> best, int[] bestSessionCount, int[] bestSlot1Count) {
        if (idx == courses.size()) {
            List<AClass> allClasses = new ArrayList<>();
            List<Integer>[] classIndicesOfCourse = new ArrayList[courses.size()];
            SolutionClass[] xArray = new SolutionClass[courses.size()];

            for (int i = 0; i < courses.size(); i++) classIndicesOfCourse[i] = new ArrayList<>();

            for (int i = 0; i < current.size(); i++) {
                SolutionClass sc = current.get(i);
                allClasses.add(sc.cls);
                classIndicesOfCourse[courses.indexOf(sc.cls.course)].add(i);
                xArray[i] = sc;
            }

            CombinationChecker checker = new CombinationChecker(classIndicesOfCourse, allClasses, xArray);
            for (int i = 0; i < allClasses.size(); i++) {
                if (!checker.checkInCombination(i)) return;
            }

            Set<Integer> sessionsUsed = new HashSet<>();
            int slot1Count = 0;
            for (SolutionClass sc : current) {
                for (int[] p : sc.periods) {
                    sessionsUsed.add(p[2]);
                    if (p[0] == 1) slot1Count++;
                }
            }

            int sessionCount = sessionsUsed.size();
            boolean better = (sessionCount > bestSessionCount[0]) ||
                    (sessionCount == bestSessionCount[0] && slot1Count > bestSlot1Count[0]);

            if (better) {
                bestSessionCount[0] = sessionCount;
                bestSlot1Count[0] = slot1Count;
                best.clear();
                for (SolutionClass sc : current) {
                    List<int[]> copyPeriods = new ArrayList<>();
                    for (int[] p : sc.periods)
                        copyPeriods.add(new int[]{p[0], p[1], p[2]});
                    best.add(new SolutionClass(sc.cls, copyPeriods));
                }
            }
            return;
        }

        // Try assigning a slot for the current course's first class
        String course = courses.get(idx);
        AClass cls = USC.get(course).get(0);
        for (int session = 0; session < nbSessions; session++) {
            for (int slot = 1; slot <= nbSlotPerSession; slot++) {
                List<int[]> periods = new ArrayList<>();
                int currSlot = slot;
                boolean fits = true;

                for (AClassSegment seg : cls.classSegments) {
                    int end = currSlot + seg.duration - 1;
                    if (end > nbSlotPerSession) {
                        fits = false;
                        break;
                    }
                    periods.add(new int[]{currSlot, end, session});
                    currSlot = end + 1;
                }

                if (!fits) continue;

                current.add(new SolutionClass(cls, periods));
                backtrackPhase1Smart(idx + 1, courses, current, best, bestSessionCount, bestSlot1Count);
                current.remove(current.size() - 1);
            }
        }
    }


    public void runPhase2() {
        System.out.println("\n-- Starting Phase 2 --");
        int m = courses.size();
        int ic = 0;

        while (!candidates.isEmpty()) {
            String course = courses.get(ic);
            List<AClass> unscheduled = USC.get(course);

            System.out.println("\nChecking course: " + course + ", index: " + ic);
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

    private boolean isCompatible(List<int[]> sched) { // wrong logic
        for (String c : Alpha) {
            boolean found = false;
            for (AClass ac : SC.get(c)) { // check every class in course c
                SolutionClass sc = best_x[courses.indexOf(c)].get(ac);
                boolean conflict = false;
                for (int[] p1 : sched) {
                    for (int[] p2 : sc.periods) {
                        if (!disjoint(p1, p2)) {
                            conflict = true;
                            break;
                        }
                    }
                }
                if (!conflict) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
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
    public void readFromStdin() {
        try (Scanner scanner = new Scanner(System.in)) {
            nbSlotPerSession = scanner.nextInt();
            nbSessions = scanner.nextInt();
            int nbClasses = scanner.nextInt();

            mCourse2Classes = new HashMap<>();
            SC = new HashMap<>();
            USC = new HashMap<>();
            candidates = new ArrayList<>();

            int id = 0;
            for (int i = 0; i < nbClasses; i++) {
                int classId = scanner.nextInt();
                String course = scanner.next();
                int nbSeg = scanner.nextInt();

                List<AClassSegment> segs = new ArrayList<>();
                for (int j = 0; j < nbSeg; j++) {
                    int dur = scanner.nextInt();
                    AClassSegment seg = new AClassSegment(++id, course, dur);
                    segs.add(seg);
                }

                AClass cls = new AClass(classId, course, segs);
                mCourse2Classes.computeIfAbsent(course, k -> new ArrayList<>()).add(cls);
            }

            courses = new ArrayList<>(mCourse2Classes.keySet());
            best_x = new HashMap[courses.size()];
            for (int i = 0; i < courses.size(); i++) best_x[i] = new HashMap<>();

            for (String course : courses) {
                SC.put(course, new ArrayList<>());
                USC.put(course, new ArrayList<>(mCourse2Classes.get(course)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        TwoPhaseSchedulerTest scheduler = new TwoPhaseSchedulerTest();
//        scheduler.readFromStdin();
        scheduler.inputFile("/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/Web/web-app/timetabling-app/backend/data/em4-2nd-s-altered.txt");
        scheduler.printInputSummary();
//        scheduler.runPhase1Manual();
        scheduler.runPhase1Smart();
        scheduler.runPhase2();
        scheduler.printFinalSolution();
    }
}
