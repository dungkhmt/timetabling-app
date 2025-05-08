import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Tabs,
  Tab,
  Grid,
  Card,
  CardHeader,
  CardContent,
  Button,
  Divider,
  CircularProgress,
  Chip,
  Alert,
  Snackbar
} from '@mui/material';
import { useParams } from 'react-router-dom';
import { useWms2Data } from 'services/useWms2Data';
import { format } from 'date-fns';
import { vi } from 'date-fns/locale';
import GeneralInfoPanel from './components/GeneralInfoPanel';
import DeliveryBillsPanel from './components/DeliveryBillsPanel';
import ShippersPanel from './components/ShippersPanel';
import RoutesPanel from './components/RoutesPanel';
import AutoAssignRoutesPanel from './components/AutoAssignRoutesPanel';

// Tab panel component
function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

function a11yProps(index) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`,
  };
}

// Status chip component
const StatusChip = ({ status }) => {
  const getStatusConfig = (status) => {
    switch (status) {
      case 'CREATED':
        return { label: 'Đã tạo', color: 'primary' };
      case 'IN_PROGRESS':
        return { label: 'Đang xử lý', color: 'warning' };
      case 'ASSIGNED':
        return { label: 'Đã phân công', color: 'info' };
      case 'COMPLETED':
        return { label: 'Hoàn thành', color: 'success' };
      case 'CANCELLED':
        return { label: 'Đã hủy', color: 'error' };
      default:
        return { label: status, color: 'default' };
    }
  };

  const { label, color } = getStatusConfig(status);
  return <Chip label={label} color={color} size="small" />;
};

const DeliveryPlanDetail = () => {
  const { id } = useParams();
  const { getDeliveryPlanById, autoAssignDeliveryRoutes } = useWms2Data();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [deliveryPlan, setDeliveryPlan] = useState(null);
  const [activeTab, setActiveTab] = useState(0);
  const [optimizing, setOptimizing] = useState(false);
  const [optimizationResult, setOptimizationResult] = useState(null);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });

  // Fetch delivery plan details
  useEffect(() => {
    fetchDeliveryPlanDetails();
  }, [id]);

  const fetchDeliveryPlanDetails = async () => {
    setLoading(true);
    try {
      const response = await getDeliveryPlanById(id);
      if (response && response.code === 200) {
        setDeliveryPlan(response.data);
      } else {
        setError("Failed to fetch delivery plan details");
      }
    } catch (err) {
      console.error("Error fetching delivery plan:", err);
      setError("An error occurred while fetching the delivery plan");
    } finally {
      setLoading(false);
    }
  };

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleAutoAssignRoutes = async () => {
    setOptimizing(true);
    setOptimizationResult(null);
    
    try {
      const response = await autoAssignDeliveryRoutes(id);
      
      if (response && response.code === 200) {
        setOptimizationResult(response.data);
        setSnackbar({
          open: true,
          message: "Routes successfully optimized and assigned",
          severity: "success"
        });
        
        // Refresh delivery plan data to show new routes
        fetchDeliveryPlanDetails();
      } else {
        setSnackbar({
          open: true,
          message: response?.message || "Failed to optimize routes",
          severity: "error"
        });
      }
    } catch (error) {
      console.error("Error optimizing routes:", error);
      setSnackbar({
        open: true,
        message: "Error optimizing routes: " + (error.message || "Unknown error"),
        severity: "error"
      });
    } finally {
      setOptimizing(false);
    }
  };

  const handleCloseSnackbar = () => {
    setSnackbar(prev => ({ ...prev, open: false }));
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="300px">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box p={3}>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  if (!deliveryPlan) {
    return (
      <Box p={3}>
        <Alert severity="info">No delivery plan found</Alert>
      </Box>
    );
  }

  return (
    <Box p={3}>
      <Box mb={3} display="flex" justifyContent="space-between" alignItems="center">
        <Typography variant="h5" fontWeight="bold">
          Chi tiết kế hoạch giao hàng
        </Typography>
        <StatusChip status={deliveryPlan.statusId} />
      </Box>

      <Paper elevation={1} sx={{ mb: 3 }}>
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
          aria-label="delivery plan detail tabs"
        >
          <Tab label="Thông tin chung" {...a11yProps(0)} />
          <Tab label="Danh sách vận đơn" {...a11yProps(1)} />
          <Tab label="Danh sách shipper" {...a11yProps(2)} />
          <Tab label="Danh sách tuyến đường" {...a11yProps(3)} />
          <Tab label="Tối ưu tuyến đường" {...a11yProps(4)} />
        </Tabs>

        {/* General Info Tab */}
        <TabPanel value={activeTab} index={0}>
          <GeneralInfoPanel deliveryPlan={deliveryPlan} />
        </TabPanel>

        {/* Delivery Bills Tab */}
        <TabPanel value={activeTab} index={1}>
          <DeliveryBillsPanel deliveryBills={deliveryPlan.deliveryBills || []} />
        </TabPanel>

        {/* Shippers Tab */}
        <TabPanel value={activeTab} index={2}>
          <ShippersPanel shippers={deliveryPlan.shippers || []} />
        </TabPanel>

        {/* Routes Tab */}
        <TabPanel value={activeTab} index={3}>
          <RoutesPanel routes={deliveryPlan.existingRoutes || []} />
        </TabPanel>

        {/* Auto-assign Tab */}
        <TabPanel value={activeTab} index={4}>
          <AutoAssignRoutesPanel 
            id={id}
            onAutoAssign={handleAutoAssignRoutes}
            optimizing={optimizing}
            optimizationResult={optimizationResult}
            existingRoutes={deliveryPlan.existingRoutes || []}
          />
        </TabPanel>
      </Paper>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default DeliveryPlanDetail;