import React, { useState } from "react";
import {
  Box,
  Grid,
  TextField,
  Typography,
  Card,
  CardContent,
  IconButton
} from "@mui/material";
import { Edit as EditIcon } from "@mui/icons-material";
import SupplierField from "../../common/components/SupplierField";
import { useOrderForm } from "views/wms/common/context/OrderFormContext";
import DiscountDialog from "../../common/components/DiscountDialog";

const BasicInfoForm = () => {
  const { order, setOrder, calculateItemsTotal } = useOrderForm();
  const [discountDialogOpen, setDiscountDialogOpen] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setOrder((prev) => ({ ...prev, [name]: value }));
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const openDiscountDialog = () => {
    setDiscountDialogOpen(true);
  };

  const saveDiscount = (discountAmount) => {
    setOrder((prev) => ({ ...prev, discount: discountAmount }));
  };

  const baseAmount = calculateItemsTotal();

  return (
    <>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" component="h3" gutterBottom sx={{ fontWeight: 'bold', mb: 3 }}>
            Thông tin cơ bản
          </Typography>
          
          <Grid container spacing={3}>
            {/* Supplier Field */}
            <Grid item xs={12}>
              <SupplierField />
            </Grid>

            {/* Order Name */}
            <Grid item xs={12} md={6}>
              <Typography variant="subtitle2" fontWeight="medium" gutterBottom>
                Tên đơn hàng
              </Typography>
              <TextField
                fullWidth
                size="small"
                name="orderName"
                value={order.orderName || ''}
                onChange={handleInputChange}
                placeholder="Nhập tên đơn hàng..."
                variant="outlined"
              />
            </Grid>

            {/* Discount */}
            <Grid item xs={12} md={6}>
              <Typography variant="subtitle2" fontWeight="medium" gutterBottom>
                Giảm giá đơn hàng
              </Typography>
              <Box display="flex" alignItems="center" gap={1}>
                <TextField
                  fullWidth
                  size="small"
                  value={order.discount > 0 ? formatCurrency(order.discount) : 'Không có giảm giá'}
                  InputProps={{
                    readOnly: true,
                  }}
                  variant="outlined"
                />
                <IconButton 
                  size="small" 
                  onClick={openDiscountDialog}
                  disabled={baseAmount === 0}
                >
                  <EditIcon />
                </IconButton>
              </Box>
            </Grid>

            {/* Note */}
            <Grid item xs={12}>
              <Typography variant="subtitle2" fontWeight="medium" gutterBottom>
                Ghi chú
              </Typography>
              <TextField
                fullWidth
                multiline
                rows={3}
                name="note"
                value={order.note || ''}
                onChange={handleInputChange}
                placeholder="Nhập ghi chú cho đơn hàng..."
                variant="outlined"
              />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      <DiscountDialog
        open={discountDialogOpen}
        onClose={() => setDiscountDialogOpen(false)}
        onSave={saveDiscount}
        currentDiscount={order.discount || 0}
        baseAmount={baseAmount}
        title="Cài đặt giảm giá đơn hàng"
        amountLabel="Tổng tiền sản phẩm"
      />
    </>
  );
};

export default BasicInfoForm;