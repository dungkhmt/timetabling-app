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

    // Chuẩn bị dữ liệu khoảng tin cậy
    const upperBoundsEntries = forecastData.weeklyUpperBounds ?
        Object.entries(forecastData.weeklyUpperBounds).sort(([a], [b]) => a.localeCompare(b)) : [];
    const lowerBoundsEntries = forecastData.weeklyLowerBounds ?
        Object.entries(forecastData.weeklyLowerBounds).sort(([a], [b]) => a.localeCompare(b)) : [];

    // Tạo mảng weeks và data
    const allWeeks = [
      ...historicalEntries.map(([week]) => week),
      ...forecastEntries.map(([week]) => week)
    ];

    const historicalValues = historicalEntries.map(([, value]) => value);
    const forecastValues = forecastEntries.map(([, value]) => value);
    const upperBoundValues = upperBoundsEntries.map(([, value]) => value);
    const lowerBoundValues = lowerBoundsEntries.map(([, value]) => value);

    // Tìm điểm phân cách giữa lịch sử và dự báo
    const todayIndex = historicalEntries.length - 1;

    // Tạo series
    const series = [
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
        connectNulls: false,
        z: 3
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
        connectNulls: false,
        z: 3
      }
    ];

    // Thêm khoảng tin cậy nếu có dữ liệu
    if (upperBoundValues.length > 0 && lowerBoundValues.length > 0) {
      // Đường giới hạn trên
      series.push({
        name: 'Khoảng tin cậy trên',
        type: 'line',
        data: [
          ...new Array(historicalValues.length).fill(null),
          ...upperBoundValues
        ],
        lineStyle: {
          color: '#4caf50',
          width: 2,
          type: 'dotted'
        },
        itemStyle: {
          color: '#4caf50'
        },
        symbol: 'triangle',
        symbolSize: 6,
        connectNulls: false,
        z: 2
      });

      // Đường giới hạn dưới
      series.push({
        name: 'Khoảng tin cậy dưới',
        type: 'line',
        data: [
          ...new Array(historicalValues.length).fill(null),
          ...lowerBoundValues
        ],
        lineStyle: {
          color: '#f44336',
          width: 2,
          type: 'dotted'
        },
        itemStyle: {
          color: '#f44336'
        },
        symbol: 'triangle',
        symbolSize: 6,
        connectNulls: false,
        z: 2
      });
    }

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
          const unit = forecastData.unit || '';

          params.forEach(param => {
            const seriesName = param.seriesName;
            const value = param.value;

            // Chỉ hiển thị các series chính và khoảng tin cậy
            if (value !== null && value !== undefined &&
                ['Lịch sử xuất kho', 'Dự báo', 'Khoảng tin cậy trên', 'Khoảng tin cậy dưới'].includes(seriesName)) {
              result += `<div style="color: ${param.color};">
                ${seriesName}: ${value} ${unit}
              </div>`;
            }
          });
          return result;
        }
      },
      legend: {
        data: upperBoundValues.length > 0 ?
            ['Lịch sử xuất kho', 'Dự báo', 'Khoảng tin cậy trên', 'Khoảng tin cậy dưới'] :
            ['Lịch sử xuất kho', 'Dự báo'],
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
      series: series,
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
              {/*{forecastData.rmse && (*/}
              {/*    <>*/}
              {/*      {' • '}*/}
              {/*      <strong> RMSE:</strong> {forecastData.rmse.toFixed(2)}*/}
              {/*    </>*/}
              {/*)}*/}
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