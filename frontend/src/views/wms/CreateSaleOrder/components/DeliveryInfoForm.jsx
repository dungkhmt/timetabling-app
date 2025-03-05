import React from 'react';
import { 
  Box, 
  Grid, 
  TextField, 
  Select, 
  MenuItem, 
  FormControl, 
  Typography,
  FormControlLabel,
  Checkbox,
  TextareaAutosize
} from "@mui/material";
import { useOrderForm } from "../context/OrderFormContext";

const DeliveryInfoForm = () => {
  const { salesOrder, setSalesOrder } = useOrderForm();
  const [shippingMethods] = React.useState(["STANDARD", "EXPRESS", "ECONOMY"]);
  const [shippingCarriers] = React.useState(["VNPOST", "GHTK", "GHN", "VIETTEL"]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setSalesOrder(prev => ({ ...prev, [name]: value }));
  };

  const handleCheckboxChange = (e) => {
    const { name, checked } = e.target;
    setSalesOrder(prev => ({ ...prev, [name]: checked }));
  };

  return (
    <Box sx={{ mb: 3 }}>
      <Grid container spacing={2}>
        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Địa chỉ nhận:{" "}
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <TextField
            fullWidth
            size="small"
            name="deliveryAddress"
            value={salesOrder.deliveryAddress}
            onChange={handleInputChange}
          />
        </Grid>

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Số điện thoại nhận:{" "}
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <TextField
            fullWidth
            size="small"
            name="deliveryPhone"
            value={salesOrder.deliveryPhone}
            onChange={handleInputChange}
          />
        </Grid>

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Phương thức vận chuyển:
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <FormControl fullWidth size="small">
            <Select
              name="shippingMethod"
              value={salesOrder.shippingMethod}
              onChange={handleInputChange}
            >
              <MenuItem value="">Chọn phương thức</MenuItem>
              {shippingMethods.map((method) => (
                <MenuItem key={method} value={method}>
                  {method}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Đơn vị vận chuyển
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <FormControl fullWidth size="small">
            <Select
              name="shippingCarrier"
              value={salesOrder.shippingCarrier}
              onChange={handleInputChange}
            >
              <MenuItem value="">Chọn đơn vị</MenuItem>
              {shippingCarriers.map((carrier) => (
                <MenuItem key={carrier} value={carrier}>
                  {carrier}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Ghi chú:
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <TextareaAutosize
            minRows={3} 
            maxRows={6}
            name="notes"
            value={salesOrder.notes}
            onChange={handleInputChange}
            style={{ 
              width: "100%", 
              padding: "8.5px 14px",
              border: "1px solid rgba(0, 0, 0, 0.23)",
              borderRadius: "4px",
              fontFamily: "inherit",
              fontSize: "1rem",
              resize: "vertical"
            }}
            placeholder="Nhập ghi chú..." 
          />
        </Grid>

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>Xuất hóa đơn VAT</Typography>
        </Grid>
        <Grid item xs={8}>
          <FormControlLabel 
            control={
              <Checkbox 
                name="requireVatInvoice"
                checked={salesOrder.requireVatInvoice}
                onChange={handleCheckboxChange}
              />
            }
            label=""
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default DeliveryInfoForm;