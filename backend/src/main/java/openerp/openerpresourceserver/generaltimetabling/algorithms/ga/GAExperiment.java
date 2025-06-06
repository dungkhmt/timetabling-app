package openerp.openerpresourceserver.generaltimetabling.algorithms.ga;

import lombok.extern.log4j.Log4j2;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Log4j2
public class GAExperiment {
    
    @lombok.Data
    public static class ExperimentConfig {
        private int populationSize;
        private int maxGenerations;
        private double mutationRate;
        private double crossoverRate;
        
        public ExperimentConfig(int populationSize, int maxGenerations, double mutationRate, double crossoverRate) {
            this.populationSize = populationSize;
            this.maxGenerations = maxGenerations;
            this.mutationRate = mutationRate;
            this.crossoverRate = crossoverRate;
        }
        
        @Override
        public String toString() {
            return String.format("pop=%d_gen=%d_mut=%.2f_cross=%.2f", 
                populationSize, maxGenerations, mutationRate, crossoverRate);
        }
    }
    
    @lombok.Data
    public static class RunResult {
        private int teacherCount;
        private boolean isCombinationValid;
        private Map<Integer, Integer> sessionUtilization;
        private long executionTimeMs;
        private int generationsUntilBest;
        private List<Double> convergenceCurve;
        
        public RunResult() {
            this.sessionUtilization = new HashMap<>();
            this.convergenceCurve = new ArrayList<>();
        }
    }
    
    private final String inputFile;
    private final String outputDir;
    private final int runsPerConfig;
    
    // Expanded parameter ranges
    private static final int[] DEFAULT_POPULATION_SIZES = {30, 50, 100, 200, 300, 400, 500};
    private static final int[] DEFAULT_MAX_GENERATIONS = {50, 100, 200, 300, 500, 750, 1000};
    private static final double[] DEFAULT_MUTATION_RATES = {0.01, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.4};
    private static final double[] DEFAULT_CROSSOVER_RATES = {0.5, 0.6, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95};
    
    public GAExperiment(String inputFile, String outputDir, int runsPerConfig) {
        this.inputFile = inputFile;
        this.outputDir = outputDir;
        this.runsPerConfig = runsPerConfig;
        new File(outputDir).mkdirs();
    }
    
    public void runParameterExperiment(String paramName, Object[] values, ExperimentConfig baseConfig) {
        String experimentDir = createExperimentDirectory(paramName);
        log.info("Starting {} experiment with {} values", paramName, values.length);
        
        for (Object value : values) {
            ExperimentConfig config = createConfigWithValue(paramName, value, baseConfig);
            List<RunResult> results = runExperimentWithConfig(config);
            saveResults(experimentDir, config, results);
            logProgress(paramName, value, results);
        }
    }
    
    private String createExperimentDirectory(String paramName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String dir = outputDir + "/" + paramName + "_" + timestamp;
        new File(dir).mkdirs();
        return dir;
    }
    
    private ExperimentConfig createConfigWithValue(String paramName, Object value, ExperimentConfig baseConfig) {
        ExperimentConfig config = new ExperimentConfig(
            baseConfig.getPopulationSize(),
            baseConfig.getMaxGenerations(),
            baseConfig.getMutationRate(),
            baseConfig.getCrossoverRate()
        );
        
        switch (paramName) {
            case "population":
                config.setPopulationSize((Integer) value);
                break;
            case "generations":
                config.setMaxGenerations((Integer) value);
                break;
            case "mutation":
                config.setMutationRate((Double) value);
                break;
            case "crossover":
                config.setCrossoverRate((Double) value);
                break;
        }
        
        return config;
    }
    
    private List<RunResult> runExperimentWithConfig(ExperimentConfig config) {
        List<RunResult> results = new ArrayList<>();
        
        for (int run = 0; run < runsPerConfig; run++) {
            RunResult result = runSingleExperiment(config);
            results.add(result);
            
            // Log progress for long-running experiments
            if (runsPerConfig > 5) {
                log.info("Completed run {}/{} for config: {}", 
                    run + 1, runsPerConfig, config.toString());
            }
        }
        
        return results;
    }
    
