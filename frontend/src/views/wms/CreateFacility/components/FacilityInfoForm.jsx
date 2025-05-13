import React from 'react';
import { TextField, Grid, FormControlLabel, Checkbox } from '@mui/material';

const FacilityInfoForm = ({ facility, errors, onChange }) => {
  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Tên cơ sở"
          name="name"
          value={facility.name}
          onChange={onChange}
          error={!!errors.name}
          helperText={errors.name}
          required
        />
      </Grid>
      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Số điện thoại"
          name="phone"
          value={facility.phone}
          onChange={onChange}
          error={!!errors.phone}
          helperText={errors.phone}
        />
      </Grid>
      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Mã bưu điện"
          name="postalCode"
          value={facility.postalCode}
          onChange={onChange}
          error={!!errors.postalCode}
          helperText={errors.postalCode}
        />
      </Grid>
      <Grid item xs={12}>
        <FormControlLabel
          control={
            <Checkbox
              checked={facility.isDefault}
              onChange={onChange}
              name="isDefault"
              color="primary"
            />
          }
          label="Đặt làm cơ sở mặc định"
        />
      </Grid>
    </Grid>
  );
};

export default FacilityInfoForm;