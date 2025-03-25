import React from "react";
import { Grid, TextareaAutosize, TextField, Typography, Box, InputLabel } from "@mui/material";

const OutboundFormFields = ({ formData, errors, onChange }) => {
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    onChange(name, value);
  };

  return (
    <Grid container spacing={2} pt={2} mt={1}>
      <Grid item xs={6}>
        <Box height="100%">
          <InputLabel htmlFor="shipmentName" sx={{ mb: 1 }}>
            Tên phiếu xuất
          </InputLabel>
          <TextField
            id="shipmentName"
            name="shipmentName"
            multiline
            rows={1}
            fullWidth
            value={formData.shipmentName}
            onChange={handleInputChange}
            error={!!errors.shipmentName}
            helperText={errors.shipmentName}
            size="small"
          />
        </Box>
      </Grid>

      <Grid item xs={6}>
        <Box height="100%">
          <InputLabel htmlFor="expectedDeliveryDate" sx={{ mb: 1 }}>
            Ngày dự kiến giao hàng
          </InputLabel>
          <TextField
            id="expectedDeliveryDate"
            fullWidth
            type="date"
            name="expectedDeliveryDate"
            value={formData.expectedDeliveryDate}
            onChange={handleInputChange}
            size="small"
            InputLabelProps={{ shrink: true }}
          />
        </Box>
      </Grid>

      <Grid item xs={12}>
        <InputLabel htmlFor="note" sx={{ mb: 1 }}>
          Ghi chú
        </InputLabel>
        <TextareaAutosize
          id="note"
          minRows={3}
          maxRows={6}
          name="note"
          value={formData.note}
          onChange={handleInputChange}
          style={{
            width: "100%",
            padding: "8.5px 14px",
            border: "1px solid rgba(0, 0, 0, 0.23)",
            borderRadius: "4px",
            fontFamily: "inherit",
            fontSize: "1rem",
            resize: "vertical",
          }}
          placeholder="Nhập ghi chú..."
        />
      </Grid>
    </Grid>
  );
};

export default React.memo(OutboundFormFields);