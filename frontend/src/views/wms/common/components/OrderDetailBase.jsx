import React, { useState } from "react";
import { Box, Grid, CircularProgress, Typography } from "@mui/material";
import CustomTabs from "./CustomTabs";
import { ORDER_TYPE_ID } from "../constants/constants";
import { useOrderDetail } from "../context/OrderDetailContext";

const OrderDetailBase = ({
  tabLabels,
  BasicInfoComponent,
  DeliveryInfoComponent,
  ItemsListComponent,
  SummaryComponent,
  HeaderComponent,
  children,
}) => {
  const { loading, orderData, orderType } = useOrderDetail();
  const [tabValue, setTabValue] = useState(0);

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  // Hiển thị loading spinner khi đang tải dữ liệu
  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="50vh">
        <CircularProgress />
      </Box>
    );
  }

  // Hiển thị thông báo nếu không có dữ liệu
  if (!orderData) {
    const orderTypeName = orderType === ORDER_TYPE_ID.SALES_ORDER 
      ? "đơn hàng bán" 
      : orderType === ORDER_TYPE_ID.PURCHASE_ORDER
        ? "đơn hàng mua"
        : "đơn hàng";
        
    return (
      <Box p={3}>
        <Typography variant="h6">
          Không tìm thấy thông tin {orderTypeName}
        </Typography>
      </Box>
    );
  }

  return (
    <Box p={{ xs: 1, md: 3 }}>
      <HeaderComponent />
      
      <CustomTabs 
        value={tabValue} 
        onChange={handleTabChange} 
        labels={tabLabels} 
      />
      
      {tabValue === 0 && (
        <>
          <Grid container spacing={2} mt={1}>
            <Grid item xs={12} md={6}>
              <BasicInfoComponent />
            </Grid>
            <Grid item xs={12} md={6}>
              <DeliveryInfoComponent />
            </Grid>
          </Grid>
          
          <Box mt={3}>
            <ItemsListComponent />
          </Box>
        </>
      )}
      
      {tabValue === 1 && (
        <Box mt={2}>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <BasicInfoComponent />
            </Grid>
            <Grid item xs={12} md={6}>
              <DeliveryInfoComponent />
            </Grid>
          </Grid>
        </Box>
      )}
      
      {tabValue === 2 && (
        <Box mt={2}>
          <SummaryComponent showDetailedPayment={true} />
        </Box>
      )}
      
      {tabValue === 3 && (
        <Box mt={2}>
          <ItemsListComponent />
        </Box>
      )}

      {children && children({ tabValue, handleTabChange })}
    </Box>
  );
};

export default OrderDetailBase;