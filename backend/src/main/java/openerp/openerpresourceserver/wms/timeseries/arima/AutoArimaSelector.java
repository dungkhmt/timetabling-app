package openerp.openerpresourceserver.wms.timeseries.arima;



import openerp.openerpresourceserver.wms.timeseries.arima.struct.ArimaParams;
import openerp.openerpresourceserver.wms.timeseries.arima.struct.ForecastResult;
import openerp.openerpresourceserver.wms.timeseries.timeseriesutil.ForecastUtil;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Automatic ARIMA parameter selection through Bayesian Optimization
 */
public final class AutoArimaSelector {

    private AutoArimaSelector() {
    }

    // Number of initial random explorations before starting Bayesian optimization
    private static final int INITIAL_POINTS = 10;

    // Number of optimization iterations
    private static final int OPTIMIZATION_ITERATIONS = 25;

    // Exploration-exploitation trade-off parameter
    private static final double KAPPA = 2.576;

    /**
     * Find optimal ARIMA parameters using Bayesian Optimization
     *
     * @param data input time series data
     * @param maxP maximum value for non-seasonal AR order
     * @param maxD maximum value for non-seasonal differencing
     * @param maxQ maximum value for non-seasonal MA order
     * @param maxSP maximum value for seasonal AR order
     * @param maxSD maximum value for seasonal differencing
     * @param maxSQ maximum value for seasonal MA order
     * @param seasonalPeriods array of possible seasonal periods to test
     * @return the optimal ARIMA parameters
     */
    public static ArimaParams findBestParameters(double[] data,
                                                 int maxP, int maxD, int maxQ,
                                                 int maxSP, int maxSD, int maxSQ,
                                                 int[] seasonalPeriods) {
        // Split data for validation
        int trainSize = (int)(data.length * (1 - ForecastUtil.testSetPercentage));
        double[] trainData = new double[trainSize];
        System.arraycopy(data, 0, trainData, 0, trainSize);

        // Setup parameter space bounds
        Map<String, int[]> paramBounds = new HashMap<>();
        paramBounds.put("p", new int[]{1, maxP});
        paramBounds.put("d", new int[]{maxD});
        paramBounds.put("q", new int[]{1, maxQ});
        paramBounds.put("P", new int[]{0, maxSP});
        paramBounds.put("D", new int[]{0, maxSD});
        paramBounds.put("Q", new int[]{0, maxSQ});

        // Observed points (parameters -> AIC)
        List<Map<String, Integer>> observedPoints = new ArrayList<>();
        List<Double> observedValues = new ArrayList<>();

        // Best parameters found so far
        double bestAIC = Double.POSITIVE_INFINITY;
        ArimaParams bestParams = null;

        // 1. Initial random exploration phase
        for (int i = 0; i < INITIAL_POINTS; i++) {
            Map<String, Integer> point = generateRandomPoint(paramBounds, seasonalPeriods);
            ArimaParams params = createArimaParams(point);

            if (params != null) {
                double aic = evaluateModel(trainData, params);
                observedPoints.add(point);
                observedValues.add(aic);

                if (aic < bestAIC) {
                    bestAIC = aic;
                    bestParams = params;
                }
            }
        }

        // 2. Bayesian optimization phase
        for (int iter = 0; iter < OPTIMIZATION_ITERATIONS; iter++) {
            // Generate candidate points
            List<Map<String, Integer>> candidates = generateCandidates(paramBounds, seasonalPeriods, 50);

            // Find next point to evaluate
            Map<String, Integer> nextPoint = selectNextPoint(candidates, observedPoints, observedValues, maxD);

            // Evaluate the selected point
            ArimaParams params = createArimaParams(nextPoint);
            if (params != null) {
                double aic = evaluateModel(trainData, params);
                observedPoints.add(nextPoint);
                observedValues.add(aic);

                if (aic < bestAIC) {
                    bestAIC = aic;
                    bestParams = params;
                    System.out.println("New best AIC: " + aic + " at " + nextPoint);
                }
            }
        }

        return bestParams;
    }

