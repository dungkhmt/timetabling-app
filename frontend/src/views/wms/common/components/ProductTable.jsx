import React, { useState } from 'react';
import {
  TableContainer,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Paper,
  TextField,
  IconButton,
  Box,
  Typography
} from '@mui/material';
import { Delete as DeleteIcon, Add, Remove, Edit } from '@mui/icons-material';
import { useOrderForm } from "../context/OrderFormContext";
import DiscountDialog from './DiscountDialog';
import {formatCurrency} from "../utils/functions";

const ProductTable = () => {
  const { 
    order, 
    entities,
    updateItemQuantity,
    updateItemDiscount,
    updateItemPrice,
    removeItemFromOrder
  } = useOrderForm();

  const [discountDialog, setDiscountDialog] = useState({
    open: false,
    productId: null,
    currentDiscount: 0,
    baseAmount: 0
  });

  const getProductDetails = (productId) => {
    return entities.products.find(product => product.id === productId) || {};
  };

  const calculateItemTotal = (item) => {
    const subtotal = item.price * item.quantity;
    const discountAmount = item.discount || 0;
    return subtotal - discountAmount;
  };

  const openDiscountDialog = (productId, currentDiscount, itemPrice, itemQuantity) => {
    const baseAmount = itemPrice * itemQuantity;
    setDiscountDialog({
      open: true,
      productId,
      currentDiscount: currentDiscount || 0,
      baseAmount
    });
  };

  const closeDiscountDialog = () => {
    setDiscountDialog({
      open: false,
      productId: null,
      currentDiscount: 0,
      baseAmount: 0
    });
  };

  const saveDiscount = (discountAmount) => {
    if (discountDialog.productId) {
      updateItemDiscount(discountDialog.productId, discountAmount);
    }
  };

  const getDiscountDisplay = (item) => {
    if (!item.discount || item.discount === 0) return '0';
    
    // Always display as specific amount, formatted as currency
    return formatCurrency(item.discount);
  };

  return (
    <>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>STT</TableCell>
              <TableCell>Mã sản phẩm</TableCell>
              <TableCell>Tên sản phẩm</TableCell>
              <TableCell>Đơn vị</TableCell>
              <TableCell>Đơn giá</TableCell>
              <TableCell>Số lượng</TableCell>
              <TableCell>Giảm giá</TableCell>
              <TableCell>Thành tiền</TableCell>
              <TableCell>Thao tác</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {order.orderItems.length > 0 ? (
              order.orderItems.map((item, index) => {
                const product = getProductDetails(item.productId);
                return (
                  <TableRow key={item.productId}>
                    <TableCell>{index + 1}</TableCell>
                    <TableCell>{product.code || product.id || item.productId}</TableCell>
                    <TableCell>{product.name || "—"}</TableCell>
                    <TableCell>{item.unit || "—"}</TableCell>
                    <TableCell>
                      <TextField
                        type="number"
                        size="small"
                        value={item.price}
                        onChange={(e) => updateItemPrice(item.productId, parseFloat(e.target.value) || 0)}
                        inputProps={{ min: 0, step: 1000 }}
                        sx={{ width: 120 }}
                      />
                    </TableCell>
                    <TableCell>
                      <Box display="flex" alignItems="center" gap={1}>
                        <IconButton 
                          size="small" 
                          onClick={() => updateItemQuantity(item.productId, -1)}
                          disabled={item.quantity <= 1}
                          color="primary"
                        >
                          <Remove fontSize="small" />
                        </IconButton>
                        <Typography variant="body2" sx={{ minWidth: 30, textAlign: 'center' }}>
                          {item.quantity}
                        </Typography>
                        <IconButton 
                          size="small" 
                          onClick={() => updateItemQuantity(item.productId, 1)}
                          color="primary"
                        >
                          <Add fontSize="small" />
                        </IconButton>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Box 
                        display="flex" 
                        alignItems="center" 
                        gap={1}
                        sx={{ cursor: 'pointer' }}
                        onClick={() => openDiscountDialog(item.productId, item.discount, item.price, item.quantity)}
                      >
                        <Typography variant="body2">
                          {getDiscountDisplay(item)}
                        </Typography>
                        <IconButton size="small">
                          <Edit fontSize="small" />
                        </IconButton>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium">
                        {formatCurrency(calculateItemTotal(item))}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <IconButton 
                        color="error" 
                        size="small"
                        onClick={() => removeItemFromOrder(item.productId)}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                );
              })
            ) : (
              <TableRow>
                <TableCell colSpan={9} align="center">
                  <Typography variant="body2" color="text.secondary" py={2}>
                    Chưa có sản phẩm nào được thêm vào đơn hàng
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
          {order.orderItems.length > 0 && (
            <TableBody>
              <TableRow>
                <TableCell colSpan={7} align="right">
                  <Typography variant="h6" fontWeight="bold">
                    Tổng cộng:
                  </Typography>
                </TableCell>
                <TableCell>
                  <Typography variant="h6" fontWeight="bold" color="primary">
                    {formatCurrency(
                      order.orderItems.reduce((total, item) => total + calculateItemTotal(item), 0)
                    )}
                  </Typography>
                </TableCell>
                <TableCell></TableCell>
              </TableRow>
            </TableBody>
          )}
        </Table>
      </TableContainer>

      <DiscountDialog
        open={discountDialog.open}
        onClose={closeDiscountDialog}
        onSave={saveDiscount}
        currentDiscount={discountDialog.currentDiscount}
        baseAmount={discountDialog.baseAmount}
        title="Cài đặt giảm giá sản phẩm"
        amountLabel="Tổng tiền sản phẩm"
      />
    </>
  );
};

export default ProductTable;