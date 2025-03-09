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
} from "@mui/material";
import { useHistory, useLocation } from "react-router-dom";

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

const getStatusLabel = (status) => {
  switch (status) {
    case "CREATED":
      return "Chưa xử lý giao hàng";
    case "APPROVED":
      return "Đã duyệt";
    case "REJECTED":
      return "Từ chối";
    case "CANCELED":
      return "Đã hủy";
    case "SHIPPING":
      return "Đang giao hàng";
    default:
      return status;
  }
};

const SaleOrderTable = ({
  orders,
  page,
  rowsPerPage,
  totalCount,
  onPageChange,
  onRowsPerPageChange,
}) => {
  const history = useHistory();
  const location = useLocation();
  const handleViewDetails = (orderId) => {
    history.push(location.pathname + "/details/" + orderId);
  };

  return (
    <Paper sx={{ width: "100%", overflow: "hidden", mt: 2 }}>
      <TableContainer sx={{ maxHeight: 440 }}>
        <Table stickyHeader aria-label="sticky table">
          <TableHead>
            <TableRow>
              <TableCell>Mã đơn hàng</TableCell>
              <TableCell>Ngày tạo</TableCell>
              <TableCell>Khách hàng</TableCell>
              <TableCell>Thành tiền</TableCell>
              <TableCell>Trạng thái</TableCell>
              <TableCell align="center">Thao tác</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {orders.map((order) => (
              <TableRow hover key={order.id}>
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
                <TableCell>{order.customerName}</TableCell>
                <TableCell>
                  {new Intl.NumberFormat("vi-VN", {
                    style: "currency",
                    currency: "VND",
                  }).format(order.totalAmount)}
                </TableCell>
                <TableCell>
                  <Chip
                    label={getStatusLabel(order.status)}
                    color={getStatusColor(order.status)}
                    variant="outlined"
                    size="small"
                  />
                </TableCell>
                <TableCell align="center">
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => handleViewDetails(order.id)}
                  >
                    Chi tiết
                  </Button>
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

export default SaleOrderTable;
