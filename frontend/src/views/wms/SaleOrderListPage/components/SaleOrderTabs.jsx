import React from "react";
import { Tabs, Tab } from "@mui/material";

const SaleOrderTabs = ({ value, onChange }) => {
  return (
    <Tabs value={value} onChange={onChange} sx={{ mt: 2 }}>
      <Tab label="Tất cả" />
      <Tab label="Chưa xử lý giao hàng" />
      <Tab label="Chờ lấy hàng" />
      <Tab label="Đang giao hàng" />
      <Tab label="Chưa thanh toán" />
    </Tabs>
  );
};

export default SaleOrderTabs;