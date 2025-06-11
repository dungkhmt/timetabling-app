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
  Typography,
  Chip
} from '@mui/material';
import { Delete as DeleteIcon, Add, Remove, Edit } from '@mui/icons-material';
import { useOrderForm } from "../context/OrderFormContext";
import DiscountDialog from './DiscountDialog';
import { formatCurrency } from "../utils/functions";
import { ORDER_TYPE_ID } from '../constants/constants';

const ProductTable = ({ orderTypeId = ORDER_TYPE_ID.SALES_ORDER }) => { // Add orderTypeId prop with default ORDER_TYPE_ID.SALES_ORDER
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

  const isPurchaseOrder = orderTypeId === ORDER_TYPE_ID.PURCHASE_ORDER;
  const isSaleOrder = orderTypeId === ORDER_TYPE_ID.SALES_ORDER;

  const getProductDetails = (productId) => {
    return entities.products.find(product => product.id === productId) || {};
  };

  const calculateItemTotal = (item) => {
    const subtotal = item.price * item.quantity;
    const discountAmount = item.discount || 0;
    const afterDiscount = subtotal - discountAmount;
    
    // Only calculate tax for purchase orders
    if (isPurchaseOrder) {
      const taxAmount = afterDiscount * (item.tax || 0) / 100;
      return afterDiscount + taxAmount;
    }
    
    return afterDiscount;
  };

  const calculateItemTax = (item) => {
    if (!isPurchaseOrder) return 0;
    const subtotal = item.price * item.quantity;
    const discountAmount = item.discount || 0;
    const afterDiscount = subtotal - discountAmount;
    return afterDiscount * (item.tax || 0) / 100;
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
    return formatCurrency(item.discount);
  };

  const getVATDisplay = (item) => {
    if (!isPurchaseOrder || !item.tax) return '0%';
    return `${item.tax}%`;
  };

  // Dynamic column configuration based on order type
  const getColumns = () => {
    const baseColumns = [
      { key: 'stt', label: 'STT', width: 60 },
      { key: 'code', label: 'Mã SP', width: 100 },
      { key: 'name', label: 'Tên sản phẩm', width: 200 },
      { key: 'unit', label: 'Đơn vị', width: 80 },
      { key: 'price', label: 'Đơn giá', width: 120 },
      { key: 'quantity', label: 'Số lượng', width: 120 },
      { key: 'discount', label: 'Giảm giá', width: 100 }
    ];

    if (isPurchaseOrder) {
      baseColumns.push({ key: 'tax', label: 'VAT', width: 80 });
    }

    baseColumns.push(
      { key: 'total', label: 'Thành tiền', width: 120 },
      { key: 'actions', label: 'Thao tác', width: 80 }
    );

    return baseColumns;
  };

  const columns = getColumns();

  return (
    <>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell key={column.key} sx={{ width: column.width }}>
                  {column.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {order.orderItems.length > 0 ? (
              order.orderItems.map((item, index) => {
                const product = getProductDetails(item.productId);
                return (
                  <TableRow key={item.productId}>
                    {/* STT */}
                    <TableCell>{index + 1}</TableCell>
                    
                    {/* Mã sản phẩm */}
                    <TableCell>{product.code || product.id || item.productId}</TableCell>
                    
                    {/* Tên sản phẩm */}
                    <TableCell>
                      <Typography variant="body2">
                        {product.name || "—"}
                      </Typography>
                      {isPurchaseOrder && item.productName && (
                        <Typography variant="caption" color="textSecondary">
                          {item.productName}
                        </Typography>
                      )}
                    </TableCell>
                    
                    {/* Đơn vị */}
                    <TableCell>{item.unit || "—"}</TableCell>
                    
                    {/* Đơn giá */}
                    <TableCell>
                      <TextField
                        type="number"
                        size="small"
                        value={item.price}
                        onChange={(e) => updateItemPrice(item.productId, parseFloat(e.target.value) || 0)}
                        inputProps={{ min: 0, step: 1000 }}
                        sx={{ width: 100 }}
                      />
                    </TableCell>
                    
                    {/* Số lượng */}
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
                    
                    {/* Giảm giá */}
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
                    
                    {/* VAT - Only for purchase orders */}
                    {isPurchaseOrder && (
                      <TableCell>
                        <Box display="flex" flexDirection="column" alignItems="center">
                          <Typography variant="body2" fontWeight="medium">
                            {getVATDisplay(item)}
                          </Typography>
                          {item.tax > 0 && (
                            <Typography variant="caption" color="secondary">
                              +{formatCurrency(calculateItemTax(item))}
                            </Typography>
                          )}
                        </Box>
                      </TableCell>
                    )}
                    
                    {/* Thành tiền */}
                    <TableCell>
                      <Box display="flex" flexDirection="column">
                        <Typography variant="body2" fontWeight="medium">
                          {formatCurrency(calculateItemTotal(item))}
                        </Typography>
                        {isPurchaseOrder && item.tax > 0 && (
                          <Typography variant="caption" color="textSecondary">
                            (Có VAT)
                          </Typography>
                        )}
                      </Box>
                    </TableCell>
                    
                    {/* Thao tác */}
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
                <TableCell colSpan={columns.length} align="center">
                  <Typography variant="body2" color="text.secondary" py={2}>
                    Chưa có sản phẩm nào được thêm vào đơn hàng
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
          
          {/* Summary row */}
          {order.orderItems.length > 0 && (
            <TableBody>
              <TableRow sx={{ backgroundColor: 'grey.50' }}>
                <TableCell colSpan={columns.length - 2} align="right">
                  <Typography variant="h6" fontWeight="bold">
                    {isPurchaseOrder ? 'Tổng cộng (bao gồm VAT):' : 'Tổng cộng:'}
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
              
              {/* Additional tax summary for purchase orders */}
              {isPurchaseOrder && (
                <TableRow>
                  <TableCell colSpan={columns.length - 2} align="right">
                    <Typography variant="body2" color="textSecondary">
                      Trong đó VAT:
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="secondary" fontWeight="medium">
                      {formatCurrency(
                        order.orderItems.reduce((total, item) => total + calculateItemTax(item), 0)
                      )}
                    </Typography>
                  </TableCell>
                  <TableCell></TableCell>
                </TableRow>
              )}
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