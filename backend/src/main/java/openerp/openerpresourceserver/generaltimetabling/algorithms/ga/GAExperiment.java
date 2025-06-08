package openerp.openerpresourceserver.generaltimetabling.algorithms.ga;

import lombok.extern.log4j.Log4j2;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GAExperiment {
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
        public int getPopulationSize() {
            return populationSize;
        }

        public int getMaxGenerations() {
            return maxGenerations;
        }

        public double getMutationRate() {
            return mutationRate;
        }

        public double getCrossoverRate() {
            return crossoverRate;
        }

        public void setPopulationSize(int populationSize) {
            this.populationSize = populationSize;
        }

        public void setMaxGenerations(int maxGenerations) {
            this.maxGenerations = maxGenerations;
        }

        public void setMutationRate(double mutationRate) {
            this.mutationRate = mutationRate;
        }

        public void setCrossoverRate(double crossoverRate) {
            this.crossoverRate = crossoverRate;
        }
    }

    public static class RunResult {
        private Integer teacherCount;
        private boolean isCombinationValid;
        private long executionTimeMs;
        private int generationsUntilBest;

        public void setTeacherCount(Integer teacherCount) {
            this.teacherCount = teacherCount;
        }

        public void setCombinationValid(boolean combinationValid) {
            isCombinationValid = combinationValid;
        }

        public void setExecutionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
        }

        public void setGenerationsUntilBest(int generationsUntilBest) {
            this.generationsUntilBest = generationsUntilBest;
        }

        public Integer getTeacherCount() {
            return teacherCount;
        }

        public boolean isCombinationValid() {
            return isCombinationValid;
        }

        public long getExecutionTimeMs() {
            return executionTimeMs;
        }

        public int getGenerationsUntilBest() {
            return generationsUntilBest;
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
        // values - list of testing values
        String experimentDir = createExperimentDirectory(paramName);
        System.out.println("Starting " + paramName + "  experiment with " + values.length + " values");

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
                System.out.println("Completed run " + (run + 1) + "/" + runsPerConfig + " for config: " + config.toString());
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
            result.setCombinationValid(solver.isValidCombination(finalChromosome));
            // Only calculate teacher count if combination is valid
            if (result.isCombinationValid()) {
                result.setTeacherCount(solver.calculateTeacherCount(finalChromosome));
            }
            result.setGenerationsUntilBest(solver.getGenerationsUntilBest());
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

            // Write individual run results
            writer.println("Individual Run Results:");
            writer.println("Run\tTeachers\tValid\tTime(ms)\tGenerations");
            for (int i = 0; i < results.size(); i++) {
                RunResult run = results.get(i);
                writer.println(String.format("%d\t%s\t%b\t%d\t%d",
                        i + 1,
                        run.isCombinationValid() ? run.getTeacherCount() : "N/A",
                        run.isCombinationValid(),
                        run.getExecutionTimeMs(),
                        run.getGenerationsUntilBest()
                ));
            }
            writer.println();

            // Write aggregate statistics
            writer.println("Results Summary (across " + results.size() + " runs):");

            // Count valid solutions
            long validSolutions = results.stream()
                    .filter(RunResult::isCombinationValid)
                    .count();

            // Combination validity rate
            double validRate = validSolutions * 100.0 / results.size();
            writer.println(String.format("Combination Validity Rate: %.1f%% (%d/%d runs)",
                    validRate, validSolutions, results.size()));

            // Teacher count statistics (only for valid solutions)
            if (validSolutions > 0) {
                double avgTeachers = results.stream()
                        .filter(RunResult::isCombinationValid)
                        .mapToInt(r -> r.getTeacherCount())
                        .average()
                        .orElse(0);
                double stdDevTeachers = calculateStdDev(
                        results.stream().filter(RunResult::isCombinationValid).collect(java.util.stream.Collectors.toList()),
                        r -> r.getTeacherCount(),
                        avgTeachers
                );
                writer.println(String.format("Teacher Count (valid solutions only): %.2f (±%.2f)", avgTeachers, stdDevTeachers));

                // Best/Worst teacher counts (only for valid solutions)
                writer.println("Best Teacher Count: " + results.stream()
                        .filter(RunResult::isCombinationValid)
                        .mapToInt(RunResult::getTeacherCount)
                        .min().orElse(0));
                writer.println("Worst Teacher Count: " + results.stream()
                        .filter(RunResult::isCombinationValid)
                        .mapToInt(RunResult::getTeacherCount)
                        .max().orElse(0));
            } else {
                writer.println("No valid solutions found - teacher count statistics unavailable");
            }

            // Execution time statistics
            double avgTime = results.stream()
                    .mapToLong(RunResult::getExecutionTimeMs)
                    .average()
                    .orElse(0);
            double stdDevTime = calculateStdDev(results, RunResult::getExecutionTimeMs, avgTime);
            writer.println(String.format("Execution Time: %.2f ms (±%.2f)", avgTime, stdDevTime));
            writer.println("Fastest Run (ms): " + results.stream().mapToLong(RunResult::getExecutionTimeMs).min().orElse(0));
            writer.println("Slowest Run (ms): " + results.stream().mapToLong(RunResult::getExecutionTimeMs).max().orElse(0));

            // Convergence statistics
            double avgGens = results.stream()
                    .mapToInt(RunResult::getGenerationsUntilBest)
                    .average()
                    .orElse(0);
            double stdDevGens = calculateStdDev(results, RunResult::getGenerationsUntilBest, avgGens);
            writer.println(String.format("Generations until best: %.2f (±%.2f)", avgGens, stdDevGens));
            writer.println("Fastest Convergence (generations): " + results.stream().mapToInt(RunResult::getGenerationsUntilBest).min().orElse(0));
            writer.println("Slowest Convergence (generations): " + results.stream().mapToInt(RunResult::getGenerationsUntilBest).max().orElse(0));

        } catch (IOException e) {
            System.out.println("Error saving results: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static double calculateStdDev(List<RunResult> results, java.util.function.ToDoubleFunction<RunResult> mapper, double mean) {
        return Math.sqrt(results.stream()
                .mapToDouble(mapper)
                .map(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0));
    }

    private void logProgress(String paramName, Object value, List<RunResult> results) {
        // Count valid solutions
        long validCount = results.stream()
                .filter(RunResult::isCombinationValid)
                .count();
        // Calculate average teacher count only for valid solutions
        String teacherStats;
        if (validCount > 0) {
            double avgTeachers = results.stream()
                    .filter(RunResult::isCombinationValid)
                    .mapToInt(RunResult::getTeacherCount)
                    .average()
                    .orElse(0);
            teacherStats = String.format("%.2f", avgTeachers);
        } else {
            teacherStats = "N/A";
        }
        double avgTime = results.stream()
                .mapToLong(RunResult::getExecutionTimeMs)
                .average()
                .orElse(0);
        System.out.println(String.format(
                "%s = %s: Teachers = %s, Valid = %d/%d, Time = %.0f ms",
                paramName, value, teacherStats, validCount, results.size(), avgTime
        ));
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

    //    public static void main(String[] args) {
//        String inputFile = "/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/TimetablingAlgorithmPackage/data/ch1-3th-s.txt";
//        String outputDir = "/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/TimetablingAlgorithmPackage/src/experiment/ga/ch13ths";
//
//        // Number of runs per configuration
//        int runsPerConfig = 10;
//
//        System.out.println("Starting GA Parameter Experiments");
//        System.out.println("Input file: " + inputFile);
//        System.out.println("Output directory: " + outputDir);
//        System.out.println("Runs per configuration: " +  runsPerConfig);
//
//        GAExperiment experiment = new GAExperiment(inputFile, outputDir, runsPerConfig);
//        experiment.runAllExperiments();
//    }
    public static void main(String[] args){
        // Base directory containing the data files
        String dataDir = "/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/TimetablingAlgorithmPackage/data";
        String outputDir = "/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/TimetablingAlgorithmPackage/src/experiment/ga/results";
        int runsPerInstance = 10;

        // Create timestamp for this run
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String resultsDir = outputDir + "/" + timestamp;
        new File(resultsDir).mkdirs();

        // Get all .txt files from the data directory
        File folder = new File(dataDir);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (listOfFiles != null) {
            System.out.println("Starting experiments with " + listOfFiles.length + " instances");
            System.out.println("Each instance will be run " + runsPerInstance + " times");
            System.out.println("Results will be saved in: " + resultsDir);

            // Process each file
            for (File file : listOfFiles) {
                String instanceName = file.getName().replace(".txt", "");
                System.out.println("\nProcessing instance: " + instanceName);

                // Create experiment instance
                GAExperiment experiment = new GAExperiment(
                        file.getAbsolutePath(),
                        resultsDir,
                        runsPerInstance
                );

                // Run with default configuration
                ExperimentConfig config = new ExperimentConfig(500, 750, 0.4, 0.75);
                List<RunResult> results = experiment.runExperimentWithConfig(config);

                // Save results for this instance
                try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/" + instanceName + "_results.txt"))) {
                    // Write instance info
                    writer.println("Results for instance: " + instanceName);
                    writer.println("Configuration: Default (pop=500, gen=750, mut=0.4, cross=0.75)");
                    writer.println("Number of runs: " + runsPerInstance);
                    writer.println();

                    // Write individual run results
                    writer.println("Individual Run Results:");
                    writer.println("Run\tTeachers\tValid\tTime(ms)\tGenerations");
                    for (int i = 0; i < results.size(); i++) {
                        RunResult run = results.get(i);
                        writer.println(String.format("%d\t%s\t%b\t%d\t%d",
                                i + 1,
                                run.isCombinationValid() ? run.getTeacherCount() : "N/A",
                                run.isCombinationValid(),
                                run.getExecutionTimeMs(),
                                run.getGenerationsUntilBest()
                        ));
                    }
                    writer.println();

                    // Calculate and write aggregated results
                    long validSolutions = results.stream()
                            .filter(RunResult::isCombinationValid)
                            .count();
                    double validRate = validSolutions * 100.0 / results.size();

                    writer.println("Aggregated Results:");
                    writer.println(String.format("Valid solutions: %d/%d (%.1f%%)",
                            validSolutions, results.size(), validRate));

                    // Teacher count statistics (only for valid solutions)
                    if (validSolutions > 0) {
                        double avgTeachers = results.stream()
                                .filter(RunResult::isCombinationValid)
                                .mapToInt(RunResult::getTeacherCount)
                                .average()
                                .orElse(0);
                        double stdDevTeachers = calculateStdDev(
                                results.stream().filter(RunResult::isCombinationValid).collect(java.util.stream.Collectors.toList()),
                                r -> r.getTeacherCount(),
                                avgTeachers
                        );
                        writer.println(String.format("Teacher Count (valid solutions only): %.2f (±%.2f)", avgTeachers, stdDevTeachers));
                        writer.println("Best Teacher Count: " + results.stream()
                                .filter(RunResult::isCombinationValid)
                                .mapToInt(RunResult::getTeacherCount)
                                .min().orElse(0));
                        writer.println("Worst Teacher Count: " + results.stream()
                                .filter(RunResult::isCombinationValid)
                                .mapToInt(RunResult::getTeacherCount)
                                .max().orElse(0));
                    } else {
                        writer.println("No valid solutions found - teacher count statistics unavailable");
                    }

                    // Time statistics
                    double avgTime = results.stream()
                            .mapToLong(RunResult::getExecutionTimeMs)
                            .average()
                            .orElse(0);
                    double stdDevTime = calculateStdDev(results, RunResult::getExecutionTimeMs, avgTime);
                    writer.println(String.format("\nExecution Time: %.2f ms (±%.2f)", avgTime, stdDevTime));
                    writer.println("Fastest Run: " + results.stream().mapToLong(RunResult::getExecutionTimeMs).min().orElse(0) + " ms");
                    writer.println("Slowest Run: " + results.stream().mapToLong(RunResult::getExecutionTimeMs).max().orElse(0) + " ms");
                    // Convergence statistics
                    double avgGens = results.stream()
                            .mapToInt(RunResult::getGenerationsUntilBest)
                            .average()
                            .orElse(0);
                    double stdDevGens = calculateStdDev(results, RunResult::getGenerationsUntilBest, avgGens);
                    writer.println(String.format("\nGenerations until best: %.2f (±%.2f)", avgGens, stdDevGens));
                    writer.println("Fastest Convergence: " + results.stream().mapToInt(RunResult::getGenerationsUntilBest).min().orElse(0) + " generations");
                    writer.println("Slowest Convergence: " + results.stream().mapToInt(RunResult::getGenerationsUntilBest).max().orElse(0) + " generations");
                    System.out.println("Completed " + instanceName +
                            String.format(": Valid = %.1f%%, Time = %.0f ms",
                                    validRate, avgTime));
                }catch (IOException e) {
                    System.out.println("Error writing results for " + instanceName + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No data files found in " + dataDir);
        }
    }
}