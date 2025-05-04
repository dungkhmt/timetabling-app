import React, { useState, useEffect } from "react";
import {
  Box,
  Button,
  Typography,
  Grid,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Chip,
  Divider,
  IconButton,
  Alert,
  AlertTitle,
  CircularProgress,
} from "@mui/material";
import {
  Person,
  LocalShipping,
  Refresh,
  LocationOn,
} from "@mui/icons-material";
import { useDeliveryPlanForm } from "../../context/DeliveryPlanFormContext";
import { useWms2Data } from "services/useWms2Data";

// A simple component to display a delivery bill in a list
const DeliveryBillItem = ({ bill }) => {
  return (
    <ListItem alignItems="flex-start">
      <ListItemAvatar>
        <Avatar>
          <LocalShipping />
        </Avatar>
      </ListItemAvatar>
      <ListItemText
        primary={bill.deliveryBillName || bill.id}
        secondary={
          <>
            <Typography component="span" variant="body2" color="text.primary">
              {bill.customerName || "Không có thông tin khách hàng"}
            </Typography>
            <br />
            {bill.deliveryAddress}
            <br />
            <Box mt={0.5}>
              <Chip 
                label={`${bill.totalWeight ? bill.totalWeight.toFixed(2) : "0"} kg`} 
                size="small" 
                color="primary" 
                variant="outlined"
              />
            </Box>
          </>
        }
      />
    </ListItem>
  );
};

const RoutesTab = () => {
  const { deliveryPlan, entities } = useDeliveryPlanForm();
  const { optimizeRoutes } = useWms2Data();
  const [loading, setLoading] = useState(false);
  const [optimizedRoutes, setOptimizedRoutes] = useState([]);
  
  // Check if we have the necessary data for optimization
  const canOptimize = 
    deliveryPlan.deliveryBillIds.length > 0 && 
    deliveryPlan.shipperIds.length > 0 && 
    deliveryPlan.facilityId;
  
  const handleOptimizeRoutes = async () => {
    if (!canOptimize) return;
    
    setLoading(true);
    
    try {
      // This is a placeholder for the actual API call
      // You would replace this with your actual route optimization API
      const response = await optimizeRoutes({
        facilityId: deliveryPlan.facilityId,
        deliveryBillIds: deliveryPlan.deliveryBillIds,
        shipperIds: deliveryPlan.shipperIds
      });
      
      if (response && response.code === 200) {
        setOptimizedRoutes(response.data || []);
      }
    } catch (error) {
      console.error("Error optimizing routes:", error);
    } finally {
      setLoading(false);
    }
  };
  
  // Create simple assignment if no optimization is available
  const getSimpleAssignments = () => {
    const assignments = [];
    
    if (entities.selectedShippers.length === 0 || entities.selectedDeliveryBills.length === 0) {
      return [];
    }
    
    // Simple round-robin assignment
    let shipperIndex = 0;
    
    entities.selectedDeliveryBills.forEach((bill) => {
      const shipper = entities.selectedShippers[shipperIndex];
      
      // Find or create an assignment for this shipper
      let assignment = assignments.find(a => a.shipperId === shipper.userLoginId);
      
      if (!assignment) {
        assignment = {
          shipperId: shipper.userLoginId,
          shipperName: shipper.fullName || shipper.username || shipper.userLoginId,
          deliveryBills: []
        };
        assignments.push(assignment);
      }
      
      assignment.deliveryBills.push(bill);
      
      // Move to next shipper using modulo to wrap around
      shipperIndex = (shipperIndex + 1) % entities.selectedShippers.length;
    });
    
    return assignments;
  };
  
  // Use optimized routes if available, otherwise use simple assignments
  const routes = optimizedRoutes.length > 0 ? optimizedRoutes : getSimpleAssignments();

  return (
    <Box>
      <Box mb={3} display="flex" justifyContent="space-between" alignItems="center">
        <Typography variant="h6">
          Phân công đơn hàng
        </Typography>
        
        <Button
          variant="contained"
          color="primary"
          startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <Refresh />}
          disabled={loading || !canOptimize}
          onClick={handleOptimizeRoutes}
        >
          Tối ưu hoá tự động
        </Button>
      </Box>
      
      {!canOptimize && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          <AlertTitle>Không thể phân công</AlertTitle>
          Vui lòng chọn ít nhất một phiếu giao hàng, một người giao hàng và một cơ sở xuất phát để tiếp tục.
        </Alert>
      )}
      
      {canOptimize && routes.length === 0 && !loading && (
        <Alert severity="info" sx={{ mb: 3 }}>
          <AlertTitle>Chưa có phân công</AlertTitle>
          Nhấn nút "Tối ưu hoá tự động" để tự động phân công đơn hàng cho người giao hàng.
        </Alert>
      )}
      
      {loading && (
        <Box display="flex" justifyContent="center" my={4}>
          <CircularProgress />
        </Box>
      )}
      
      {routes.length > 0 && (
        <Grid container spacing={2}>
          {routes.map((route, index) => (
            <Grid item xs={12} md={6} key={route.shipperId || index}>
              <Card variant="outlined">
                <CardContent>
                  <Box display="flex" alignItems="center" mb={2}>
                    <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
                      <Person />
                    </Avatar>
                    <Typography variant="subtitle1">
                      {route.shipperName || route.shipperId}
                    </Typography>
                    <Box flexGrow={1} />
                    <Chip 
                      label={`${route.deliveryBills.length} đơn hàng`} 
                      color="primary" 
                      size="small"
                    />
                  </Box>
                  
                  <Divider />
                  
                  <List dense sx={{ maxHeight: "300px", overflow: "auto" }}>
                    <ListItem>
                      <ListItemAvatar>
                        <Avatar sx={{ bgcolor: 'success.main' }}>
                          <LocationOn />
                        </Avatar>
                      </ListItemAvatar>
                      <ListItemText 
                        primary="Điểm xuất phát" 
                        secondary={entities.selectedFacility?.facilityName || entities.selectedFacility?.id}
                      />
                    </ListItem>
                    
                    {route.deliveryBills.map((bill, billIndex) => (
                      <React.Fragment key={bill.id}>
                        <Divider component="li" />
                        <DeliveryBillItem 
                          bill={bill} 
                          index={billIndex + 1}
                        />
                      </React.Fragment>
                    ))}
                  </List>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
};

export default RoutesTab;