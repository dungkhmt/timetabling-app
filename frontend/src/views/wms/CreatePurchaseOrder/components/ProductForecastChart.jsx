import React, { useMemo } from 'react';
import { Card, CardContent, Typography, Box, Chip, Grid, Divider } from '@mui/material';
import ReactECharts from 'echarts-for-react';
import { TrendingUp, Inventory, ShowChart } from '@mui/icons-material';

const ProductForecastChart = ({ forecastData }) => {
  const chartOption = useMemo(() => {
    if (!forecastData || !forecastData.historicalData || !forecastData.forecastData) {
      return {};
    }

    // Chuẩn bị dữ liệu
    const historicalEntries = Object.entries(forecastData.historicalData).sort(([a], [b]) => new Date(a) - new Date(b));
    const forecastEntries = Object.entries(forecastData.forecastData).sort(([a], [b]) => new Date(a) - new Date(b));
    
    // Tạo mảng dates và data
    const allDates = [
      ...historicalEntries.map(([date]) => date),
      ...forecastEntries.map(([date]) => date)
    ];
    
    const historicalValues = historicalEntries.map(([, value]) => value);
    const forecastValues = forecastEntries.map(([, value]) => value); // Đổi tên biến từ forecastData thành forecastValues
    
    // Tìm điểm phân cách giữa lịch sử và dự báo
    const todayIndex = historicalEntries.length - 1;

    return {
      title: {
        text: `Biểu đồ dự báo: ${forecastData.productName}`,
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
        data: allDates,
        axisLabel: {
          formatter: function (value) {
            const date = new Date(value);
            return `${date.getDate()}/${date.getMonth() + 1}`;
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
            ...new Array(forecastValues.length).fill(null) // Sử dụng forecastValues thay vì forecastData
          ],
          lineStyle: {
            color: '#2196f3',
            width: 2
          },
          itemStyle: {
            color: '#2196f3'
          },
          symbol: 'circle',
          symbolSize: 6,
          connectNulls: false
        },
        {
          name: 'Dự báo',
          type: 'line',
          data: [
            ...new Array(historicalValues.length).fill(null),
            ...forecastValues // Sử dụng forecastValues thay vì forecastData
          ],
          lineStyle: {
            color: '#ff9800',
            width: 2,
            type: 'dashed'
          },
          itemStyle: {
            color: '#ff9800'
          },
          symbol: 'circle',
          symbolSize: 6,
          connectNulls: false
        }
      ],
      markLine: {
        data: [
          {
            name: 'Hôm nay',
            xAxis: todayIndex < allDates.length ? allDates[todayIndex] : allDates[allDates.length - 1],
            lineStyle: {
              color: '#666',
              type: 'dashed',
              width: 1
            },
            label: {
              show: true,
              position: 'end',
              formatter: 'Hôm nay'
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
          <Typography color="textSecondary">Không có dữ liệu dự báo</Typography>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box display="flex" alignItems="center" mb={2}>
          <ShowChart color="primary" sx={{ mr: 1 }} />
          <Typography variant="h6" component="h3" sx={{ flexGrow: 1 }}>
            {forecastData.productName}
          </Typography>
          <Chip 
            label={forecastData.modelInfo || 'ARIMA'} 
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
                Dự báo 7 ngày
              </Typography>
            </Box>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Box textAlign="center">
              <Typography variant="h6">
                {forecastData.averageDailyOutbound || 0}
              </Typography>
              <Typography variant="caption" color="textSecondary">
                Trung bình/ngày
              </Typography>
            </Box>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Box textAlign="center">
              <Typography variant="h6">
                {forecastData.maxDailyOutbound || 0}
              </Typography>
              <Typography variant="caption" color="textSecondary">
                Cao nhất/ngày
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
            <strong>Mô hình:</strong> {forecastData.modelInfo || 'Auto ARIMA'} • 
            <strong> Độ tin cậy:</strong> {((forecastData.confidenceLevel || 0.95) * 100).toFixed(0)}% • 
            <strong> Đơn vị:</strong> {forecastData.unit || 'N/A'}
            {forecastData.mse && (
              <>
                {' • '}
                <strong> MSE:</strong> {forecastData.mse.toFixed(2)}
              </>
            )}
            {forecastData.rmse && (
              <>
                {' • '}
                <strong> RMSE:</strong> {forecastData.rmse.toFixed(2)}
              </>
            )}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
};

export default ProductForecastChart;