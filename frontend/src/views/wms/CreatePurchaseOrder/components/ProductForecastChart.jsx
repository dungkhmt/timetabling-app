import React, { useMemo } from 'react';
import { Card, CardContent, Typography, Box, Chip, Grid, Divider } from '@mui/material';
import ReactECharts from 'echarts-for-react';
import { TrendingUp, Inventory, ShowChart, DateRange } from '@mui/icons-material';

const ProductForecastChart = ({ forecastData }) => {
  const chartOption = useMemo(() => {
    if (!forecastData || !forecastData.historicalWeeklyData || !forecastData.weeklyForecastData) {
      return {};
    }

    // Chuẩn bị dữ liệu tuần
    const historicalEntries = Object.entries(forecastData.historicalWeeklyData)
      .sort(([a], [b]) => a.localeCompare(b));
    const forecastEntries = Object.entries(forecastData.weeklyForecastData)
      .sort(([a], [b]) => a.localeCompare(b));
    
    // Tạo mảng weeks và data
    const allWeeks = [
      ...historicalEntries.map(([week]) => week),
      ...forecastEntries.map(([week]) => week)
    ];
    
    const historicalValues = historicalEntries.map(([, value]) => value);
    const forecastValues = forecastEntries.map(([, value]) => value);
    
    // Tìm điểm phân cách giữa lịch sử và dự báo
    const todayIndex = historicalEntries.length - 1;

    return {
      title: {
        text: `Dự báo theo tuần: ${forecastData.productName}`,
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'normal'
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: function (params) {
          let result = `<div style="font-weight: bold;">${params[0].axisValue}</div>`;
          params.forEach(param => {
            const seriesName = param.seriesName;
            const value = param.value;
            const unit = forecastData.unit || '';
            if (value !== null && value !== undefined) {
              result += `<div style="color: ${param.color};">
                ${seriesName}: ${value} ${unit}
              </div>`;
            }
          });
          return result;
        }
      },
      legend: {
        data: ['Lịch sử xuất kho', 'Dự báo'],
        top: 30
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: allWeeks,
        axisLabel: {
          formatter: function (value) {
            return value.replace('-W', '\nT');
          },
          rotate: 45
        }
      },
      yAxis: {
        type: 'value',
        name: `Số lượng (${forecastData.unit || ''})`,
        nameLocation: 'middle',
        nameGap: 50,
        min: 0
      },
      series: [
        {
          name: 'Lịch sử xuất kho',
          type: 'line',
          data: [
            ...historicalValues,
            ...new Array(forecastValues.length).fill(null)
          ],
          lineStyle: {
            color: '#2196f3',
            width: 3
          },
          itemStyle: {
            color: '#2196f3'
          },
          symbol: 'circle',
          symbolSize: 8,
          connectNulls: false
        },
        {
          name: 'Dự báo',
          type: 'line',
          data: [
            ...new Array(historicalValues.length).fill(null),
            ...forecastValues
          ],
          lineStyle: {
            color: '#ff9800',
            width: 3,
            type: 'dashed'
          },
          itemStyle: {
            color: '#ff9800'
          },
          symbol: 'diamond',
          symbolSize: 8,
          connectNulls: false
        }
      ],
      markLine: {
        data: [
          {
            name: 'Tuần hiện tại',
            xAxis: todayIndex < allWeeks.length ? allWeeks[todayIndex] : allWeeks[allWeeks.length - 1],
            lineStyle: {
              color: '#666',
              type: 'dashed',
              width: 1
            },
            label: {
              show: true,
              position: 'end',
              formatter: 'Tuần hiện tại'
            }
          }
        ]
      }
    };
  }, [forecastData]);

  if (!forecastData) {
    return (
      <Card>
        <CardContent>
          <Typography color="textSecondary">Không có dữ liệu dự báo theo tuần</Typography>
        </CardContent>
      </Card>
    );
  }

  const trendColor = forecastData.trend >= 0 ? 'success' : 'error';

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box display="flex" alignItems="center" mb={2}>
          <DateRange color="primary" sx={{ mr: 1 }} />
          <Typography variant="h6" component="h3" sx={{ flexGrow: 1 }}>
            {forecastData.productName}
          </Typography>
          <Chip 
            label={`${forecastData.trend > 0 ? '+' : ''}${forecastData.trend?.toFixed(1)}%`}
            color={trendColor}
            size="small"
            sx={{ mr: 1 }}
          />
          <Chip 
            label={forecastData.modelInfo || 'ARIMA (Weekly)'}
            variant="outlined" 
            size="small" 
            color="primary"
          />
        </Box>

        <Grid container spacing={2} mb={3}>
          <Grid item xs={6} sm={3}>
            <Box textAlign="center">
              <Typography variant="h6" color="primary">
                {forecastData.currentStock || 0}
              </Typography>
              <Typography variant="caption" color="textSecondary">
                <Inventory fontSize="small" sx={{ verticalAlign: 'middle', mr: 0.5 }} />
                Tồn kho hiện tại
              </Typography>
            </Box>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Box textAlign="center">
              <Typography variant="h6" color="secondary">
                {forecastData.totalPredictedQuantity || 0}
              </Typography>
              <Typography variant="caption" color="textSecondary">
                <TrendingUp fontSize="small" sx={{ verticalAlign: 'middle', mr: 0.5 }} />
                Dự báo 4 tuần
              </Typography>
            </Box>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Box textAlign="center">
              <Typography variant="h6">
                {forecastData.averageWeeklyQuantity || 0}
              </Typography>
              <Typography variant="caption" color="textSecondary">
                Trung bình/tuần
              </Typography>
            </Box>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Box textAlign="center">
              <Typography variant="h6" color={forecastData.weeksUntilStockout <= 4 ? 'error' : 'inherit'}>
                {forecastData.weeksUntilStockout || 0}
              </Typography>
              <Typography variant="caption" color="textSecondary">
                Tuần nữa hết hàng
              </Typography>
            </Box>
          </Grid>
        </Grid>

        <Divider sx={{ mb: 2 }} />

        <Box height={400}>
          <ReactECharts
            option={chartOption}
            style={{ height: '100%', width: '100%' }}
            opts={{ renderer: 'svg' }}
          />
        </Box>

        <Box mt={2}>
          <Typography variant="body2" color="textSecondary">
            <strong>Mô hình:</strong> {forecastData.modelInfo || 'ARIMA (Weekly)'} •
            <strong> Độ tin cậy:</strong> {(forecastData.confidenceLevel || 75).toFixed(0)}% • 
            <strong> Đơn vị:</strong> {forecastData.unit || 'N/A'}
            {forecastData.rmse && (
              <>
                {' • '}
                <strong> RMSE:</strong> {forecastData.rmse.toFixed(2)}
              </>
            )}
            {forecastData.trend && (
              <>
                {' • '}
                <strong> Xu hướng:</strong> {forecastData.trend > 0 ? '↗' : '↘'} {Math.abs(forecastData.trend).toFixed(1)}%
              </>
            )}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
};

export default ProductForecastChart;