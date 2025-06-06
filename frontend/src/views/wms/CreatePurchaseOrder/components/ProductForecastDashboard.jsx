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
  Assessment
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
  const { getForecastData } = useWms2Data();

  const loadForecastData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getForecastData();
      if (response?.code === 200 && response?.data) {
        setForecastData(response.data);
      } else {
        throw new Error('Invalid response format');
      }
    } catch (err) {
      setError('Không thể tải dữ liệu dự báo: ' + (err.message || 'Lỗi không xác định'));
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

  // Calculate summary statistics
  const totalProducts = forecastData.length;
  const totalPredictedDemand = forecastData.reduce((sum, item) => sum + (item.totalPredictedQuantity || 0), 0);
  const criticalStockItems = forecastData.filter(item => 
    (item.currentStock || 0) < ((item.averageDailyOutbound || 0) * 7)
  ).length;

  // Biểu đồ tổng quan
  const overviewChartOption = {
    title: {
      text: 'Tổng quan dự báo nhu cầu',
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
          <div>Dự báo: ${params[0].value} ${item?.unit}</div>
          <div>Tồn kho: ${item?.currentStock || 0} ${item?.unit}</div>
          <div>TB/ngày: ${item?.averageDailyOutbound || 0} ${item?.unit}</div>
        `;
      }
    },
    legend: {
      data: ['Dự báo 7 ngày', 'Tồn kho hiện tại'],
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
        name: 'Dự báo 7 ngày',
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

  // Biểu đồ xu hướng tồn kho
  const stockTrendOption = {
    title: {
      text: 'Phân tích tình trạng tồn kho',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
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
        data: [
          {
            value: criticalStockItems,
            name: 'Cần nhập thêm',
            itemStyle: { color: '#f44336' }
          },
          {
            value: totalProducts - criticalStockItems,
            name: 'Đủ tồn kho',
            itemStyle: { color: '#4caf50' }
          }
        ],
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
            <Insights sx={{ mr: 1, verticalAlign: 'middle' }} />
            Dự báo hàng tồn kho
          </Typography>
          <Typography variant="body1" color="textSecondary">
            Phân tích và dự báo nhu cầu xuất kho dựa trên mô hình ARIMA
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
                    Dự báo 7 ngày tới
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
                    Sản phẩm cần nhập thêm
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
                  <Typography variant="h5">95%</Typography>
                  <Typography variant="body2" color="textSecondary">
                    Độ tin cậy mô hình
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
                Cần nhập thêm
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
              .filter(item => (item.currentStock || 0) < ((item.averageDailyOutbound || 0) * 7))
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
            Không tìm thấy dữ liệu dự báo
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