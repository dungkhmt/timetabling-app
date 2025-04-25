import React from "react";
import {
  Box,
  Typography,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Card,
  CardContent,
  Grid
} from "@mui/material";
import { useDeliveryBillForm } from "../../common/context/DeliveryBillFormContext";

const DeliveryBillInfoForm = () => {
  const { deliveryBill, setDeliveryBill } = useDeliveryBillForm();

  // Handle input change
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setDeliveryBill(prev => ({
      ...prev,
      [name]: value
    }));
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Thông tin phiếu giao hàng
        </Typography>

        <Grid container spacing={2}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Tên phiếu giao hàng"
              name="deliveryBillName"
              value={deliveryBill.deliveryBillName}
              onChange={handleInputChange}
              required
              size="small"
            />
          </Grid>
          
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Ngày giao hàng dự kiến"
              type="date"
              name="expectedDeliveryDate"
              value={deliveryBill.expectedDeliveryDate}
              onChange={handleInputChange}
              required
              size="small"
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          
          <Grid item xs={12}>
            <FormControl fullWidth size="small">
              <InputLabel>Độ ưu tiên</InputLabel>
              <Select
                name="priority"
                value={deliveryBill.priority}
                onChange={handleInputChange}
                label="Độ ưu tiên"
              >
                <MenuItem value={1}>Thấp</MenuItem>
                <MenuItem value={2}>Trung bình</MenuItem>
                <MenuItem value={3}>Cao</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Ghi chú"
              name="note"
              value={deliveryBill.note}
              onChange={handleInputChange}
              multiline
              rows={4}
              size="small"
            />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default DeliveryBillInfoForm;