    /**
     * Generate a random point in the parameter space
     */
    private static Map<String, Integer> generateRandomPoint(Map<String, int[]> paramBounds, int[] seasonalPeriods) {
        Map<String, Integer> point = new HashMap<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Generate non-seasonal parameters
        for (String param : new String[]{"p", "q"}) {
            int[] bounds = paramBounds.get(param);
            point.put(param, random.nextInt(bounds[0], bounds[1] + 1));
        }

        point.put("d", paramBounds.get("d")[0]);

        // Decide if we should use seasonal components
        boolean useSeasonal = seasonalPeriods.length > 0;

        if (useSeasonal) {
            // Select a seasonal period
            int m = seasonalPeriods[random.nextInt(seasonalPeriods.length)];
            point.put("m", m);

            // Generate seasonal parameters
            for (String param : new String[]{"P", "D", "Q"}) {
                int[] bounds = paramBounds.get(param);
                point.put(param, random.nextInt(bounds[0], bounds[1] + 1));
            }

            // Ensure at least one seasonal component is non-zero
            if (point.get("P") == 0 && point.get("D") == 0 && point.get("Q") == 0) {
                String[] seasonalParams = {"P", "D", "Q"};
                String paramToChange = seasonalParams[random.nextInt(seasonalParams.length)];
                point.put(paramToChange, 1);
            }
        } else {
            // No seasonality
            point.put("P", 0);
            point.put("D", 0);
            point.put("Q", 0);
            point.put("m", 0);
        }

        return point;
    }

    /**
     * Generate candidate points for evaluation
     */
    private static List<Map<String, Integer>> generateCandidates(
            Map<String, int[]> paramBounds, int[] seasonalPeriods, int count) {
        List<Map<String, Integer>> candidates = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            candidates.add(generateRandomPoint(paramBounds, seasonalPeriods));
        }

