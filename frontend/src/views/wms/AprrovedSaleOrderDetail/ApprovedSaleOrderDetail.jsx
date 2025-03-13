import React, { useState } from "react";
import { Box, Grid, CircularProgress, Typography } from "@mui/material";
import { ApprovedOrderDetailProvider, useApprovedOrderDetail   } from "./context/OrderDetailContext";
import CustomTabs from "../common/components/CustomTabs";
import OutBoundList from "./components/OutBoundList";

const approveOrderLabels = [
  "Tổng quan",
  "Thông tin chung",
  "Thanh toán",
  "Đơn hàng",
  "Phiếu xuất"
];
const SaleOrderDetailContent = () => {
  const [tabValue, setTabValue] = useState(0);
  const { loading, orderData } = useApprovedOrderDetail();

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
      <Typography variant="h6">Chi tiết đơn hàng</Typography>

      <CustomTabs value={tabValue} onChange={handleTabChange} labels={approveOrderLabels} />

      {
        tabValue === 4 && (
            // this is the part showing created oubound order
          <OutBoundList />
        )
      }
      
    
    </Box>
  );
};

const ApprovedSaleOrderDetail = () => {
  return (
    <ApprovedOrderDetailProvider>
      <SaleOrderDetailContent />
    </ApprovedOrderDetailProvider>
  );
};

export default ApprovedSaleOrderDetail;