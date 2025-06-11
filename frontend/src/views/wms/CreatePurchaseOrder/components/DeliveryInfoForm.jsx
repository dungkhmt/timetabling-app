import React from 'react';
import { 
  Grid, 
  TextField, 
  Typography,
  Card,
  CardContent,
  Alert
} from "@mui/material";
import { useOrderForm } from 'views/wms/common/context/OrderFormContext';

const DeliveryInfoForm = () => {
  const { order, setOrder } = useOrderForm();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setOrder(prev => ({ ...prev, [name]: value }));
  };

  const isDateRangeValid = () => {
    if (!order.deliveryAfterDate || !order.deliveryBeforeDate) return true;
    return new Date(order.deliveryAfterDate) <= new Date(order.deliveryBeforeDate);
  };

  const today = new Date().toISOString().split('T')[0];

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Typography variant="h6" component="h3" gutterBottom sx={{ fontWeight: 'bold', mb: 3 }}>
          Thông tin giao hàng
        </Typography>
        
        <Grid container spacing={3}>
          {/* Order Date */}
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" fontWeight="medium" gutterBottom>
              Ngày đặt hàng
            </Typography>
            <TextField
              fullWidth
              type="date"
              name="orderDate"
              value={order.orderDate || ''}
              onChange={handleInputChange}
              size="small"
              InputLabelProps={{ shrink: true }}
              inputProps={{ min: today }}
              variant="outlined"
                error={order.orderDate && new Date(order.orderDate) < new Date(today)}
            />
          </Grid>

          {/* Delivery After Date */}
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" fontWeight="medium" gutterBottom>
              Giao sau ngày *
            </Typography>
            <TextField
              fullWidth
              type="date"
              name="deliveryAfterDate"
              value={order.deliveryAfterDate || ''}
              onChange={handleInputChange}
              size="small"
              InputLabelProps={{ shrink: true }}
              variant="outlined"
              inputProps={{ min: today }}
              error={order.deliveryAfterDate && new Date(order.deliveryAfterDate) < new Date(today)}
            />
          </Grid>

          {/* Delivery Before Date */}
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" fontWeight="medium" gutterBottom>
              Giao trước ngày
            </Typography>
            <TextField
              fullWidth
              type="date"
              name="deliveryBeforeDate"
              value={order.deliveryBeforeDate || ''}
              onChange={handleInputChange}
              size="small"
              InputLabelProps={{ shrink: true }}
              variant="outlined"
              inputProps={{ min: order.deliveryAfterDate || today }}
              error={!isDateRangeValid()}
            />
          </Grid>
        </Grid>

        {/* Date Range Validation */}
        {!isDateRangeValid() && (
          <Alert severity="error" sx={{ mt: 2 }}>
            Ngày giao sau phải nhỏ hơn hoặc bằng ngày giao trước!
          </Alert>
        )}
      </CardContent>
    </Card>
  );
};

export default DeliveryInfoForm;