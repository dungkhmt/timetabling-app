import React from "react";
import { Stack, Typography, Box, Button, useTheme, useMediaQuery, CircularProgress } from "@mui/material";
import { CheckOutlined, DeleteOutlined } from "@mui/icons-material";
import EditIcon from '@mui/icons-material/Edit';
import { useOrderDetail } from "../../common/context/OrderDetailContext";
import { PURCHASE_ORDER_STATUSES } from "../../common/constants/constants";

// Hàm tiện ích để kiểm tra trạng thái đơn hàng
const getOrderStatusInfo = (status) => {
  // Chuẩn hóa status
  const normalizedStatus = status?.toUpperCase?.() || "CREATED";
  
  // Tìm trong object statuses đã định nghĩa
  const statusInfo = PURCHASE_ORDER_STATUSES[normalizedStatus];
  
  // Trả về thông tin status
  return {
    display: statusInfo || "Không xác định",
    allowApprove: ["CREATED", "WAITING_FOR_APPROVAL"].includes(normalizedStatus),
    allowCancel: !["COMPLETED", "CANCELED", "DELIVERED"].includes(normalizedStatus),
    allowEdit: ["CREATED", "WAITING_FOR_APPROVAL"].includes(normalizedStatus),
  };
};

const OrderHeader = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const { orderData, loading, approveOrderApi, cancelOrder, editOrder } = useOrderDetail();

  if (!orderData) return null;

  // Lấy thông tin trạng thái
  const statusInfo = getOrderStatusInfo(orderData.status);
  
  // Sử dụng thông tin trạng thái để xác định quyền hành động
  const { allowApprove, allowCancel, allowEdit } = statusInfo;

  return (
    <Stack spacing={2} direction={isMobile ? "column" : "row"} justifyContent={isMobile ? "center" : "space-between"}>
      <Typography
        variant="h5"
        fontWeight="bold"
        sx={{ mb: 2, fontSize: isMobile ? "1.2rem" : "1.5rem" }}
      >
        Chi tiết đơn hàng mua: {orderData.id.substring(0, 8)}
      </Typography>

      <Box>
        <Stack
          direction={isMobile ? "column" : "row"}
          spacing={1}
          justifyContent="space-between"
          width="100%"
        >
          <Button
            variant="contained"
            color="primary"
            fullWidth={isMobile}
            startIcon={loading ? <CircularProgress size={20} /> : <CheckOutlined />}
            onClick={approveOrderApi}
            disabled={loading || !allowApprove}
          >
            Duyệt
          </Button>
          <Button
            variant="outlined"
            color="error"
            fullWidth={isMobile}
            startIcon={loading ? <CircularProgress size={20} /> : <DeleteOutlined />}
            onClick={cancelOrder}
            disabled={loading || !allowCancel}
          >
            Hủy bỏ
          </Button>
          <Button
            variant="outlined"
            color="info"
            fullWidth={isMobile}
            startIcon={<EditIcon />}
            onClick={editOrder}
            disabled={loading || !allowEdit}
          >
            Chỉnh sửa
          </Button>
        </Stack>
      </Box>
    </Stack>
  );
};

export default React.memo(OrderHeader);