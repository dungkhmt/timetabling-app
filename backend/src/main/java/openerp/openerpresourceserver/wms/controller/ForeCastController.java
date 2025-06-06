package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.forecast.DailyProductForecastDTO;
import openerp.openerpresourceserver.wms.service.ForecastService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/forecast")
@RestController
@RequiredArgsConstructor
public class ForeCastController {
    private final ForecastService forecastService;

    @GetMapping("/daily-low-stock")
    public ApiResponse<List<DailyProductForecastDTO>> getDailyLowStockForecast() {
        return forecastService.forecastDailyLowStockProducts();
    }
}