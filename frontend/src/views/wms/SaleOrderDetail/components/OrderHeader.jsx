import React from "react";
import { Stack, Typography, Box, Button, useTheme, useMediaQuery, CircularProgress } from "@mui/material";
import { CheckOutlined, DeleteOutlined } from "@mui/icons-material";
import EditIcon from '@mui/icons-material/Edit';
import PercentIcon from '@mui/icons-material/Percent';
import { useOrderDetail } from "../context/OrderDetailContext";

const OrderHeader = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const { orderData, loading, approveOrder, cancelOrder, editOrder, applyDiscount } = useOrderDetail();

  if (!orderData) return null;

  // Kiểm tra nếu đơn hàng có thể thực hiện các thao tác
  const canApprove = orderData.status === "Mới tạo" || orderData.status === "NEW";
  const canCancel = orderData.status !== "Đã hủy" && orderData.status !== "CANCELED";
  const canEdit = orderData.status === "Mới tạo" || orderData.status === "NEW";
  const canDiscount = orderData.status === "Mới tạo" || orderData.status === "NEW";

  return (
    <Stack spacing={2} direction={isMobile ? "column" : "row"} justifyContent={isMobile ? "center" : "space-between"}>
      <Typography
        variant="h5"
        fontWeight="bold"
        sx={{ mb: 2, fontSize: isMobile ? "1.2rem" : "1.5rem" }}
      >
        Chi tiết đơn hàng bán: {orderData.id}
      </Typography>

      <Box mt={3}>
        <Stack
          direction={isMobile ? "column" : "row"}
          spacing={1}
          justifyContent="space-between"
          width="100%"
        >
          <Button
            variant="outlined"
            color="primary"
            fullWidth={isMobile}
            startIcon={loading ? <CircularProgress size={20} /> : <CheckOutlined />}
            onClick={approveOrder}
            disabled={loading || !canApprove}
          >
            Duyệt
          </Button>
          <Button
            variant="outlined"
            color="error"
            fullWidth={isMobile}
            startIcon={loading ? <CircularProgress size={20} /> : <DeleteOutlined />}
            onClick={cancelOrder}
            disabled={loading || !canCancel}
          >
            Hủy bỏ
          </Button>
          <Button
            variant="outlined"
            color="info"
            fullWidth={isMobile}
            startIcon={<EditIcon />}
            onClick={editOrder}
            disabled={loading || !canEdit}
          >
            Chỉnh sửa
          </Button>
          <Button
            variant="outlined"
            color="secondary"
            fullWidth={isMobile}
            startIcon={<PercentIcon />}
            onClick={applyDiscount}
            disabled={loading || !canDiscount}
          >
            Chiết khấu
          </Button>
        </Stack>
      </Box>
    </Stack>
  );
};

export default React.memo(OrderHeader);