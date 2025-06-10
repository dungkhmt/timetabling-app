import React from 'react';
import {
  Box,
  Typography,
  Divider,
  Paper
} from '@mui/material';
import { useOrderForm } from 'views/wms/common/context/OrderFormContext';

const OrderSummary = () => {
  const { order, calculateItemsSubtotal, calculateItemsTotal, calculateOrderTotal } = useOrderForm();

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const totalQuantity = order.orderItems.reduce((total, item) => total + item.quantity, 0);
  const totalItemDiscount = calculateItemsSubtotal() - calculateItemsTotal();

  return (
    <Paper elevation={1} sx={{ p: 3, mt: 3 }}>
      <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', mb: 2 }}>
        📋 Tổng giá trị đơn hàng
      </Typography>

      <Box sx={{ mb: 2 }}>
        <Box display="flex" justifyContent="space-between" sx={{ mb: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Số lượng sản phẩm:
          </Typography>
          <Typography variant="body2" fontWeight="medium">
            {totalQuantity} sản phẩm
          </Typography>
        </Box>

        <Box display="flex" justifyContent="space-between" sx={{ mb: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Tổng tiền gốc:
          </Typography>
          <Typography variant="body2" fontWeight="medium">
            {formatCurrency(calculateItemsSubtotal())}
          </Typography>
        </Box>

        {totalItemDiscount > 0 && (
          <Box display="flex" justifyContent="space-between" sx={{ mb: 1 }}>
            <Typography variant="body2" color="warning.main">
              Giảm giá sản phẩm:
            </Typography>
            <Typography variant="body2" color="warning.main" fontWeight="medium">
              -{formatCurrency(totalItemDiscount)}
            </Typography>
          </Box>
        )}

        <Box display="flex" justifyContent="space-between" sx={{ mb: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Tổng sau giảm giá sản phẩm:
          </Typography>
          <Typography variant="body2" fontWeight="medium">
            {formatCurrency(calculateItemsTotal())}
          </Typography>
        </Box>

        {order.discount > 0 && (
          <Box display="flex" justifyContent="space-between" sx={{ mb: 1 }}>
            <Typography variant="body2" color="error.main">
              Giảm giá đơn hàng:
            </Typography>
            <Typography variant="body2" color="error.main" fontWeight="medium">
              -{formatCurrency(order.discount)}
            </Typography>
          </Box>
        )}
      </Box>

      <Divider sx={{ my: 2 }} />

      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Typography variant="h6" fontWeight="bold">
          💰 Thành tiền cuối:
        </Typography>
        <Typography variant="h5" color="primary" fontWeight="bold">
          {formatCurrency(calculateOrderTotal())}
        </Typography>
      </Box>

      {/* Additional Summary Info */}
      <Box sx={{ mt: 2, p: 1.5, backgroundColor: 'grey.50', borderRadius: 1 }}>
        <Typography variant="caption" color="text.secondary" display="block">
          📊 Thống kê:
        </Typography>
        <Box display="flex" justifyContent="space-between" sx={{ mt: 0.5 }}>
          <Typography variant="caption" color="text.secondary">
            Số loại sản phẩm:
          </Typography>
          <Typography variant="caption" fontWeight="medium">
            {order.orderItems.length} loại
          </Typography>
        </Box>
        
        {(totalItemDiscount > 0 || order.discount > 0) && (
          <Box display="flex" justifyContent="space-between">
            <Typography variant="caption" color="text.secondary">
              Tổng giảm giá:
            </Typography>
            <Typography variant="caption" color="success.main" fontWeight="medium">
              {formatCurrency(totalItemDiscount + (order.discount || 0))}
            </Typography>
          </Box>
        )}
      </Box>
    </Paper>
  );
};

export default OrderSummary;