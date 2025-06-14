import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  Button,
  CircularProgress,
  Alert,
  Chip,
  TextField,
  InputAdornment,
  Tab,
  Tabs,
  Divider
} from '@mui/material';
import {
  Refresh,
  Search,
  Insights,
  Warning,
  TrendingUp,
  Inventory,
  Assessment,
  DateRange
} from '@mui/icons-material';
import ReactECharts from 'echarts-for-react';
import { useWms2Data } from 'services/useWms2Data';
import ProductForecastChart from './ProductForecastChart';

const TabPanel = ({ children, value, index, ...other }) => {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`forecast-tabpanel-${index}`}
      aria-labelledby={`forecast-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ pt: 2 }}>
          {children}
        </Box>
      )}
    </div>
  );
};

const ProductForecastDashboard = () => {
  const [forecastData, setForecastData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeTab, setActiveTab] = useState(0);
  const { getWeeklyLowStockForecast } = useWms2Data();

  const loadForecastData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getWeeklyLowStockForecast();
      if (response?.code === 200 && response?.data) {
        setForecastData(response.data);
      } else {
        throw new Error('Invalid response format');
      }
    } catch (err) {
      setError('Không thể tải dữ liệu dự báo theo tuần: ' + (err.message || 'Lỗi không xác định'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadForecastData();
  }, []);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const filteredData = forecastData.filter(item =>
    item.productName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    item.productId?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Calculate summary statistics for weekly data
  const totalProducts = forecastData.length;
  const totalPredictedDemand = forecastData.reduce((sum, item) => sum + (item.totalPredictedQuantity || 0), 0);
  const criticalStockItems = forecastData.filter(item => 
    (item.weeksUntilStockout || 0) <= 4 && (item.weeksUntilStockout || 0) > 0
  ).length;
  const avgConfidence = forecastData.length > 0 
    ? (forecastData.reduce((sum, item) => sum + (item.confidenceLevel || 75), 0) / forecastData.length).toFixed(0)
    : 75;

  // Biểu đồ tổng quan theo tuần
  const overviewChartOption = {
    title: {
      text: 'Tổng quan dự báo nhu cầu theo tuần',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function (params) {
        const dataIndex = params[0].dataIndex;
        const item = filteredData[dataIndex];
        return `
          <div style="font-weight: bold;">${item?.productName}</div>
          <div>Dự báo 4 tuần: ${params[0].value} ${item?.unit}</div>
          <div>Tồn kho: ${item?.currentStock || 0} ${item?.unit}</div>
          <div>TB/tuần: ${item?.averageWeeklyQuantity || 0} ${item?.unit}</div>
          <div>Tuần hết hàng: ${item?.weeksUntilStockout || 0}</div>
        `;
      }
    },
    legend: {
      data: ['Dự báo 4 tuần', 'Tồn kho hiện tại'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: filteredData.map(item => item.productName || 'N/A'),
      axisLabel: {
        rotate: 45,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: 'Số lượng',
      nameLocation: 'middle',
      nameGap: 50
    },
    series: [
      {
        name: 'Dự báo 4 tuần',
        type: 'bar',
        data: filteredData.map(item => item.totalPredictedQuantity || 0),
        itemStyle: {
          color: '#2196f3'
        }
      },
      {
        name: 'Tồn kho hiện tại',
        type: 'bar',
        data: filteredData.map(item => item.currentStock || 0),
        itemStyle: {
          color: '#ff9800'
        }
      }
    ]
  };

  // Biểu đồ phân tích tình trạng tồn kho theo tuần
  const stockAnalysisData = [
    {
      value: forecastData.filter(item => (item.weeksUntilStockout || 0) <= 2 && (item.weeksUntilStockout || 0) > 0).length,
      name: 'Cần nhập gấp (≤2 tuần)',
      itemStyle: { color: '#f44336' }
    },
    {
      value: forecastData.filter(item => 
        (item.weeksUntilStockout || 0) > 2 && (item.weeksUntilStockout || 0) <= 4
      ).length,
      name: 'Cần theo dõi (3-4 tuần)',
      itemStyle: { color: '#ff9800' }
    },
    {
      value: forecastData.filter(item => (item.weeksUntilStockout || 0) > 4).length,
      name: 'Đủ tồn kho (>4 tuần)',
      itemStyle: { color: '#4caf50' }
    },
    {
      value: forecastData.filter(item => !(item.weeksUntilStockout > 0)).length,
      name: 'Không xác định',
      itemStyle: { color: '#9e9e9e' }
    }
  ];

  const stockTrendOption = {
    title: {
      text: 'Phân tích tình trạng tồn kho theo tuần',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} sản phẩm ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [
      {
        name: 'Tình trạng tồn kho',
        type: 'pie',
        radius: '50%',
        center: ['50%', '50%'],
        data: stockAnalysisData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            <DateRange sx={{ mr: 1, verticalAlign: 'middle' }} />
            Dự báo hàng tồn kho theo tuần
          </Typography>
          <Typography variant="body1" color="textSecondary">
            Phân tích và dự báo nhu cầu xuất kho theo tuần dựa trên mô hình ARIMA
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <Refresh />}
          onClick={loadForecastData}
          disabled={loading}
        >
          {loading ? 'Đang cập nhật...' : 'Cập nhật dự báo'}
        </Button>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={3} mb={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center">
                <Inventory color="primary" sx={{ mr: 2, fontSize: 40 }} />
                <Box>
                  <Typography variant="h5">{totalProducts}</Typography>
                  <Typography variant="body2" color="textSecondary">
                    Sản phẩm theo dõi
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center">
                <TrendingUp color="secondary" sx={{ mr: 2, fontSize: 40 }} />
                <Box>
                  <Typography variant="h5">{totalPredictedDemand}</Typography>
                  <Typography variant="body2" color="textSecondary">
                    Dự báo 4 tuần tới
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center">
                <Warning color="warning" sx={{ mr: 2, fontSize: 40 }} />
                <Box>
                  <Typography variant="h5">{criticalStockItems}</Typography>
                  <Typography variant="body2" color="textSecondary">
                    Cần nhập trong 4 tuần
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center">
                <Assessment color="info" sx={{ mr: 2, fontSize: 40 }} />
                <Box>
                  <Typography variant="h5">{avgConfidence}%</Typography>
                  <Typography variant="body2" color="textSecondary">
                    Độ tin cậy TB
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Biểu đồ tổng quan */}
      {!loading && filteredData.length > 0 && (
        <Grid container spacing={3} mb={3}>
          <Grid item xs={12} md={8}>
            <Card>
              <CardContent>
                <ReactECharts
                  option={overviewChartOption}
                  style={{ height: '400px', width: '100%' }}
                  opts={{ renderer: 'svg' }}
                />
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <ReactECharts
                  option={stockTrendOption}
                  style={{ height: '400px', width: '100%' }}
                  opts={{ renderer: 'svg' }}
                />
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Search and Filter */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <TextField
            fullWidth
            variant="outlined"
            placeholder="Tìm kiếm sản phẩm theo tên hoặc mã..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <Search />
                </InputAdornment>
              ),
            }}
          />
        </CardContent>
      </Card>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
        <Tabs value={activeTab} onChange={handleTabChange}>
          <Tab label="Tất cả sản phẩm" />
          <Tab 
            label={
              <Box display="flex" alignItems="center">
                Cần nhập trong 4 tuần
                {criticalStockItems > 0 && (
                  <Chip 
                    label={criticalStockItems} 
                    size="small" 
                    color="warning" 
                    sx={{ ml: 1 }}
                  />
                )}
              </Box>
            }
          />
        </Tabs>
      </Box>

      {/* Product Forecast Charts */}
      <TabPanel value={activeTab} index={0}>
        {loading ? (
          <Box display="flex" justifyContent="center" my={4}>
            <CircularProgress />
          </Box>
        ) : (
          <Grid container spacing={3}>
            {filteredData.map((product) => (
              <Grid item xs={12} key={product.productId}>
                <ProductForecastChart forecastData={product} />
              </Grid>
            ))}
          </Grid>
        )}
      </TabPanel>

      <TabPanel value={activeTab} index={1}>
        {loading ? (
          <Box display="flex" justifyContent="center" my={4}>
            <CircularProgress />
          </Box>
        ) : (
          <Grid container spacing={3}>
            {filteredData
              .filter(item => (item.weeksUntilStockout || 0) <= 4 && (item.weeksUntilStockout || 0) > 0)
              .map((product) => (
                <Grid item xs={12} key={product.productId}>
                  <ProductForecastChart forecastData={product} />
                </Grid>
              ))}
          </Grid>
        )}
      </TabPanel>

      {!loading && filteredData.length === 0 && (
        <Box textAlign="center" py={4}>
          <Typography variant="h6" color="textSecondary">
            Không tìm thấy dữ liệu dự báo theo tuần
          </Typography>
          <Typography variant="body2" color="textSecondary" mt={1}>
            Hãy thử làm mới dữ liệu hoặc kiểm tra kết nối
          </Typography>
        </Box>
      )}
    </Box>
  );
};

export default ProductForecastDashboard;