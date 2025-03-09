import React from "react";
import { Box, Tabs, Tab, useMediaQuery, useTheme } from "@mui/material";

const OrderTabs = ({ value, onChange }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));

  return (
    <Box sx={{ borderBottom: 1, borderColor: "divider", mb: 3 }}>
      <Tabs
        value={value}
        onChange={onChange}
        aria-label="order details tabs"
        variant={isMobile ? "scrollable" : "standard"}
        scrollButtons={isMobile ? "auto" : false}
      >
        <Tab label="Tổng quan" />
        <Tab label="Thông tin chung" />
        <Tab label="Thanh toán" />
        <Tab label="Sản phẩm" />
      </Tabs>
    </Box>
  );
};

export default React.memo(OrderTabs);