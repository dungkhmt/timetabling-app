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
import RequireField from "../../common/components/RequireField";
import { useOrderForm } from 'views/wms/common/context/OrderFormContext';

const DeliveryInfoForm = () => {
  const { order, setOrder } = useOrderForm();
  const [shippingMethods] = React.useState(["STANDARD", "EXPRESS", "ECONOMY"]);
  const [shippingCarriers] = React.useState(["VNPOST", "GHTK", "GHN", "VIETTEL"]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setOrder(prev => ({ ...prev, [name]: value }));
  };

  const handleCheckboxChange = (e) => {
    const { name, checked } = e.target;
    setOrder(prev => ({ ...prev, [name]: checked }));
  };

  return (
    <Box sx={{ mb: 3 }}>
      <Grid container spacing={2}>

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Số hóa đơn:{" "}
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <TextField
            fullWidth
            size="small"
            name="numberOfInvoices"
            value={order.numberOfInvoices}
            onChange={handleInputChange}
          />
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
            name="note"
            value={order.note}
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
          <Typography variant="body1" sx={{ pt: 1 }}>
            Giao sau ngày: <RequireField />
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <TextField
              fullWidth
              type="date"
              name="deliveryAfterDate"
              value={order.deliveryAfterDate}
              onChange={handleInputChange}
              size="small"
              InputLabelProps={{ shrink: true }}
          />
        </Grid>

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Giao trước ngày: <RequireField />
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <TextField
              fullWidth
              type="date"
              name="deliveryBeforeDate"
              value={order.deliveryBeforeDate}
              onChange={handleInputChange}
              size="small"
              InputLabelProps={{ shrink: true }}
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default DeliveryInfoForm;