        return candidates;
    }

    /**
     * Create ARIMA parameters object from a point in parameter space
     */
    private static ArimaParams createArimaParams(Map<String, Integer> point) {
        try {
            int p = point.get("p");
            int d = point.get("d");
            int q = point.get("q");
            int P = point.get("P");
            int D = point.get("D");
            int Q = point.get("Q");
            int m = point.get("m");

            return new ArimaParams(p, d, q, P, D, Q, m);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Select the next point to evaluate using Upper Confidence Bound (UCB)
     */
    private static Map<String, Integer> selectNextPoint(
            List<Map<String, Integer>> candidates,
            List<Map<String, Integer>> observedPoints,
            List<Double> observedValues, int maxD) {

        if (observedPoints.isEmpty()) {
            // If no points observed yet, select randomly
            return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        }

        // Normalize observed values to improve GP modeling
        double[] normalizedValues = normalizeValues(observedValues);

        Map<String, Integer> bestCandidate = null;
        double bestAcquisition = Double.NEGATIVE_INFINITY;

        for (Map<String, Integer> candidate : candidates) {
            // Skip if we've already evaluated this point
            if (observedPoints.contains(candidate)) {
                continue;
            }

            // Compute mean and variance predictions with GP
            double[] prediction = gaussianProcessPredict(candidate, observedPoints, normalizedValues);
            double mean = prediction[0];
            double stdDev = Math.sqrt(prediction[1]);

            // Upper Confidence Bound acquisition function
            double acquisition = -mean + KAPPA * stdDev;  // Negative because we minimize AIC

            if (acquisition > bestAcquisition) {
                bestAcquisition = acquisition;
                bestCandidate = candidate;
            }
        }

        // If all candidates are already evaluated, randomly select a new one
        if (bestCandidate == null) {
            return generateRandomPoint(createBoundsMap(candidates, maxD), getSPeriods(candidates));
        }

        return bestCandidate;
    }

    /**
     * Extract bounds from observed candidates
     */
    private static Map<String, int[]> createBoundsMap(List<Map<String, Integer>> candidates, int maxD) {
        Map<String, int[]> bounds = new HashMap<>();
        bounds.put("p", new int[]{0, 3});
        bounds.put("d", new int[]{maxD});
        bounds.put("q", new int[]{0, 3});
        bounds.put("P", new int[]{0, 2});
        bounds.put("D", new int[]{0, 1});
        bounds.put("Q", new int[]{0, 2});
        return bounds;
    }

    /**
     * Extract seasonal periods from observed candidates
     */
    private static int[] getSPeriods(List<Map<String, Integer>> candidates) {
        Set<Integer> periods = new HashSet<>();
        for (Map<String, Integer> point : candidates) {
            Integer m = point.get("m");
            if (m != null && m > 0) {
                periods.add(m);
            }
        }

        int[] result = new int[periods.size()];
        int i = 0;
        for (Integer period : periods) {
            result[i++] = period;
        }
        return result;
    }

    /**
     * Normalize values to have zero mean and unit variance
     */
    private static double[] normalizeValues(List<Double> values) {
        double sum = 0;
        for (Double value : values) {
            sum += value;
        }
        double mean = sum / values.size();

        double sumSquaredDiff = 0;
        for (Double value : values) {
            sumSquaredDiff += Math.pow(value - mean, 2);
        }
        double stdDev = Math.sqrt(sumSquaredDiff / values.size());

        double[] normalized = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            normalized[i] = stdDev > 0 ? (values.get(i) - mean) / stdDev : 0;
        }

        return normalized;
    }

    /**
     * Simple Gaussian Process prediction
     * Returns [mean, variance]
     */
    private static double[] gaussianProcessPredict(
            Map<String, Integer> x,
            List<Map<String, Integer>> xTrain,
            double[] yTrain) {

        // Hyperparameters
        double lengthScale = 1.0;
        double signalVariance = 1.0;
        double noiseVariance = 0.1;

        int n = xTrain.size();

        // Compute kernel between x and all training points
        double[] k = new double[n];
        for (int i = 0; i < n; i++) {
            k[i] = kernelFunction(x, xTrain.get(i), lengthScale, signalVariance);
        }

        // Compute kernel matrix of training points
        double[][] K = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                K[i][j] = kernelFunction(xTrain.get(i), xTrain.get(j), lengthScale, signalVariance);

                // Add noise to diagonal
                if (i == j) {
                    K[i][j] += noiseVariance;
                }
            }
        }

        // Compute K^-1 * y
        double[] alpha = solveLinearSystem(K, yTrain);

        // Predict mean
        double mean = 0;
        for (int i = 0; i < n; i++) {
            mean += alpha[i] * k[i];
        }

        // Predict variance
        double kStar = kernelFunction(x, x, lengthScale, signalVariance) + noiseVariance;
        double variance = kStar;

        // Compute v = K^-1 * k
        double[] v = solveLinearSystem(K, k);

        // Subtract k^T * K^-1 * k
        for (int i = 0; i < n; i++) {
            variance -= k[i] * v[i];
        }

        variance = Math.max(variance, 1e-6); // Ensure positive variance

        return new double[]{mean, variance};
    }

    /**
     * Radial Basis Function (RBF) kernel
     */
    private static double kernelFunction(
            Map<String, Integer> x1,
            Map<String, Integer> x2,
            double lengthScale,
            double signalVariance) {

        double sum = 0;
        // Only consider common keys to handle seasonal vs non-seasonal
        Set<String> keys = new HashSet<>(x1.keySet());
        keys.retainAll(x2.keySet());

        for (String key : keys) {
            int val1 = x1.get(key);
            int val2 = x2.get(key);
            sum += Math.pow(val1 - val2, 2);
        }

        // Add penalty for different sets of parameters
        if (!x1.keySet().equals(x2.keySet())) {
            sum += 4.0; // Add larger distance for structural differences
        }

        return signalVariance * Math.exp(-0.5 * sum / (lengthScale * lengthScale));
    }

    /**
     * Solve linear system Ax = b using Cholesky decomposition
     */
    private static double[] solveLinearSystem(double[][] A, double[] b) {
        int n = A.length;
        double[][] L = new double[n][n];

        // Cholesky decomposition
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = 0;
                for (int k = 0; k < j; k++) {
                    sum += L[i][k] * L[j][k];
                }

                if (i == j) {
                    L[i][j] = Math.sqrt(Math.max(A[i][i] - sum, 1e-10));
                } else {
                    L[i][j] = (A[i][j] - sum) / L[j][j];
                }
            }
        }

        // Forward substitution to solve Ly = b
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int j = 0; j < i; j++) {
                sum += L[i][j] * y[j];
            }
            y[i] = (b[i] - sum) / L[i][i];
        }

        // Backward substitution to solve L^T x = y
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0;
            for (int j = i + 1; j < n; j++) {
                sum += L[j][i] * x[j];
            }
            x[i] = (y[i] - sum) / L[i][i];
        }

        return x;
    }

    /**
     * Evaluate model using Akaike Information Criterion (AIC)
     *
     * @param data input data
     * @param params ARIMA parameters to evaluate
     * @return AIC value (lower is better)
     */
    private static double evaluateModel(double[] data, ArimaParams params) {
        try {
            // Create model and estimate
            ForecastResult result = Arima.forecast_arima(data, 1, params);
            double rmse = result.getRMSE();

            // Calculate AIC
            int numParams = params.getNumParamsP() + params.getNumParamsQ() +
                            (params.d > 0 ? 1 : 0) + (params.D > 0 ? 1 : 0);
            double aic = data.length * Math.log(rmse * rmse) + 2 * numParams;

            return aic;
        } catch (Exception e) {
            return Double.POSITIVE_INFINITY;
        }
    }

    // Keep all the original methods below

    /**
     * Determine optimal differencing order using unit root tests
     */
    public static int determineOptimalDifferencingOrder(double[] data) {
        // Simple implementation based on variance reduction
        if (data.length < 10) return 0;

        double originalVariance = calculateVariance(data);
        double[] diffOnce = difference(data);
        double varianceAfterDiff = calculateVariance(diffOnce);

        // If variance is reduced by at least 20%, suggest differencing
        if (varianceAfterDiff < 0.8 * originalVariance) {
            double[] diffTwice = difference(diffOnce);
            double varianceAfterTwoDiffs = calculateVariance(diffTwice);

            // If second differencing helps significantly, use it
            if (varianceAfterTwoDiffs < 0.8 * varianceAfterDiff) {
                return 2;
            }
            return 1;
        }

        return 0;
    }

    /**
     * Detect seasonal period in the data using autocorrelation
     */
    public static int detectSeasonalPeriod(double[] data, int maxLag) {
        if (data.length <= maxLag) return 0;

        // Calculate autocorrelations
        double[] acf = calculateACF(data, maxLag);

        // Find significant peaks in ACF
        double threshold = 1.96 / Math.sqrt(data.length); // 95% confidence
        int bestPeriod = 0;
        double maxAcf = threshold;

        // Start from lag 2 to avoid small lags
        for (int i = 2; i < acf.length; i++) {
            if (acf[i] > maxAcf) {
                maxAcf = acf[i];
                bestPeriod = i;
            }
        }

        return bestPeriod;
    }

    /**
     * Simple first-order differencing
     */
    private static double[] difference(double[] data) {
        double[] result = new double[data.length - 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = data[i + 1] - data[i];
        }
        return result;
    }

    /**
     * Calculate variance of data array
     */
    private static double calculateVariance(double[] data) {
        double mean = 0;
        for (double value : data) {
            mean += value;
        }
        mean /= data.length;

        double variance = 0;
        for (double value : data) {
            variance += (value - mean) * (value - mean);
        }
        variance /= data.length;

        return variance;
    }

    /**
     * Calculate autocorrelation function
     */
    private static double[] calculateACF(double[] data, int maxLag) {
        double mean = 0;
        for (double value : data) {
            mean += value;
        }
        mean /= data.length;

        double[] centeredData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            centeredData[i] = data[i] - mean;
        }

        double variance = 0;
        for (double value : centeredData) {
            variance += value * value;
        }

        double[] acf = new double[maxLag + 1];
        acf[0] = 1.0; // ACF at lag 0 is always 1

        for (int lag = 1; lag <= maxLag; lag++) {
            double sum = 0;
            for (int i = 0; i < data.length - lag; i++) {
                sum += centeredData[i] * centeredData[i + lag];
            }
            acf[lag] = sum / variance;
        }

        return acf;
    }

    /**
     * Determine if the time series truly has seasonality based on statistical tests
     */
    public static boolean isSeasonalitySignificant(double[] data, int potentialPeriod) {
        // Don't attempt seasonal modeling if period is too small or data too short
        if (potentialPeriod <= 1 || data.length < 2 * potentialPeriod) {
            return false;
        }

        // Check if there's enough data for at least 2 full seasonal cycles
        if (data.length < 2 * potentialPeriod) {
            return false;
        }

        // Calculate seasonal means
        double[] seasonalMeans = new double[potentialPeriod];
        int[] seasonalCounts = new int[potentialPeriod];

        for (int i = 0; i < data.length; i++) {
            int seasonIdx = i % potentialPeriod;
            seasonalMeans[seasonIdx] += data[i];
            seasonalCounts[seasonIdx]++;
        }

        for (int i = 0; i < potentialPeriod; i++) {
            seasonalMeans[i] /= seasonalCounts[i];
        }

        // Calculate overall mean
        double overallMean = 0;
        for (double value : data) {
            overallMean += value;
        }
        overallMean /= data.length;

        // Calculate between-season and within-season variance
        double betweenSeasonVar = 0;
        double withinSeasonVar = 0;

        for (int i = 0; i < potentialPeriod; i++) {
            betweenSeasonVar += seasonalCounts[i] * Math.pow(seasonalMeans[i] - overallMean, 2);
        }
        betweenSeasonVar /= (potentialPeriod - 1);

        for (int i = 0; i < data.length; i++) {
            int seasonIdx = i % potentialPeriod;
            withinSeasonVar += Math.pow(data[i] - seasonalMeans[seasonIdx], 2);
        }
        withinSeasonVar /= (data.length - potentialPeriod);

        // Calculate F-statistic (similar to ANOVA)
        double fStat = betweenSeasonVar / withinSeasonVar;

        // Use a higher threshold (e.g., 3.0) for more certainty of seasonality
        // Standard F-test would use critical values from F-distribution tables
        return fStat > 3.0;
    }
}