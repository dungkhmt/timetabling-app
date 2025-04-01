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

import { useOrderForm } from "../../common/context/OrderFormContext";
import FacilityField from "../../common/components/FacilityField";
import CustomerField from "../../common/components/CustomerField";
import RequireField from "views/wms/common/components/RequireField";
import { SALE_CHANNELS } from "views/wms/common/constants/constants";
const BasicInfoForm = () => {
  const { order, setOrder } = useOrderForm();
  const [discountTypes] = React.useState(["PERCENT", "FIXED"]);
  const [orderPurposes] = React.useState([
    "SALES",
    "SAMPLE",
    "GIFT",
    "REPLACEMENT",
  ]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setOrder((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <Box sx={{ mb: 3 }}>
      <Grid container spacing={2}>
        
      <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Kênh bán hàng: <RequireField />
          </Typography>
        </Grid>
        <Grid item xs={8}>
        <Select
          name="saleChannel"
          value={order.saleChannel}
          onChange={handleInputChange}
          fullWidth
          size="small"
        >
          {Object.keys(SALE_CHANNELS).map((channel) => (
            <MenuItem key={channel} value={channel}>
              {SALE_CHANNELS[channel]}
            </MenuItem>
          ))}
        </Select>
        </Grid>
        <FacilityField />

        <CustomerField />

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Ngày giao dự kiến: <RequireField />
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
            Số hóa đơn (TC):
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
            Loại chiết khấu:
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <FormControl fullWidth size="small">
            <Select
              name="discountType"
              value={order.discountType}
              onChange={handleInputChange}
            >
              <MenuItem value="">Không có</MenuItem>
              {discountTypes.map((type) => (
                <MenuItem key={type} value={type}>
                  {type === "PERCENT" ? "Phần trăm" : "Số tiền cố định"}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>

        <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Giá trị chiết khấu:
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <TextField
            fullWidth
            size="small"
            name="discountValue"
            value={order.discountValue}
            onChange={handleInputChange}
            type="number"
            disabled={!order.discountType}
          />
        </Grid>

        {/* <Grid item xs={4}>
          <Typography variant="body1" sx={{ pt: 1 }}>
            Mục đích đơn hàng:
          </Typography>
        </Grid>
        <Grid item xs={8}>
          <FormControl fullWidth size="small">
            <Select
              name="orderPurpose"
              value={order.orderPurpose}
              onChange={handleInputChange}
            >
              {orderPurposes.map((purpose) => (
                <MenuItem key={purpose} value={purpose}>
                  {purpose === "SALES"
                    ? "Bán hàng"
                    : purpose === "SAMPLE"
                    ? "Mẫu"
                    : purpose === "GIFT"
                    ? "Quà tặng"
                    : "Thay thế"}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid> */}
      </Grid>
    </Box>
  );
};

export default BasicInfoForm;
