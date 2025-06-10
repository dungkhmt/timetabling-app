import React from "react";
import {
  Paper,
  Typography,
  Box,
  Grid,
  Divider,
} from "@mui/material";
import { useOrderDetail } from "../context/OrderDetailContext";
import { ORDER_TYPE_ID } from "../constants/constants";
import {formatCurrency} from "../utils/functions";

const OrderSummary = ({ showDetailedPayment = false }) => {
  const { orderData } = useOrderDetail();

  if (!orderData) return null;

  // Xác định loại đơn hàng
  const orderTypeId = orderData.orderTypeId || ORDER_TYPE_ID.SALES_ORDER;

  // Tổng tiền hàng (chưa thuế, chiết khấu)
  const subtotal = orderData.orderItems?.reduce(
    (sum, item) => sum + item.unitPrice * item.quantity,
    0
  ) || 0;

  // Tiền thuế
  const tax = orderData.taxAmount || orderData.tax || 0;
  
  // Phí vận chuyển
  const shippingFee = orderData.deliveryCost || 0;
  
  // Chiết khấu tổng đơn hàng
  const orderDiscount = orderData.discountAmount || 0;
  
  // Chiết khấu từng sản phẩm
  const itemDiscounts = orderData.orderItems?.reduce(
    (sum, item) => sum + (item.discountAmount || 0),
    0
  ) || 0;
  
  // Tổng chiết khấu
  const totalDiscount = orderDiscount + itemDiscounts;
  
  // Tổng cộng (hoặc lấy từ orderData nếu có)
  const total = orderData.totalAmount || (subtotal + tax + shippingFee - totalDiscount);

  // Tiêu đề theo loại đơn hàng
  let title = "Thông tin thanh toán";
  if (orderTypeId === ORDER_TYPE_ID.PURCHASE_ORDER) {
    title = "Thông tin thanh toán đơn hàng mua";
  } else if (orderTypeId === ORDER_TYPE_ID.TRANSFER_ORDER) {
    title = "Thông tin vận chuyển nội bộ";
  } else if (orderTypeId === ORDER_TYPE_ID.RETURN_ORDER) {
    title = "Thông tin đơn trả hàng";
  }

  return (
    <Paper sx={{ width: "100%", overflow: "hidden", mt: 2 }}>
      <Box p={3}>
        <Typography variant="h6" fontWeight="bold" gutterBottom>
          {title}
        </Typography>
        
        {showDetailedPayment && (
          <>
            <Grid container spacing={2} mt={1}>
              <Grid item xs={8}>
                <Typography variant="body1">Tổng tiền hàng</Typography>
              </Grid>
              <Grid item xs={4} textAlign="right">
                <Typography variant="body1">{formatCurrency(subtotal)}</Typography>
              </Grid>
              
              {/* Hiển thị chiết khấu nếu có */}
              {totalDiscount > 0 && (
                <>
                  <Grid item xs={8}>
                    <Typography variant="body1">Chiết khấu</Typography>
                  </Grid>
                  <Grid item xs={4} textAlign="right">
                    <Typography variant="body1" color="error">
                      -{formatCurrency(totalDiscount)}
                    </Typography>
                  </Grid>
                </>
              )}
              
              {/* Hiển thị thuế nếu có */}
              {tax > 0 && (
                <>
                  <Grid item xs={8}>
                    <Typography variant="body1">Thuế</Typography>
                  </Grid>
                  <Grid item xs={4} textAlign="right">
                    <Typography variant="body1">{formatCurrency(tax)}</Typography>
                  </Grid>
                </>
              )}
              
              {/* Hiển thị phí vận chuyển nếu có */}
              {shippingFee > 0 && (
                <>
                  <Grid item xs={8}>
                    <Typography variant="body1">Phí vận chuyển</Typography>
                  </Grid>
                  <Grid item xs={4} textAlign="right">
                    <Typography variant="body1">{formatCurrency(shippingFee)}</Typography>
                  </Grid>
                </>
              )}
              
              <Grid item xs={12}>
                <Divider sx={{ my: 1 }} />
              </Grid>
            </Grid>
          </>
        )}
        
        <Grid container spacing={2}>
          <Grid item xs={8}>
            <Typography variant="h6" fontWeight="bold">
              Tổng cộng
            </Typography>
          </Grid>
          <Grid item xs={4} textAlign="right">
            <Typography variant="h6" fontWeight="bold" color="primary">
              {formatCurrency(total)}
            </Typography>
          </Grid>
          
          {/* Hiển thị các thông tin thanh toán khác nếu cần */}
          {orderData.paymentMethod && (
            <>
              <Grid item xs={8}>
                <Typography variant="body1">Phương thức thanh toán</Typography>
              </Grid>
              <Grid item xs={4} textAlign="right">
                <Typography variant="body1">{orderData.paymentMethod}</Typography>
              </Grid>
            </>
          )}
          
          {orderData.paymentStatus && (
            <>
              <Grid item xs={8}>
                <Typography variant="body1">Trạng thái thanh toán</Typography>
              </Grid>
              <Grid item xs={4} textAlign="right">
                <Typography 
                  variant="body1"
                  color={orderData.paymentStatus === "PAID" ? "success.main" : "warning.main"}
                >
                  {orderData.paymentStatus === "PAID" ? "Đã thanh toán" : "Chưa thanh toán"}
                </Typography>
              </Grid>
            </>
          )}
        </Grid>
      </Box>
    </Paper>
  );
};

export default React.memo(OrderSummary);