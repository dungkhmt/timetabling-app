import React, { useState } from "react";
import { Box, Grid, CircularProgress, Typography } from "@mui/material";
import {
  ApprovedOrderDetailProvider,
  useApprovedOrderDetail,
} from "./context/OrderDetailContext";
import CustomTabs from "../common/components/CustomTabs";
import OutBoundList from "./components/OutBoundList";
import OrderBasicInfo from "../SaleOrderDetail/components/OrderBasicInfo";
import OrderDeliveryInfo from "../SaleOrderDetail/components/OrderDeliveryInfo";
import OrderItemsList from "../SaleOrderDetail/components/OrderItemsList";
import { OrderDetailProvider, useOrderDetail } from "../SaleOrderDetail/context/OrderDetailContext";

const approveOrderLabels = [
  "Tổng quan",
  "Thanh toán",
  "Phiếu xuất",
];
const SaleOrderDetailContent = () => {
  const [tabValue, setTabValue] = useState(0);
  const { loading, orderData } = useOrderDetail();

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  // Hiển thị loading spinner khi đang tải dữ liệu
  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        height="50vh"
      >
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
      <Typography variant="h6">Chi tiết đơn hàng</Typography>

      <CustomTabs
        value={tabValue}
        onChange={handleTabChange}
        labels={approveOrderLabels}
      />

      {tabValue === 2 && (
        // this is the part showing created oubound order
        <OutBoundList />
      )}
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
    </Box>
  );
};

const ApprovedSaleOrderDetail = () => {
  return (
    <ApprovedOrderDetailProvider>
      <OrderDetailProvider>
        <SaleOrderDetailContent />
      </OrderDetailProvider>
    </ApprovedOrderDetailProvider>
  );
};

export default ApprovedSaleOrderDetail;
