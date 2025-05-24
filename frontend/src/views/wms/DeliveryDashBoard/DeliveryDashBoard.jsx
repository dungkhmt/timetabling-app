import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Typography, 
  Grid, 
  Paper, 
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Card,
  CardContent,
  Divider
} from '@mui/material';
import { toast } from 'react-toastify';
import { useWms2Data } from '../../../services/useWms2Data';
import DeliverySummaryCards from './components/DeliverySummaryCards';
import DailyDeliveryChart from './components/DailyDeliveryChart';
import StatusPieCharts from './components/StatusPieCharts';
import TopCustomersChart from './components/TopCustomersChart';
import ShipperPerformanceChart from './components/ShipperPerformanceChart';
import {MENU_CONSTANTS} from "../common/constants/screenId";
import {withAuthorization} from "../common/components/withAuthorization";

const DeliveryDashboard = () => {
  const { getDeliveryDashboard } = useWms2Data();
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState('thisMonth'); // 'thisMonth', 'lastMonth', 'lastThreeMonths'
  
  useEffect(() => {
    fetchDashboardData();
  }, [period]);
  
  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      // Calculate date range based on selected period
      const now = new Date();
      let startDate, endDate;
      
      switch (period) {
        case 'thisMonth':
          startDate = new Date(now.getFullYear(), now.getMonth(), 1);
          endDate = now;
          break;
        case 'lastMonth':
          startDate = new Date(now.getFullYear(), now.getMonth() - 1, 1);
          endDate = new Date(now.getFullYear(), now.getMonth(), 0);
          break;
        case 'lastThreeMonths':
          startDate = new Date(now.getFullYear(), now.getMonth() - 3, 1);
          endDate = now;
          break;
        default:
          startDate = new Date(now.getFullYear(), now.getMonth(), 1);
          endDate = now;
      }
      
      // Format dates as YYYY-MM-DD for API
      const formattedStartDate = startDate.toISOString().split('T')[0];
      const formattedEndDate = endDate.toISOString().split('T')[0];
      
      const response = await getDeliveryDashboard(formattedStartDate, formattedEndDate);
      
      if (response && response.code === 200) {
        setDashboardData(response.data);
      } else {
        toast.error("Không thể tải dữ liệu báo cáo giao hàng");
      }
    } catch (error) {
      console.error("Error fetching delivery dashboard:", error);
      toast.error("Lỗi khi tải dữ liệu báo cáo giao hàng");
    } finally {
      setLoading(false);
    }
  };
  
  const handlePeriodChange = (e) => {
    setPeriod(e.target.value);
  };
  
  // Generate title based on selected period
  const getDashboardTitle = () => {
    switch (period) {
      case 'thisMonth':
        return `Báo cáo giao hàng - Tháng ${new Date().getMonth() + 1}/${new Date().getFullYear()}`;
      case 'lastMonth':
        const lastMonth = new Date();
        lastMonth.setMonth(lastMonth.getMonth() - 1);
        return `Báo cáo giao hàng - Tháng ${lastMonth.getMonth() + 1}/${lastMonth.getFullYear()}`;
      case 'lastThreeMonths':
        return `Báo cáo giao hàng - 3 tháng gần đây`;
      default:
        return "Báo cáo giao hàng";
    }
  };
  
  return (
    <Box p={3}>
      <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h5" gutterBottom>
              {getDashboardTitle()}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              Thống kê hoạt động giao hàng
            </Typography>
          </Box>
          
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>Thời gian</InputLabel>
            <Select
              value={period}
              label="Thời gian"
              onChange={handlePeriodChange}
            >
              <MenuItem value="thisMonth">Tháng hiện tại</MenuItem>
              <MenuItem value="lastMonth">Tháng trước</MenuItem>
              <MenuItem value="lastThreeMonths">3 tháng gần đây</MenuItem>
            </Select>
          </FormControl>
        </Box>
        
        <Divider sx={{ my: 2 }} />
        
        {loading ? (
          <Box display="flex" justifyContent="center" alignItems="center" height="50vh">
            <CircularProgress />
          </Box>
        ) : !dashboardData ? (
          <Typography align="center" variant="h6" color="textSecondary" py={5}>
            Không có dữ liệu báo cáo
          </Typography>
        ) : (
          <>
            <DeliverySummaryCards
              totalBills={dashboardData.totalDeliveryBills}
              totalPlans={dashboardData.totalDeliveryPlans}
              totalRoutes={dashboardData.totalDeliveryRoutes}
              totalWeight={dashboardData.totalDeliveryWeight}
            />
            
            <Grid container spacing={3} mt={1}>
              <Grid item xs={12} md={8}>
                <Paper elevation={1} sx={{ p: 2 }}>
                  <DailyDeliveryChart data={dashboardData.dailyDeliveryCounts} />
                </Paper>
              </Grid>
              
              <Grid item xs={12} md={4}>
                <Paper elevation={1} sx={{ p: 2, height: '100%' }}>
                  <StatusPieCharts
                    billStatusCounts={dashboardData.billStatusCounts}
                    planStatusCounts={dashboardData.planStatusCounts}
                    routeStatusCounts={dashboardData.routeStatusCounts}
                  />
                </Paper>
              </Grid>
            </Grid>
            
            <Grid container spacing={3} mt={1}>
              <Grid item xs={12} md={6}>
                <Paper elevation={1} sx={{ p: 2 }}>
                  <TopCustomersChart customers={dashboardData.topCustomers} />
                </Paper>
              </Grid>
              
              <Grid item xs={12} md={6}>
                <Paper elevation={1} sx={{ p: 2 }}>
                  <ShipperPerformanceChart shippers={dashboardData.shipperPerformances} />
                </Paper>
              </Grid>
            </Grid>
          </>
        )}
      </Paper>
    </Box>
  );
};

export default withAuthorization(DeliveryDashboard, MENU_CONSTANTS.LOGISTICS_DASHBOARD);