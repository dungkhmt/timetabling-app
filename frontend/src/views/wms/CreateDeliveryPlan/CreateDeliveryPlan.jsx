import React, { useState } from 'react';
import { Box, Typography, Paper, Tabs, Tab, Button, CircularProgress } from '@mui/material';
import GeneralInfoTab from './components/tabs/GeneralInfoTab';
import DeliveryBillsTab from './components/tabs/DeliveryBillsTab';
import ShippersTab from './components/tabs/ShippersTab';
import VehiclesTab from './components/tabs/VehiclesTab'; // Import the new VehiclesTab
import { DeliveryPlanFormProvider } from './context/DeliveryPlanFormContext';
import { useHistory } from 'react-router-dom';
import { toast } from 'react-toastify';
import {useWms2Data} from "../../../services/useWms2Data";
function a11yProps(index) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`,
  };
}

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
      {value === index && <Box>{children}</Box>}
    </div>
  );
}

const CreateDeliveryPlan = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [deliveryPlan, setDeliveryPlan] = useState({
    deliveryPlanName: '',
    description: '',
    deliveryDate: null,
    facilityId: '',
    deliveryBillIds: [],
    shipperIds: [],
    vehicleIds: [], // Add vehicleIds array
  });
  const [entities, setEntities] = useState({
    facilities: [],
    deliveryBills: [],
    totalDeliveryBills: 0,
    selectedDeliveryBills: [],
    shippers: [],
    totalShippers: 0,
    selectedShippers: [],
    vehicles: [], // Add vehicles array
    totalVehicles: 0,
    selectedVehicles: [], // Add selectedVehicles array
  });

  const {createDeliveryPlan} = useWms2Data();

  const history = useHistory();

  const handleTabChange = (event, newValue) => {
    if (validateCurrentTab()) {
      setActiveTab(newValue);
    }
  };

  const validateCurrentTab = () => {
    switch (activeTab) {
      case 0: // General Info
        if (!deliveryPlan.deliveryPlanName) {
          toast.warning("Vui lòng nhập tên kế hoạch giao hàng");
          return false;
        }
        if (!deliveryPlan.facilityId) {
          toast.warning("Vui lòng chọn cơ sở");
          return false;
        }
        if (!deliveryPlan.deliveryDate) {
          toast.warning("Vui lòng chọn ngày giao hàng");
          return false;
        }
        return true;

      case 1: // Delivery Bills
        if (deliveryPlan.deliveryBillIds.length === 0) {
          toast.warning("Vui lòng chọn ít nhất một vận đơn");
          return false;
        }
        return true;

      case 2: // Shippers
        if (deliveryPlan.shipperIds.length === 0) {
          toast.warning("Vui lòng chọn ít nhất một người giao hàng");
          return false;
        }
        return true;
        
      case 3: // Vehicles
        if (deliveryPlan.vehicleIds.length === 0) {
          toast.warning("Vui lòng chọn ít nhất một phương tiện");
          return false;
        }
        if (deliveryPlan.vehicleIds.length < deliveryPlan.shipperIds.length) {
          toast.warning(`Cần chọn ít nhất ${deliveryPlan.shipperIds.length} phương tiện cho ${deliveryPlan.shipperIds.length} shipper`);
          return false;
        }
        
        // Check if total vehicle capacity is sufficient
        let totalCapacity = 0;
        entities.selectedVehicles.forEach(vehicle => {
          totalCapacity += parseFloat(vehicle.capacity || 0);
        });
        
        let totalWeight = 0;
        entities.selectedDeliveryBills.forEach(bill => {
          totalWeight += parseFloat(bill.totalWeight || 0);
        });
        
        if (totalCapacity < totalWeight) {
          toast.warning(`Tổng trọng tải phương tiện (${totalCapacity}) nhỏ hơn tổng khối lượng đơn hàng (${totalWeight})`);
          return false;
        }
        return true;
      
      default:
        return true;
    }
  };

  // Handle submit
  const handleSubmit = async () => {
    if (!validateCurrentTab()) {
      return;
    }

    setSubmitting(true);

    try {
      const response = await createDeliveryPlan(deliveryPlan);
      
      if (response && (response.code === 200 || response.code === 201)) {
        toast.success("Tạo kế hoạch giao hàng thành công");
        history.push("/wms/logistics/delivery");
      } else {
        toast.error("Lỗi khi tạo kế hoạch giao hàng: " + (response?.message || "Lỗi không xác định"));
      }
    } catch (error) {
      console.error("Error creating delivery plan:", error);
      toast.error("Lỗi khi tạo kế hoạch giao hàng: " + (error?.message || "Lỗi không xác định"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <DeliveryPlanFormProvider value={{ deliveryPlan, setDeliveryPlan, entities, setEntities }}>
      <Box p={3}>
        <Box mb={3} display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h5" fontWeight="bold">
            Tạo kế hoạch giao hàng
          </Typography>
        </Box>

        <Paper elevation={1} sx={{ mb: 3 }}>
          <Tabs 
            value={activeTab} 
            onChange={handleTabChange} 
            variant="fullWidth"
            indicatorColor="primary"
            textColor="primary"
            aria-label="delivery plan tabs"
          >
            <Tab label="Thông tin chung" {...a11yProps(0)} />
            <Tab label="Danh sách vận đơn" {...a11yProps(1)} />
            <Tab label="Danh sách shipper" {...a11yProps(2)} />
            <Tab label="Danh sách phương tiện" {...a11yProps(3)} />
          </Tabs>
        </Paper>

        <TabPanel value={activeTab} index={0}>
          <GeneralInfoTab />
        </TabPanel>
        <TabPanel value={activeTab} index={1}>
          <DeliveryBillsTab />
        </TabPanel>
        <TabPanel value={activeTab} index={2}>
          <ShippersTab />
        </TabPanel>
        <TabPanel value={activeTab} index={3}>
          <VehiclesTab />
        </TabPanel>

        <Box mt={3} display="flex" justifyContent="space-between">
          <Button 
            variant="outlined" 
            color="primary" 
            onClick={() => activeTab > 0 && setActiveTab(activeTab - 1)}
            disabled={activeTab === 0 || submitting}
          >
            Back
          </Button>
          <Box>
            {activeTab < 3 ? (
              <Button 
                variant="contained" 
                color="primary" 
                onClick={() => {
                  if (validateCurrentTab()) {
                    setActiveTab(activeTab + 1);
                  }
                }}
                disabled={submitting}
              >
                Next
              </Button>
            ) : (
              <Button 
                variant="contained" 
                color="success" 
                onClick={handleSubmit}
                disabled={submitting}
                startIcon={submitting ? <CircularProgress size={20} color="inherit" /> : null}
              >
                {submitting ? "Đang xử lý..." : "Hoàn tất"}
              </Button>
            )}
          </Box>
        </Box>
      </Box>
    </DeliveryPlanFormProvider>
  );
};

export default CreateDeliveryPlan;