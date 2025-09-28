package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.forecast.DailyProductForecastDTO;
import openerp.openerpresourceserver.wms.dto.forecast.WeeklyProductForecastDTO;

import java.util.List;

public interface ForecastService {
    ApiResponse<List<DailyProductForecastDTO>> forecastDailyLowStockProducts();
    
    // New method for weekly forecasting
    ApiResponse<List<WeeklyProductForecastDTO>> forecastWeeklyLowStockProducts();
}