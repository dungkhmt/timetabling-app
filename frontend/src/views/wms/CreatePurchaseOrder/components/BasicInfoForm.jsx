import React from "react";
import {
  Box,
  Grid,
  TextField,
  Select,
  MenuItem,
  FormControl,
  Typography,
} from "@mui/material";

import SupplierField from "../../common/components/SupplierField";
import { useOrderForm } from "views/wms/common/context/OrderFormContext";
import FacilityField from "views/wms/common/components/FacilityField";
const BasicInfoForm = () => {
  const { order, setOrder } = useOrderForm();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setOrder((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <Box sx={{ mb: 3 }}>
      <Grid container spacing={2}>

        <FacilityField />

        <SupplierField />

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Tên đơn hàng
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <TextField
              fullWidth
              size="small"
              name="orderName"
              value={order.orderName}
              onChange={handleInputChange}
          />
        </Grid>

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Chi phí vận chuyển:
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <TextField
            fullWidth
            size="small"
            name="deliveryCost"
            value={order.deliveryCost}
            onChange={handleInputChange}
          />
        </Grid>



      </Grid>
    </Box>
  );
};

export default BasicInfoForm;
