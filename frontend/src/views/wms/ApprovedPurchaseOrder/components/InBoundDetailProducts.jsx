import React, { memo } from "react";
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
} from "@mui/material";

// Format tiền tệ
const formatCurrency = (value) => {
  if (!value) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    minimumFractionDigits: 0,
  }).format(value);
};

// Component hiển thị sản phẩm trong bảng
const ProductTableRow = memo(({ product }) => {
  // Tính thành tiền
  const totalPrice = 
    product.wholeSalePrice && product.quantity
      ? product.wholeSalePrice * (product.quantity)
      : 0;

  // Xác định các số lượng yêu cầu và số lượng thực xuất có khớp nhau không
  const hasQuantityDiscrepancy = 
    product.requestedQuantity && 
    product.quantity !== product.requestedQuantity;

  return (
    <TableRow>
      <TableCell>{product.productId}</TableCell>
      <TableCell>{product.productName}</TableCell>
      <TableCell align="center">{product.unit || "Chiếc"}</TableCell>
      <TableCell align="center">
        {product.requestedQuantity || product.quantity}
      </TableCell>
      <TableCell align="center">
        {hasQuantityDiscrepancy ? (
          <Chip 
            label={product.quantity} 
            color="warning" 
            size="small" 
            variant="outlined"
          />
        ) : (
          product.quantity
        )}
      </TableCell>
      <TableCell align="right">{formatCurrency(product.wholeSalePrice)}</TableCell>
      <TableCell align="right">{formatCurrency(totalPrice)}</TableCell>
    </TableRow>
  );
});

const InBoundDetailProducts = ({ products }) => {
  if (!products || products.length === 0) {
    return (
      <Paper elevation={1} sx={{ p: 2, mt: 2, textAlign: "center" }}>
        <Typography color="text.secondary">
          Không có sản phẩm nào trong phiếu xuất
        </Typography>
      </Paper>
    );
  }

  // Tính tổng tiền
  const totalAmount = products.reduce((sum, product) => {
    const productTotal = product.wholeSalePrice ? 
      product.wholeSalePrice * product.quantity : 0;
    return sum + productTotal;
  }, 0);

  return (
    <Box mt={3}>
      <Box mb={2} display="flex" justifyContent="space-between" alignItems="center">
        <Typography variant="h6">
          Danh sách sản phẩm ({products.length})
        </Typography>
        <Typography variant="subtitle1">
          Tổng tiền: <strong>{formatCurrency(totalAmount)}</strong>
        </Typography>
      </Box>

      <TableContainer component={Paper} sx={{ borderRadius: 2 }}>
        <Table size="small">
          <TableHead sx={{ bgcolor: "action.hover" }}>
            <TableRow>
              <TableCell>Mã sản phẩm</TableCell>
              <TableCell>Tên sản phẩm</TableCell>
              <TableCell align="center">Đơn vị</TableCell>
              <TableCell align="center">SL yêu cầu</TableCell>
              <TableCell align="center">SL thực xuất</TableCell>
              <TableCell align="right">Đơn giá</TableCell>
              <TableCell align="right">Thành tiền</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {products.map((product) => (
              <ProductTableRow key={product.id || product.productId} product={product} />
            ))}
            <TableRow sx={{ bgcolor: "action.hover" }}>
              <TableCell colSpan={5} />
              <TableCell align="right">
                <Typography variant="subtitle2">Tổng tiền:</Typography>
              </TableCell>
              <TableCell align="right">
                <Typography variant="subtitle1" fontWeight="bold">
                  {formatCurrency(totalAmount)}
                </Typography>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default memo(InBoundDetailProducts);