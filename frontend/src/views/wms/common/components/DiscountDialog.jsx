import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  FormControl,
  Select,
  MenuItem,
  InputLabel,
  TextField,
  Box,
  Typography
} from '@mui/material';

const DiscountDialog = ({
  open,
  onClose,
  onSave,
  currentDiscount = 0,
  baseAmount = 0,
  title = "Cài đặt giảm giá",
  amountLabel = "Tổng tiền"
}) => {
  const [discountType, setDiscountType] = useState('percentage');
  const [discountValue, setDiscountValue] = useState(0);

  useEffect(() => {
    if (open) {
      // Reset values when dialog opens
      if (currentDiscount === 0) {
        setDiscountType('percentage');
        setDiscountValue(0);
      } else {
        // Determine if current discount is percentage or fixed amount
        const discountPercentage = baseAmount > 0 ? (currentDiscount / baseAmount) * 100 : 0;

        // If discount percentage is a round number (within 0.1%), treat as percentage
        if (Math.abs(discountPercentage - Math.round(discountPercentage * 10) / 10) < 0.01 && discountPercentage <= 100) {
          setDiscountType('percentage');
          setDiscountValue(Math.round(discountPercentage * 10) / 10);
        } else {
          setDiscountType('fixed');
          setDiscountValue(currentDiscount);
        }
      }
    }
  }, [open, currentDiscount, baseAmount]);

  const handleSave = () => {
    debugger;
    let finalDiscountAmount = discountValue;

    if (discountType === 'percentage') {
      finalDiscountAmount = baseAmount * discountValue / 100;
    }

    // Ensure discount doesn't exceed base amount
    finalDiscountAmount = Math.min(finalDiscountAmount, baseAmount);
    finalDiscountAmount = Math.max(finalDiscountAmount, 0);

    onSave(finalDiscountAmount);
    onClose();
  };

  const getDiscountPreview = () => {
    if (discountType === 'percentage') {
      return Math.min(baseAmount * discountValue / 100, baseAmount);
    }
    return Math.min(discountValue, baseAmount);
  };

  const getFinalAmount = () => {
    return baseAmount - getDiscountPreview();
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const handleDiscountValueChange = (e) => {
    const value = parseFloat(e.target.value) || 0;

    if (discountType === 'percentage') {
      setDiscountValue(Math.max(0, Math.min(100, value)));
    } else {
      setDiscountValue(Math.max(0, Math.min(baseAmount, value)));
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Box sx={{ mt: 2 }}>
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>Loại giảm giá</InputLabel>
            <Select
              value={discountType}
              onChange={(e) => setDiscountType(e.target.value)}
              label="Loại giảm giá"
            >
              <MenuItem value="percentage">Phần trăm (%)</MenuItem>
              <MenuItem value="fixed">Số tiền cố định (VNĐ)</MenuItem>
            </Select>
          </FormControl>

          <TextField
            fullWidth
            label={discountType === 'percentage' ? 'Phần trăm giảm giá' : 'Số tiền giảm giá'}
            type="number"
            value={discountValue}
            onChange={handleDiscountValueChange}
            inputProps={{
              min: 0,
              max: discountType === 'percentage' ? 100 : baseAmount,
              step: discountType === 'percentage' ? 0.1 : 1000
            }}
            sx={{ mb: 2 }}
          />

          <Box sx={{ p: 2, backgroundColor: 'grey.100', borderRadius: 1 }}>
            <Typography variant="body2">
              {amountLabel}: {formatCurrency(baseAmount)}
            </Typography>
            <Typography variant="body2">
              Số tiền giảm: {formatCurrency(getDiscountPreview())}
            </Typography>
            <Typography variant="body2" fontWeight="bold" color="primary">
              Thành tiền: {formatCurrency(getFinalAmount())}
            </Typography>
          </Box>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Hủy</Button>
        <Button onClick={handleSave} variant="contained" color="primary">
          Lưu
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DiscountDialog;