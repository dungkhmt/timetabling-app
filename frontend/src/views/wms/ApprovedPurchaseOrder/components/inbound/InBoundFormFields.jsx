import React from "react";
import { Grid, TextareaAutosize, TextField, Typography, Box, InputLabel } from "@mui/material";

const InBoundFormFields = ({ formData, errors, onChange }) => {
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    onChange(name, value);
  };

  const today = new Date().toISOString().split('T')[0];

  return (
    <Box>
      <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', mb: 2 }}>
        Thông tin phiếu nhập
      </Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Box>
            <InputLabel htmlFor="shipmentName" sx={{ mb: 1, fontWeight: 'medium' }}>
              Tên phiếu nhập *
            </InputLabel>
            <TextField
              id="shipmentName"
              name="shipmentName"
              fullWidth
              value={formData.shipmentName}
              onChange={handleInputChange}
              error={!!errors.shipmentName}
              helperText={errors.shipmentName}
              size="small"
              placeholder="Nhập tên phiếu nhập..."
            />
          </Box>
        </Grid>

        <Grid item xs={12} md={6}>
          <Box>
            <InputLabel htmlFor="expectedDeliveryDate" sx={{ mb: 1, fontWeight: 'medium' }}>
              Ngày dự kiến giao hàng *
            </InputLabel>
            <TextField
              id="expectedDeliveryDate"
              fullWidth
              type="date"
              name="expectedDeliveryDate"
              value={formData.expectedDeliveryDate || ''}
              onChange={handleInputChange}
              size="small"
              InputLabelProps={{ shrink: true }}
              inputProps={{ min: today }}
              error={!!errors.expectedDeliveryDate}
              helperText={errors.expectedDeliveryDate}
            />
          </Box>
        </Grid>

        <Grid item xs={12}>
          <InputLabel htmlFor="note" sx={{ mb: 1, fontWeight: 'medium' }}>
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
              border: errors.note ? "1px solid #d32f2f" : "1px solid rgba(0, 0, 0, 0.23)",
              borderRadius: "4px",
              fontFamily: "inherit",
              fontSize: "1rem",
              resize: "vertical",
              outline: "none",
            }}
            placeholder="Nhập ghi chú cho phiếu nhập..."
          />
          {errors.note && (
            <Typography variant="caption" color="error" sx={{ mt: 0.5, display: 'block' }}>
              {errors.note}
            </Typography>
          )}
        </Grid>
      </Grid>
    </Box>
  );
};

export default React.memo(InBoundFormFields);