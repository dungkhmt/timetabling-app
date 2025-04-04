import React from "react";
import { Stack, Typography, Box, Button, useTheme, useMediaQuery, CircularProgress } from "@mui/material";
import { CheckOutlined, DeleteOutlined } from "@mui/icons-material";
import EditIcon from '@mui/icons-material/Edit';
import PercentIcon from '@mui/icons-material/Percent';
import { useOrderDetail } from "../../common/context/OrderDetailContext";
import { ORDER_STATUSES } from "views/wms/common/constants/constants";

// Đối tượng mapping các trạng thái


// Hàm tiện ích để kiểm tra trạng thái đơn hàng
const getOrderStatusInfo = (status) => {
  // Chuẩn hóa status
  const normalizedStatus = status?.toUpperCase?.() || "CREATED";
  
  // Tìm trong object statuses đã định nghĩa
  const statusInfo = ORDER_STATUSES[normalizedStatus];
  
  // Nếu tìm thấy, trả về thông tin status đó
  if (statusInfo) {
    return statusInfo;
  }
  
  // Nếu không tìm thấy, tìm kiếm theo tên hiển thị
  for (const [key, info] of Object.entries(ORDER_STATUSES)) {
    if (info.display === status) {
      return info;
    }
  }
  
  // Mặc định trả về trạng thái NEW nếu không tìm thấy
  return ORDER_STATUSES.CREATED;
};

const OrderHeader = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const { orderData, loading, approveOrderApi, cancelOrder, editOrder, applyDiscount } = useOrderDetail();

  if (!orderData) return null;

  // Lấy thông tin trạng thái
  const statusInfo = getOrderStatusInfo(orderData.status);
  
  // Sử dụng thông tin trạng thái để xác định quyền hành động
  const { allowApprove, allowCancel, allowEdit, allowDiscount } = statusInfo;

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
          <Button
            variant="outlined"
            color="secondary"
            fullWidth={isMobile}
            startIcon={<PercentIcon />}
            onClick={applyDiscount}
            disabled={loading || !allowDiscount}
          >
            Chiết khấu
          </Button>
        </Stack>
      </Box>
    </Stack>
  );
};

export default React.memo(OrderHeader);