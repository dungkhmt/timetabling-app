import React from "react";
import { Box, Typography } from "@mui/material";
import { useOrderDetail } from "../context/OrderDetailContext";

const OrderSummary = ({ showDetailedPayment = false }) => {
  const { orderData } = useOrderDetail();

  if (!orderData) return null;

  return (
    <Box mt={2}>
      <Typography align="right" fontWeight="bold">
        Tổng số lượng: {orderData.totalQuantity}
      </Typography>
      <Typography align="right" fontWeight="bold">
        Tổng đơn: {orderData.totalAmount}
      </Typography>
      
      {showDetailedPayment && (
        <>
          <Typography align="right" sx={{ mt: 2 }}>
            Thuế VAT (10%): {orderData.vatAmount || "646.800 ₫"}
          </Typography>
          <Typography align="right">
            Chiết khấu: {orderData.discountAmount || "0 ₫"}
          </Typography>
          <Typography align="right" fontWeight="bold" sx={{ mt: 1 }}>
            Thành tiền: {orderData.finalAmount || "7.114.800 ₫"}
          </Typography>
        </>
      )}
    </Box>
  );
};

export default React.memo(OrderSummary);