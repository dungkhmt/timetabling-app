import React from "react";
import {
  Chip,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Tooltip,
} from "@mui/material";
import {useLocation} from "react-router-dom";
import {Visibility} from "@mui/icons-material";
import {ORDER_TYPE_ID, PURCHASE_ORDER_STATUSES, SALE_ORDER_STATUSES} from "views/wms/common/constants/constants";
import {useHandleNavigate} from "../utils/functions";

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
  const navigate = useHandleNavigate();
  const location = useLocation();

  const handleViewDetails = (orderId) => {
    navigate(``, url => {
      const pathName = location.pathname;
      if (pathName.includes('logistics')) {
        if(type === ORDER_TYPE_ID.SALES_ORDER) return url + `/wms/logistics/salesorders/details/reviewed/${orderId}`;
        return url + `/wms/logistics/purchaseorders/details/reviewed/${orderId}`;
      }
      if(type === ORDER_TYPE_ID.SALES_ORDER) {
        return url + `/wms/sales/orders/details/${orderId}`;
      }
        return url + `/wms/purchase/orders/details/${orderId}`;
    });
  };

  const ORDER_STATUSES = type === ORDER_TYPE_ID.SALES_ORDER  ? SALE_ORDER_STATUSES : PURCHASE_ORDER_STATUSES;

  return (
    <Paper sx={{ width: "100%", overflow: "hidden", mt: 2 }}>
      <TableContainer sx={{ maxHeight: 440 }}>
        <Table stickyHeader aria-label="sticky table">
          <TableHead>
            <TableRow>
              <TableCell>Mã đơn hàng</TableCell>
              <TableCell>Tên đơn hàng</TableCell>
              <TableCell>Ngày mua hàng</TableCell>
              <TableCell>Ngày giao dư kiến</TableCell>
              <TableCell>Ngày tạo</TableCell>
              <TableCell>{type === ORDER_TYPE_ID.SALES_ORDER ? "Khách hàng" : "Nhà cung cấp"}</TableCell>
              <TableCell>Tổng giá trị đơn</TableCell>
              <TableCell>Tổng số lượng</TableCell>
              <TableCell>Trạng thái</TableCell>
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
                    userSelect: 'none'
                  }
                }}
              >
                <TableCell>{order.id}</TableCell>
                <TableCell>{order.orderName}</TableCell>
                <TableCell>
                  {new Date(order.orderDate).toLocaleDateString("vi-VN", {
                    day: "2-digit",
                    month: "2-digit",
                    year: "numeric",
                  }) || "Chưa xác định"}
                </TableCell>
                <TableCell>
                  {order.deliveryAfterDate
                    ? new Date(order.deliveryAfterDate).toLocaleDateString("vi-VN", {
                        day: "2-digit",
                        month: "2-digit",
                        year: "numeric",
                      })
                    : "Chưa xác định"}
                </TableCell>
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
                <TableCell>{order.totalQuantity}</TableCell>
                <TableCell>
                  <Chip
                    label={ORDER_STATUSES[order.statusId] || "Không xác định"}
                    color={getStatusColor(order.statusId)}
                    variant="outlined"
                    size="small"
                    onClick={(e) => e.stopPropagation()} // Ngăn việc bubble up sự kiện click
                  />
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