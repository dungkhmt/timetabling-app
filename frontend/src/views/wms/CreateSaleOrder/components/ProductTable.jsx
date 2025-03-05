import React from 'react';
import {
  TableContainer,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Paper,
  TextField,
  IconButton
} from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import { useOrderForm } from "../context/OrderFormContext";

const ProductTable = () => {
  const { salesOrder, setSalesOrder, entities } = useOrderForm();

  const removeProductFromOrder = (productId) => {
    const updatedItems = salesOrder.orderItems.filter(item => item.productId !== productId);
    setSalesOrder(prev => ({ ...prev, orderItems: updatedItems }));
  };

  const updateProductQuantity = (productId, quantity) => {
    if (quantity <= 0) return;
    const updatedItems = salesOrder.orderItems.map(item => 
      item.productId === productId ? { ...item, quantity: Number(quantity) } : item
    );
    setSalesOrder(prev => ({ ...prev, orderItems: updatedItems }));
  };

  const getProductDetails = (productId) => {
    return entities.products.find(product => product.id === productId) || {};
  };

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>STT</TableCell>
            <TableCell>Mã sản phẩm</TableCell>
            <TableCell>Tên sản phẩm</TableCell>
            <TableCell>Đơn vị</TableCell>
            <TableCell>Giá nhập</TableCell>
            <TableCell>Giá bán lẻ</TableCell>
            <TableCell>Số lượng</TableCell>
            <TableCell>Thao tác</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {salesOrder.orderItems.length > 0 ? (
            salesOrder.orderItems.map((item, index) => {
              const product = getProductDetails(item.productId);
              return (
                <TableRow key={item.productId}>
                  <TableCell>{index + 1}</TableCell>
                  <TableCell>{product.id || item.productId}</TableCell>
                  <TableCell>{product.name || "—"}</TableCell>
                  <TableCell>{product.unit || "—"}</TableCell>
                  <TableCell>{product.costPrice?.toLocaleString() || "—"}</TableCell>
                  <TableCell>{product.retailPrice?.toLocaleString() || "—"}</TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      size="small"
                      value={item.quantity}
                      onChange={(e) => updateProductQuantity(item.productId, e.target.value)}
                      InputProps={{ inputProps: { min: 1 } }}
                    />
                  </TableCell>
                  <TableCell>
                    <IconButton 
                      color="error" 
                      onClick={() => removeProductFromOrder(item.productId)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              );
            })
          ) : (
            <TableRow>
              <TableCell colSpan={8} align="center">
                Chưa có dữ liệu
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default ProductTable;