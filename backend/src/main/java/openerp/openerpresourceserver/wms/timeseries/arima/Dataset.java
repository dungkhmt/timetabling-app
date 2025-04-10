/*
 * Copyright (c) 2017-present, Workday, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the LICENSE file in the root repository.
 */

package openerp.openerpresourceserver.wms.timeseries.arima;

/**
 * Collection of datasets for testing ARIMA forecasting,
 * clearly separated into seasonal and non-seasonal patterns
 */
public class Dataset {

    /**
     * ====================================
     * NON-SEASONAL DATA
     * ====================================
     */

    // Random walk (non-seasonal, requires differencing)
    public static final double[] random_walk = {
            100.00, 99.26, 100.15, 101.67, 102.27, 102.49, 102.06, 101.25, 99.81, 100.38,
            99.61, 99.57, 98.50, 98.30, 97.47, 97.60, 97.82, 98.08, 98.03, 97.78,
            97.36, 97.38, 96.85, 95.23, 95.39, 97.32, 97.67, 97.89, 98.32, 98.67,
            99.49, 100.83, 100.43, 100.56, 100.04, 99.53, 100.45, 100.12, 98.85, 99.52,
            99.81, 99.82, 100.83, 101.12, 101.23, 102.52, 103.08, 104.41, 104.44, 104.65
    };

    // COVID-19 daily cases (epidemic curve, non-seasonal)
// Update the covid_cases data with this more appropriate non-seasonal epidemic wave
    public static final double[] covid_cases = {
            // Single wave epidemic curve - daily new cases (no weekly reporting patterns)
            1, 3, 7, 12, 18, 27, 39, 54, 72, 93, 119, 148, 182, 221, 265, 311,
            362, 413, 468, 519, 568, 612, 647, 675, 693, 701, 697, 684, 663,
            634, 598, 559, 518, 476, 432, 391, 349, 309, 274, 239, 207, 179,
            152, 129, 109, 91, 76, 63, 52, 43, 35, 29, 24, 19, 16, 13, 10, 8, 7, 5
    };

    // Stock price data (trend pattern with volatility, non-seasonal)
    public static final double[] stock_prices = {
            145.32, 146.89, 146.15, 145.87, 148.23, 150.14, 149.92, 149.05, 152.37,
            154.29, 153.78, 155.02, 157.19, 159.84, 158.92, 160.41, 162.73, 161.98,
            164.25, 167.18, 166.43, 165.89, 167.54, 168.97, 170.32, 172.56, 173.21,
            172.64, 173.95, 176.28
    };

    // US consumption expenditure (linear trend, non-seasonal)
    public static final double[] us_consumption = {
            5764.0, 5821.0, 5878.0, 5951.0, 6019.0, 6088.0, 6151.0, 6228.0, 6304.0, 6385.0,
            6467.0, 6559.0, 6665.0, 6731.0, 6801.0, 6876.0, 6968.0, 7040.0, 7099.0, 7170.0,
            7258.0, 7339.0, 7431.0, 7513.0, 7582.0, 7658.0, 7701.0, 7774.0, 7846.0, 7930.0,
            8032.0, 8138.0, 8273.0, 8407.0, 8527.0, 8609.0, 8692.0, 8775.0
    };

    // Simple AR(1) process (non-seasonal)
    public static final double[] ar1_process = {
            0.00, 0.41, 0.65, 0.94, 1.45, 1.17, 1.28, 1.15, 1.63, 1.46,
            1.53, 1.80, 1.73, 1.69, 1.91, 1.86, 1.70, 1.72, 1.98, 2.15,
            2.07, 2.15, 2.00, 2.13, 2.06, 2.12, 1.93, 1.70, 1.93, 2.16
    };

    /**
     * ====================================
     * SEASONAL DATA
     * ====================================
     */

