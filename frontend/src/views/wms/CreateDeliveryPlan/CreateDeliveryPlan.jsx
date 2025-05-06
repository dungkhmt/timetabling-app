import React, { useState, useEffect } from "react";
import {
  Box,
  Button,
  Typography,
  Paper,
  CircularProgress,
  Tabs,
  Tab,
  Divider,
  Alert,
} from "@mui/material";
import { toast } from "react-toastify";
import { useHistory } from "react-router-dom";
import { useWms2Data } from "services/useWms2Data";
import { DeliveryPlanFormProvider } from "./context/DeliveryPlanFormContext";

// Tab components
import GeneralInfoTab from "./components/tabs/GeneralInfoTab";
import DeliveryBillsTab from "./components/tabs/DeliveryBillsTab";
import ShippersTab from "./components/tabs/ShippersTab";
import RoutesTab from "./components/tabs/RoutesTab";

// Tab panel component
function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`delivery-plan-tabpanel-${index}`}
      aria-labelledby={`delivery-plan-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ pt: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

// Tab label props
function a11yProps(index) {
  return {
    id: `delivery-plan-tab-${index}`,
    'aria-controls': `delivery-plan-tabpanel-${index}`,
  };
}

const CreateDeliveryPlan = () => {
  const { createDeliveryPlan } = useWms2Data();
  const history = useHistory();
  const [submitting, setSubmitting] = useState(false);
  const [activeTab, setActiveTab] = useState(0);
  
  // State for form data
  const [deliveryPlan, setDeliveryPlan] = useState({
    id: null,
    deliveryPlanName: "",
    description: "",
    deliveryDate: new Date().toISOString().split('T')[0],
    deliveryBillIds: [],
    shipperIds: [],
    facilityId: ""
  });

  // State for lookup data
  const [entities, setEntities] = useState({
    deliveryBills: [],
    selectedDeliveryBills: [],
    shippers: [],
    selectedShippers: [],
    facilities: [],
    selectedFacility: null,
    routes: [] // For storing auto-assigned routes
  });

  // Handle tab change
  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  // Next tab navigation
  const handleNext = () => {
    setActiveTab(prev => Math.min(prev + 1, 3));
  };

  // Previous tab navigation
  const handleBack = () => {
    setActiveTab(prev => Math.max(prev - 1, 0));
  };

  // Validate current tab
  const validateCurrentTab = () => {
    switch (activeTab) {
      case 0: // General Info
        if (!deliveryPlan.deliveryPlanName) {
          toast.warning("Vui lòng nhập tên kế hoạch giao hàng");
          return false;
        }
        if (!deliveryPlan.deliveryDate) {
          toast.warning("Vui lòng chọn ngày giao hàng");
          return false;
        }
        if (!deliveryPlan.facilityId) {
          toast.warning("Vui lòng chọn cơ sở giao hàng");
          return false;
        }
        return true;
      
      case 1: // Delivery Bills
        if (deliveryPlan.deliveryBillIds.length === 0) {
          toast.warning("Vui lòng chọn ít nhất một phiếu giao hàng");
          return false;
        }
        return true;
      
      case 2: // Shippers
        if (deliveryPlan.shipperIds.length === 0) {
          toast.warning("Vui lòng chọn ít nhất một người giao hàng");
          return false;
        }
        return true;
      
      default:
        return true;
    }
  };

  // Handle submit
  const handleSubmit = () => {
    if (!validateCurrentTab()) {
      return;
    }

    setSubmitting(true);
    
    createDeliveryPlan(deliveryPlan)
      .then(response => {
        if (response && (response.code === 200 || response.code === 201)) {
          toast.success("Tạo kế hoạch giao hàng thành công");
          history.push("/wms/delivery/plans");
        } else {
          toast.error("Lỗi khi tạo kế hoạch giao hàng: " + (response?.message || "Lỗi không xác định"));
        }
      })
      .catch(error => {
        console.error("Error creating delivery plan:", error);
        toast.error("Lỗi khi tạo kế hoạch giao hàng: " + (error.message || "Lỗi không xác định"));
      })
      .finally(() => {
        setSubmitting(false);
      });
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
            {/*<Tab label="Danh sách chuyến" {...a11yProps(3)} />*/}
          </Tabs>
          
          <Divider />

          <Box p={3}>
            <TabPanel value={activeTab} index={0}>
              <GeneralInfoTab />
            </TabPanel>
            
            <TabPanel value={activeTab} index={1}>
              <DeliveryBillsTab />
            </TabPanel>
            
            <TabPanel value={activeTab} index={2}>
              <ShippersTab />
            </TabPanel>
            
            {/*<TabPanel value={activeTab} index={3}>*/}
            {/*  <RoutesTab />*/}
            {/*</TabPanel>*/}
            
            <Box mt={4} display="flex" justifyContent="space-between">
              <Button
                variant="outlined"
                onClick={handleBack}
                disabled={activeTab === 0}
              >
                Quay lại
              </Button>
              
              <Box>
                {activeTab === 3 ? (
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={handleSubmit}
                    disabled={submitting}
                    startIcon={submitting ? <CircularProgress size={20} color="inherit" /> : null}
                  >
                    {submitting ? "Đang xử lý..." : "Tạo kế hoạch giao hàng"}
                  </Button>
                ) : (
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => {
                      if (validateCurrentTab()) {
                        handleNext();
                      }
                    }}
                  >
                    Tiếp theo
                  </Button>
                )}
              </Box>
            </Box>
          </Box>
        </Paper>
      </Box>
    </DeliveryPlanFormProvider>
  );
};

export default CreateDeliveryPlan;