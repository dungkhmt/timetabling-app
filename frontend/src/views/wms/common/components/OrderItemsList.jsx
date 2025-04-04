import React from "react";
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Box,
} from "@mui/material";
import { useOrderDetail } from "../context/OrderDetailContext";
import { ORDER_TYPE_ID } from "../constants/constants";

const OrderItemsList = () => {
  const { orderData } = useOrderDetail();

  if (!orderData || !orderData.orderItems || orderData.orderItems.length === 0) {
    return (
      <Paper sx={{ p: 2, mt: 2 }}>
        <Typography variant="body1">Không có sản phẩm nào trong đơn hàng</Typography>
      </Paper>
    );
  }

  // Xác định loại đơn hàng
  const orderTypeId = orderData.orderTypeId || ORDER_TYPE_ID.SALES_ORDER;
  
  // Chuẩn bị header và cột theo loại đơn hàng
  let tableColumns = [];
  
  switch (orderTypeId) {
    case ORDER_TYPE_ID.SALES_ORDER:
      tableColumns = [
        { id: 'productId', label: 'Mã SP' },
        { id: 'productName', label: 'Tên sản phẩm' },
        { id: 'unitPrice', label: 'Đơn giá' },
        { id: 'quantity', label: 'Số lượng' },
        { id: 'discountAmount', label: 'Chiết khấu' },
        { id: 'totalAmount', label: 'Thành tiền' },
      ];
      break;
      
    case ORDER_TYPE_ID.PURCHASE_ORDER:
      tableColumns = [
        { id: 'productId', label: 'Mã SP' },
        { id: 'productName', label: 'Tên sản phẩm' },
        { id: 'unitPrice', label: 'Đơn giá nhập' },
        { id: 'quantity', label: 'Số lượng' },
        { id: 'totalAmount', label: 'Thành tiền' },
      ];
      break;
      
    case ORDER_TYPE_ID.TRANSFER_ORDER:
    case ORDER_TYPE_ID.RETURN_ORDER:
    default:
      tableColumns = [
        { id: 'productId', label: 'Mã SP' },
        { id: 'productName', label: 'Tên sản phẩm' },
        { id: 'unitPrice', label: 'Đơn giá' },
        { id: 'quantity', label: 'Số lượng' },
        { id: 'totalAmount', label: 'Thành tiền' },
      ];
      break;
  }

  // Format số tiền
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount);
  };

  // Lấy giá trị theo cột
  const getCellValue = (item, columnId) => {
    switch (columnId) {
      case 'unitPrice':
      case 'discountAmount':
      case 'totalAmount':
        return formatCurrency(item[columnId] || 0);
      case 'quantity':
        return item[columnId] || 0;
      case 'productName':
        return item[columnId] || item.product?.name || 'Không có tên';
      case 'productId':
        return item[columnId] || item.product?.id || 'Không có mã';
      default:
        return item[columnId] || '-';
    }
  };

  return (
    <Paper sx={{ width: "100%", overflow: "hidden", mt: 2 }}>
      <Box p={2}>
        <Typography variant="h6" fontWeight="bold">
          Danh sách sản phẩm
        </Typography>
      </Box>
      <TableContainer sx={{ maxHeight: 440 }}>
        <Table stickyHeader aria-label="sticky table">
          <TableHead>
            <TableRow>
              <TableCell align="center">STT</TableCell>
              {tableColumns.map((column) => (
                <TableCell key={column.id}>
                  {column.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {orderData.orderItems.map((item, index) => (
              <TableRow hover key={index}>
                <TableCell align="center">{index + 1}</TableCell>
                {tableColumns.map((column) => (
                  <TableCell key={column.id}>
                    {getCellValue(item, column.id)}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
};

export default React.memo(OrderItemsList);