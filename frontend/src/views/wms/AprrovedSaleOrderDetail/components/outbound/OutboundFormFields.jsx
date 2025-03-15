import React from "react";
import { Grid, TextField } from "@mui/material";

const OutboundFormFields = ({ formData, errors, onChange }) => {
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    onChange(name, value);
  };

  return (
    <Grid item xs={12}>
      <TextField
        name="note"
        label="Ghi chú"
        multiline
        rows={2}
        fullWidth
        value={formData.note}
        onChange={handleInputChange}
        error={!!errors.note}
        helperText={errors.note}
      />
    </Grid>
  );
};

export default React.memo(OutboundFormFields);