    // Monthly temperature data (yearly seasonality, m=12)
    public static final double[] monthly_temps = {
            // Jan-Dec temperature values for 5 years (°C)
            -2.3, -1.4, 3.5, 9.1, 15.2, 19.7, 22.8, 21.9, 17.3, 11.2, 5.6, 0.2,  // Year 1
            -3.1, -2.0, 2.9, 8.7, 14.9, 20.3, 23.1, 22.4, 16.9, 10.5, 4.8, -0.4, // Year 2
            -2.7, -1.8, 3.2, 9.4, 15.7, 19.9, 23.5, 22.1, 17.8, 11.7, 5.2, -0.1, // Year 3
            -2.1, -1.1, 3.8, 9.8, 16.1, 20.5, 24.0, 22.6, 18.1, 12.0, 5.9, 0.5,  // Year 4
            -1.9, -0.8, 4.1, 10.2, 16.5, 21.0, 24.3, 23.0, 18.7, 12.4, 6.2, 0.9  // Year 5
    };

    // Daily website traffic (weekly seasonality, m=7)
    public static final double[] website_traffic = {
            // Daily page views (thousands) for 6 weeks
            12.5, 10.3, 9.8, 10.1, 10.7, 15.2, 17.9,  // Week 1
            13.1, 10.7, 10.2, 10.5, 11.2, 16.1, 18.3,  // Week 2
            13.7, 11.1, 10.4, 10.9, 11.7, 16.8, 19.1,  // Week 3
            14.2, 11.4, 10.9, 11.3, 12.3, 17.4, 19.8,  // Week 4
            14.9, 12.0, 11.3, 11.8, 12.8, 18.1, 20.4,  // Week 5
            15.5, 12.6, 11.7, 12.3, 13.4, 18.9, 21.2   // Week 6
    };

    // Quarterly sales data (quarterly seasonality, m=4)
    public static final double[] quarterly_sales = {
            // Quarterly sales (millions) over 8 years
            2.3, 2.5, 3.1, 3.8,  // Year 1
            2.7, 2.9, 3.5, 4.2,  // Year 2
            3.1, 3.3, 4.0, 4.8,  // Year 3
            3.5, 3.7, 4.5, 5.2,  // Year 4
            3.9, 4.1, 5.0, 5.6,  // Year 5
            4.2, 4.4, 5.3, 6.1,  // Year 6
            4.5, 4.7, 5.7, 6.5,  // Year 7
            4.8, 5.0, 6.1, 7.0   // Year 8
    };

    // Chicago potholes (biweekly seasonality, m=14)
    public static final double[] chicago_potholes = {
            1, 2, 4, 8, 12, 14, 22, 27, 21, 21, 10, 4, 2, 1, 1, 3, 8, 9, 15, 16, 19,
            23, 18, 12, 7, 2, 1, 1, 2, 5, 8, 12, 22, 24, 29, 26, 23, 14, 8, 4, 1, 1, 3,
            7, 10, 13, 20, 25, 27, 23, 15, 9, 3, 2, 2, 3, 9, 14, 17, 20, 26, 29, 23, 16,
            8, 4, 2, 1, 2, 5, 12, 16, 21, 25, 28, 21, 14, 9, 3, 2
    };

    // Australia beer production (quarterly seasonality with trend, m=4)
    public static final double[] aus_beer = {
            284, 213, 227, 308, 262, 228, 236, 320, 272, 233, 237, 313, 261, 227, 250,
            314, 286, 227, 260, 311, 295, 233, 257, 339, 279, 250, 270, 346, 294, 255,
            278, 363, 313, 273, 300, 370, 331, 288, 306, 386, 335, 288, 308, 402, 353,
            316, 325, 405, 393, 319, 327, 442, 383, 332, 361, 446, 387, 357, 374, 466
    };

    // Sunspots (yearly seasonality with 11-year cycle, m=11)
    public static final double[] sunspots = {
            5, 11, 16, 23, 36, 58, 29, 20, 10, 8, 3, 0, 0, 2, 11, 27,
            47, 63, 60, 39, 28, 26, 22, 11, 21, 40, 78, 122, 103, 73,
            47, 35, 11, 5, 16, 34, 70, 81, 111, 101, 73, 40, 20, 16, 5, 11, 22, 40, 60, 80.9, 83.4,
            47.7, 47.8, 30.7, 12.2, 9.6, 10.2, 32.4, 47.6, 54, 62.9, 85.9, 61.2, 45.1, 36.4, 20.9
    };
}