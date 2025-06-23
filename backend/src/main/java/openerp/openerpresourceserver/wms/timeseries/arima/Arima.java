/*
 * Copyright (c) 2017-present, Workday, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the LICENSE file in the root repository.
 */

package openerp.openerpresourceserver.wms.timeseries.arima;


import openerp.openerpresourceserver.wms.timeseries.arima.struct.ArimaModel;
import openerp.openerpresourceserver.wms.timeseries.arima.struct.ArimaParams;
import openerp.openerpresourceserver.wms.timeseries.arima.struct.ForecastResult;
import openerp.openerpresourceserver.wms.timeseries.timeseriesutil.ForecastUtil;

/**
 * ARIMA implementation
 */
public final class Arima {

    private Arima() {
    } // pure static class

    /**
     * Raw-level ARIMA forecasting function.
     *
     * @param data UNMODIFIED, list of double numbers representing time-series with constant time-gap
     * @param forecastSize integer representing how many data points AFTER the data series to be
     *        forecasted
     * @param params ARIMA parameters
     * @return a ForecastResult object, which contains the forecasted values and/or error message(s)
     */
    public static ForecastResult forecast_arima(final double[] data, final int forecastSize, ArimaParams params) {

        try {
            final int p = params.p;
            final int d = params.d;
            final int q = params.q;
            final int P = params.P;
            final int D = params.D;
            final int Q = params.Q;
            final int m = params.m;
            final ArimaParams paramsForecast = new ArimaParams(p, d, q, P, D, Q, m);
            final ArimaParams paramsXValidation = new ArimaParams(p, d, q, P, D, Q, m);
            // estimate ARIMA model parameters for forecasting
            final ArimaModel fittedModel = ArimaSolver.estimateARIMA(
                    paramsForecast, data, data.length, data.length + 1);

            // compute RMSE to be used in confidence interval computation
            final double rmseValidation = ArimaSolver.computeRMSEValidation(
                    data, ForecastUtil.testSetPercentage, paramsXValidation);
            fittedModel.setRMSE(rmseValidation);
            final ForecastResult forecastResult = fittedModel.forecast(forecastSize);

            // populate confidence interval
            forecastResult.setSigma2AndPredicationInterval(fittedModel.getParams());

            // Store the model parameters in the result
            forecastResult.setModelParams(fittedModel.getParams());

            // add logging messages
            forecastResult.log("{" +
                    "\"Best ModelInterface Param\" : \"" + fittedModel.getParams().summary() + "\"," +
                    "\"Forecast Size\" : \"" + forecastSize + "\"," +
                    "\"Input Size\" : \"" + data.length + "\"" +
                    "}");

            System.out.println("ARIMA parameters: " + params.summary());
            // successfully built ARIMA model and its forecast
            return forecastResult;

        } catch (final Exception ex) {
            // failed to build ARIMA model
            throw new RuntimeException("Failed to build ARIMA forecast: " + ex.getMessage());
        }
    }
    /**
     * Automatically select parameters and forecast using ARIMA
     *
     * @param data UNMODIFIED, list of double numbers representing time-series with constant time-gap
     * @param forecastSize integer representing how many data points AFTER the data series to be
     *        forecasted
     * @return a ForecastResult object, which contains the forecasted values and/or error message(s)
     */
    public static ForecastResult auto_forecast_arima(final double[] data, final int forecastSize) {
        try {
            // Auto-detect differencing order
            int d = AutoArimaSelector.determineOptimalDifferencingOrder(data);
            
            // Auto-detect seasonality
            int m = AutoArimaSelector.detectSeasonalPeriod(data, Math.min(data.length/2, 50));
            
            // Validate if seasonality is statistically significant
            boolean isSeasonal = AutoArimaSelector.isSeasonalitySignificant(data, m);
            
            // Default parameter ranges
            int maxP = 7;
            int maxQ = 7;
            int maxSP = 5;
            int maxSD = 2;
            int maxSQ = 5;
            
            // Define seasonal periods to test - use empty array if no significant seasonality
            int[] possibleSeasons;
            if (isSeasonal) {
                possibleSeasons = new int[]{m}; // Common seasonal periods
            } else {
                possibleSeasons = new int[]{}; // Force non-seasonal model
            }
            
            // Find best parameters
            ArimaParams bestParams = AutoArimaSelector.findBestParameters(
                data, maxP, d, maxQ, maxSP, maxSD, maxSQ, possibleSeasons);

            // log the best parameters found
            System.out.println("Best ARIMA parameters found: " + bestParams.summary());
                
            // If no valid model found, fall back to simple AR(1) model
            if (bestParams == null) {
                bestParams = new ArimaParams(1, d, 0, 0, 0, 0, 0);
            }
            
            // Log the selected parameters
//            System.out.println("Auto-selected ARIMA parameters: " + bestParams.summary());
            System.out.println("Data appears to be " + (isSeasonal ? "seasonal with period " + m : "non-seasonal"));

            // Forecast with selected parameters
            return forecast_arima(data, forecastSize, bestParams);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to auto-select ARIMA parameters: " + ex.getMessage());
        }
    }
}
