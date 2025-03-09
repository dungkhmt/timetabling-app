import React, { useState } from "react";
import { Box, Grid, CircularProgress, Typography } from "@mui/material";
import { OrderDetailProvider, useOrderDetail } from "./context/OrderDetailContext";
import OrderHeader from "./components/OrderHeader";
import OrderTabs from "./components/OrderTabs";
import OrderBasicInfo from "./components/OrderBasicInfo";
import OrderDeliveryInfo from "./components/OrderDeliveryInfo";
import OrderItemsList from "./components/OrderItemsList";
import OrderSummary from "./components/OrderSummary";

const SaleOrderDetailContent = () => {
  const [tabValue, setTabValue] = useState(0);
  const { loading, orderData } = useOrderDetail();

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
    return (
      <Box p={3}>
        <Typography variant="h6">Không tìm thấy thông tin đơn hàng</Typography>
      </Box>
    );
  }

  return (
    <Box p={{ xs: 1, md: 3 }}>
      <OrderHeader />
      
      <OrderTabs value={tabValue} onChange={handleTabChange} />
      
      {tabValue === 0 && (
        <>
          <Grid container spacing={2} mt={1}>
            <Grid item xs={12} md={6}>
              <OrderBasicInfo />
            </Grid>
            <Grid item xs={12} md={6}>
              <OrderDeliveryInfo />
            </Grid>
          </Grid>
          
          <Box mt={3}>
            <OrderItemsList />
          </Box>
        </>
      )}
      
      {tabValue === 1 && (
        <Box mt={2}>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <OrderBasicInfo />
            </Grid>
            <Grid item xs={12} md={6}>
              <OrderDeliveryInfo />
            </Grid>
          </Grid>
        </Box>
      )}
      
      {tabValue === 2 && (
        <Box mt={2}>
          <OrderSummary showDetailedPayment />
        </Box>
      )}
      
      {tabValue === 3 && (
        <Box mt={2}>
          <OrderItemsList />
        </Box>
      )}
    </Box>
  );
};

const SaleOrderDetail = () => {
  return (
    <OrderDetailProvider>
      <SaleOrderDetailContent />
    </OrderDetailProvider>
  );
};

export default SaleOrderDetail;