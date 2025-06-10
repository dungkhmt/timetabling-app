import React, { useState } from "react";
import {
  Box,
  Grid,
  TextField,
  Select,
  MenuItem,
  FormControl,
  Typography,
  IconButton
} from "@mui/material";
import { Edit } from "@mui/icons-material";

import { useOrderForm } from "../../common/context/OrderFormContext";
import CustomerField from "../../common/components/CustomerField";
import RequireField from "views/wms/common/components/RequireField";
import { SALE_CHANNELS } from "views/wms/common/constants/constants";
import DiscountDialog from "../../common/components/DiscountDialog";

const BasicInfoForm = () => {
  const { order, setOrder, calculateItemsTotal } = useOrderForm();
  const [discountDialogOpen, setDiscountDialogOpen] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setOrder((prev) => ({ ...prev, [name]: value }));
  };

  const getDiscountDisplay = () => {
    if (!order.discount || order.discount === 0) return '0';
    
    const itemsTotal = calculateItemsTotal();
    const discountPercentage = itemsTotal > 0 ? (order.discount / itemsTotal) * 100 : 0;
    
    // If it's close to a round percentage, show percentage
    if (Math.abs(discountPercentage - Math.round(discountPercentage * 10) / 10) < 0.01 && discountPercentage <= 100) {
      return `${Math.round(discountPercentage * 10) / 10}%`;
    }
    
    // Otherwise show fixed amount
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(order.discount);
  };

  const saveOrderDiscount = (discountAmount) => {
    setOrder(prev => ({ ...prev, discount: discountAmount }));
  };

  const getOrderBaseAmount = () => {
    // Return items total for order-level discount calculation
    return calculateItemsTotal();
  };

  return (
    <>
      <Box sx={{ mb: 3 }}>
        <Grid container spacing={2}>
          
          <Grid item xs={4}>
            <Typography variant="body1" sx={{ pt: 1 }}>
              Kênh bán hàng: <RequireField />
            </Typography>
          </Grid>
          <Grid item xs={8}>
            <Select
              name="saleChannelId"
              value={order.saleChannelId}
              onChange={handleInputChange}
              fullWidth
              size="small"
            >
              <MenuItem value="">Chọn kênh bán hàng</MenuItem>
              {Object.keys(SALE_CHANNELS).map((channel) => (
                <MenuItem key={channel} value={channel}>
                  {SALE_CHANNELS[channel]}
                </MenuItem>
              ))}
            </Select>
          </Grid>

          <CustomerField />

          <Grid item xs={4}>
            <Typography variant="body1" sx={{ pt: 1 }}>
              Tên đơn hàng:
            </Typography>
          </Grid>
          <Grid item xs={8}>
            <TextField
              fullWidth
              size="small"
              name="orderName"
              value={order.orderName}
              onChange={handleInputChange}
              placeholder="Nhập tên đơn hàng..."
            />
          </Grid>

          <Grid item xs={4}>
            <Typography variant="body1" sx={{ pt: 1 }}>
              Chiết khấu đơn hàng:
            </Typography>
          </Grid>
          <Grid item xs={8}>
            <Box 
              display="flex" 
              alignItems="center" 
              gap={1}
              sx={{ cursor: 'pointer' }}
              onClick={() => setDiscountDialogOpen(true)}
            >
              <TextField
                fullWidth
                size="small"
                value={getDiscountDisplay()}
                placeholder="Nhập chiết khấu..."
                InputProps={{
                  readOnly: true,
                  endAdornment: (
                    <IconButton size="small">
                      <Edit fontSize="small" />
                    </IconButton>
                  )
                }}
              />
            </Box>
          </Grid>

          <Grid item xs={4}>
            <Typography variant="body1" sx={{ pt: 1 }}>
              Ghi chú:
            </Typography>
          </Grid>
          <Grid item xs={8}>
            <TextField
              fullWidth
              size="small"
              name="note"
              value={order.note}
              onChange={handleInputChange}
              multiline
              rows={2}
              placeholder="Nhập ghi chú đơn hàng..."
            />
          </Grid>

        </Grid>
      </Box>

      <DiscountDialog
        open={discountDialogOpen}
        onClose={() => setDiscountDialogOpen(false)}
        onSave={saveOrderDiscount}
        currentDiscount={order.discount || 0}
        baseAmount={getOrderBaseAmount()}
        title="Cài đặt giảm giá đơn hàng"
        amountLabel="Tổng tiền đơn hàng"
      />
    </>
  );
};

export default BasicInfoForm;