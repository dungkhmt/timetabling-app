import React from 'react';
import { TextField, Grid } from '@mui/material';

const SupplierInfoForm = ({ supplier, errors, onChange }) => {
  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Tên nhà cung cấp"
          name="name"
          value={supplier.name}
          onChange={onChange}
          error={!!errors.name}
          helperText={errors.name}
          required
        />
      </Grid>
      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Email"
          name="email"
          type="email"
          value={supplier.email}
          onChange={onChange}
          error={!!errors.email}
          helperText={errors.email}
        />
      </Grid>
      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Số điện thoại"
          name="phone"
          value={supplier.phone}
          onChange={onChange}
          error={!!errors.phone}
          helperText={errors.phone}
          required
        />
      </Grid>
    </Grid>
  );
};

export default SupplierInfoForm;