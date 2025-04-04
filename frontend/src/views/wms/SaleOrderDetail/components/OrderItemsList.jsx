import React from "react";
import { Box, Typography, Stack, useTheme, useMediaQuery } from "@mui/material";
import { useOrderDetail } from "../../common/context/OrderDetailContext";
import OrderItemsTable from "./OrderItemsTable";
import OrderItemCard from "./OrderItemCard";
import OrderSummary from "./OrderSummary";

const OrderItemsList = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const { orderData } = useOrderDetail();

  if (!orderData) return null;

  return (
    <>
      <Typography variant="h6" sx={{ mb: 2 }}>
        Đơn hàng
      </Typography>
      
      {isMobile ? (
        <Stack spacing={2}>
          {orderData.orderItems.map((item, index) => (
            <OrderItemCard key={index} item={item} />
          ))}
        </Stack>
      ) : (
        <OrderItemsTable items={orderData.orderItems} />
      )}
      
      <OrderSummary />
    </>
  );
};

export default OrderItemsList;