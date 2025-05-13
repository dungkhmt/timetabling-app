import React from 'react';
import { TextField, Grid } from '@mui/material';

const CustomerInfoForm = ({ customer, errors, onChange }) => {
  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Tên khách hàng"
          name="name"
          value={customer.name}
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
          value={customer.email}
          onChange={onChange}
          error={!!errors.email}
          helperText={errors.email}
          required
        />
      </Grid>
      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Số điện thoại"
          name="phone"
          value={customer.phone}
          onChange={onChange}
          error={!!errors.phone}
          helperText={errors.phone}
          required
        />
      </Grid>
    </Grid>
  );
};

export default CustomerInfoForm;