    private RunResult runSingleExperiment(ExperimentConfig config) {
        RunResult result = new RunResult();
        
        GeneticAlgorithmSolver solver = new GeneticAlgorithmSolver(
            config.getPopulationSize(),
            config.getMaxGenerations(),
            config.getMutationRate(),
            config.getCrossoverRate()
        );
        
        solver.inputFile(inputFile);
        
        long startTime = System.currentTimeMillis();
        solver.solve();
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        
        if (solver.hasSolution()) {
            Map<Integer, Integer> solution = solver.getSolutionSlot();
            
            // Get metrics
            Chromosome finalChromosome = new Chromosome(solution);
            result.setTeacherCount(solver.calculateTeacherCount(finalChromosome));
            result.setCombinationValid(solver.isValidCombination(finalChromosome));
            result.setConvergenceCurve(solver.getConvergenceCurve());
            result.setGenerationsUntilBest(solver.getGenerationsUntilBest());
            
            // Calculate session utilization
            for (Integer slot : solution.values()) {
                int session = slot / solver.getNbSlotPerSession();
                result.getSessionUtilization().merge(session, 1, Integer::sum);
            }
        }
        
        return result;
    }
    
    private void saveResults(String experimentDir, ExperimentConfig config, List<RunResult> results) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(experimentDir + "/" + config + ".txt"))) {
            // Write configuration
            writer.println("Configuration:");
            writer.println("Population Size: " + config.getPopulationSize());
            writer.println("Max Generations: " + config.getMaxGenerations());
            writer.println("Mutation Rate: " + config.getMutationRate());
            writer.println("Crossover Rate: " + config.getCrossoverRate());
            writer.println();
            
            // Write aggregate statistics
            writer.println("Results Summary (across " + results.size() + " runs):");
            
            // Teacher count statistics
            double avgTeachers = results.stream()
                .mapToInt(RunResult::getTeacherCount)
                .average()
                .orElse(0);
            double stdDevTeachers = calculateStdDev(results, RunResult::getTeacherCount, avgTeachers);
            writer.println(String.format("Teacher Count: %.2f (±%.2f)", avgTeachers, stdDevTeachers));
            
            // Combination validity rate
            double validRate = results.stream()
                .filter(RunResult::isCombinationValid)
                .count() * 100.0 / results.size();
            writer.println(String.format("Combination Validity Rate: %.1f%%", validRate));
            
            // Execution time statistics
            double avgTime = results.stream()
                .mapToLong(RunResult::getExecutionTimeMs)
                .average()
                .orElse(0);
            double stdDevTime = calculateStdDev(results, RunResult::getExecutionTimeMs, avgTime);
            writer.println(String.format("Execution Time: %.2f ms (±%.2f)", avgTime, stdDevTime));
            
            // Convergence statistics
            double avgGens = results.stream()
                .mapToInt(RunResult::getGenerationsUntilBest)
                .average()
                .orElse(0);
            double stdDevGens = calculateStdDev(results, RunResult::getGenerationsUntilBest, avgGens);
            writer.println(String.format("Generations until best: %.2f (±%.2f)", avgGens, stdDevGens));
            
            // Session utilization
            writer.println("\nAverage Session Utilization:");
            Map<Integer, Double> avgUtilization = calculateAverageUtilization(results);
            avgUtilization.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> 
                    writer.println(String.format("Session %d: %.2f", entry.getKey(), entry.getValue())));
            
            // Convergence curve (average across all runs)
            writer.println("\nAverage Convergence Curve:");
            List<Double> avgCurve = calculateAverageConvergenceCurve(results);
            for (int i = 0; i < avgCurve.size(); i++) {
                if (i % 10 == 0 || i == avgCurve.size() - 1) { // Print every 10th generation and last
                    writer.println(String.format("Generation %d: %.4f", i, avgCurve.get(i)));
                }
            }
            
        } catch (IOException e) {
            log.error("Error saving results", e);
        }
    }
    
    private double calculateStdDev(List<RunResult> results, java.util.function.ToDoubleFunction<RunResult> mapper, double mean) {
        return Math.sqrt(results.stream()
            .mapToDouble(mapper)
            .map(x -> Math.pow(x - mean, 2))
            .average()
            .orElse(0));
    }
    
    private Map<Integer, Double> calculateAverageUtilization(List<RunResult> results) {
        Map<Integer, Double> avgUtilization = new HashMap<>();
        
        // Get all sessions
        Set<Integer> allSessions = new HashSet<>();
        results.forEach(r -> allSessions.addAll(r.getSessionUtilization().keySet()));
        
        // Calculate average for each session
        for (Integer session : allSessions) {
            double avg = results.stream()
                .mapToInt(r -> r.getSessionUtilization().getOrDefault(session, 0))
                .average()
                .orElse(0);
            avgUtilization.put(session, avg);
        }
        
        return avgUtilization;
    }
    
    private List<Double> calculateAverageConvergenceCurve(List<RunResult> results) {
        List<Double> avgCurve = new ArrayList<>();
        
        // Find maximum curve length
        int maxLength = results.stream()
            .mapToInt(r -> r.getConvergenceCurve().size())
            .max()
            .orElse(0);
        
        // Calculate average for each generation
        for (int i = 0; i < maxLength; i++) {
            final int gen = i;
            double avgFitness = results.stream()
                .filter(r -> r.getConvergenceCurve().size() > gen)
                .mapToDouble(r -> r.getConvergenceCurve().get(gen))
                .average()
                .orElse(0);
            avgCurve.add(avgFitness);
        }
        
        return avgCurve;
    }
    
    private void logProgress(String paramName, Object value, List<RunResult> results) {
        double avgTeachers = results.stream()
            .mapToInt(RunResult::getTeacherCount)
            .average()
            .orElse(0);
        long validCount = results.stream()
            .filter(RunResult::isCombinationValid)
            .count();
        double avgTime = results.stream()
            .mapToLong(RunResult::getExecutionTimeMs)
            .average()
            .orElse(0);
        
        log.info("{} = {}: Teachers = {}, Valid = {}/{}, Time = {} ms",
            paramName, value, String.format("%.2f", avgTeachers), 
            validCount, results.size(), String.format("%.0f", avgTime));
    }
    
    public void runAllExperiments() {
        // Base configuration
        ExperimentConfig baseConfig = new ExperimentConfig(50, 100, 0.1, 0.8);
        
        // Run experiments for each parameter
        runParameterExperiment("population", Arrays.stream(DEFAULT_POPULATION_SIZES)
            .boxed().toArray(), baseConfig);
            
        runParameterExperiment("generations", Arrays.stream(DEFAULT_MAX_GENERATIONS)
            .boxed().toArray(), baseConfig);
            
        runParameterExperiment("mutation", Arrays.stream(DEFAULT_MUTATION_RATES)
            .boxed().toArray(), baseConfig);
            
        runParameterExperiment("crossover", Arrays.stream(DEFAULT_CROSSOVER_RATES)
            .boxed().toArray(), baseConfig);
    }
    
    public static void main(String[] args) {
        // Input file path - CHANGE THIS TO YOUR INPUT FILE PATH
        String inputFile = "/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/Web/web-app/timetabling-app/backend/data/ch1-3th-s.txt";
        
        // Output directory path - CHANGE THIS TO YOUR DESIRED OUTPUT DIRECTORY
        String outputDir = "/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/Web/web-app/timetabling-app/backend/src/main/java/openerp/openerpresourceserver/generaltimetabling/algorithms/ga/experiments";
        
        // Number of runs per configuration
        int runsPerConfig = 10;
        
        log.info("Starting GA Parameter Experiments");
        log.info("Input file: {}", inputFile);
        log.info("Output directory: {}", outputDir);
        log.info("Runs per configuration: {}", runsPerConfig);
        
        GAExperiment experiment = new GAExperiment(inputFile, outputDir, runsPerConfig);
        experiment.runAllExperiments();
    }
} 