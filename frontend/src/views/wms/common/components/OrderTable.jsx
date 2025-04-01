import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TablePagination,
  Chip,
  Button,
  IconButton,
  Tooltip,
} from "@mui/material";
import { useHistory, useLocation } from "react-router-dom";
import { Info, Visibility } from "@mui/icons-material";
import { SALE_ORDER_STATUSES, PURCHASE_ORDER_STATUSES, ORDER_TYPE_ID } from "views/wms/common/constants/constants";

const getStatusColor = (status) => {
  switch (status) {
    case "CREATED":
      return "warning";
    case "APPROVED":
      return "success";
    case "REJECTED":
    case "CANCELED":
      return "error";
    case "SHIPPING":
      return "info";
    default:
      return "default";
  }
};

const OrderTable = ({
  orders,
  page,
  rowsPerPage,
  totalCount,
  onPageChange,
  onRowsPerPageChange,
  type
}) => {
  const history = useHistory();
  const location = useLocation();
  const baseUrl = type === ORDER_TYPE_ID.SALES_ORDER ? "/wms/sales" : "/wms/purchase";
  
  const handleViewDetails = (orderId) => {
    history.push(`${baseUrl}/orders/details/${orderId}`);
  };

  // Chọn statuses phù hợp cho từng loại order
  const ORDER_STATUSES = type === ORDER_TYPE_ID.SALES_ORDER  ? SALE_ORDER_STATUSES : PURCHASE_ORDER_STATUSES;

  return (
    <Paper sx={{ width: "100%", overflow: "hidden", mt: 2 }}>
      <TableContainer sx={{ maxHeight: 440 }}>
        <Table stickyHeader aria-label="sticky table">
          <TableHead>
            <TableRow>
              <TableCell>Mã đơn hàng</TableCell>
              <TableCell>Ngày tạo</TableCell>
              <TableCell>{type === ORDER_TYPE_ID.SALES_ORDER ? "Khách hàng" : "Nhà cung cấp"}</TableCell>
              <TableCell>Thành tiền</TableCell>
              <TableCell>Trạng thái</TableCell>
              <TableCell align="center">Thao tác</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {orders.map((order) => (
              <TableRow 
                hover 
                key={order.id}
                onClick={() => handleViewDetails(order.id)}
                sx={{ 
                  cursor: 'pointer',
                  '&:hover': {
                    backgroundColor: 'rgba(0, 0, 0, 0.04)',
                  },
                  '& > td': {
                    // Đảm bảo tất cả các cell đều không chọn được text khi hover
                    userSelect: 'none'
                  }
                }}
              >
                <TableCell>{order.id}</TableCell>
                <TableCell>
                  {new Date(order.createdStamp).toLocaleDateString("vi-VN", {
                    day: "2-digit",
                    month: "2-digit",
                    year: "numeric",
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </TableCell>
                <TableCell>{type === ORDER_TYPE_ID.SALES_ORDER ? order?.customerName : order?.supplierName}</TableCell>
                <TableCell>
                  {new Intl.NumberFormat("vi-VN", {
                    style: "currency",
                    currency: "VND",
                  }).format(order.totalAmount)}
                </TableCell>
                <TableCell>
                  <Chip
                    label={ORDER_STATUSES[order.status] || "Không xác định"}
                    color={getStatusColor(order.status)}
                    variant="outlined"
                    size="small"
                    onClick={(e) => e.stopPropagation()} // Ngăn việc bubble up sự kiện click
                  />
                </TableCell>
                <TableCell align="center">
                  <Tooltip title="Xem chi tiết">
                    <IconButton 
                      size="small" 
                      color="primary"
                      onClick={(e) => {
                        e.stopPropagation(); // Ngăn việc bubble up sự kiện click
                        handleViewDetails(order.id);
                      }}
                    >
                      <Visibility fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={[5, 10, 25]}
        component="div"
        count={totalCount}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        labelRowsPerPage="Số dòng mỗi trang:"
        labelDisplayedRows={({ from, to, count }) =>
          `${from}-${to} của ${count}`
        }
      />
    </Paper>
  );
};

export default OrderTable;