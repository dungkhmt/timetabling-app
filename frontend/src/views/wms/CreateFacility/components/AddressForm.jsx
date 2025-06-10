import React from 'react';
import { 
  TextField, 
  Grid, 
  FormControl, 
  InputLabel, 
  Select, 
  MenuItem,
  FormControlLabel,
  Checkbox
} from '@mui/material';

const AddressForm = ({ address, errors, onChange }) => {
  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <FormControl fullWidth>
          <InputLabel id="address-type-label">Loại địa chỉ</InputLabel>
          <Select
            labelId="address-type-label"
            name="addressType"
            value={address.addressType}
            onChange={onChange}
            label="Loại địa chỉ"
          >
            <MenuItem value="FACILITY">Kho hàng</MenuItem>
            <MenuItem value="DISTRIBUTION_CENTER">Trung tâm phân phối</MenuItem>
            <MenuItem value="BRANCH">Chi nhánh</MenuItem>
            <MenuItem value="OTHER">Khác</MenuItem>
          </Select>
        </FormControl>
      </Grid>
      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Địa chỉ đầy đủ"
          name="fullAddress"
          value={address.fullAddress}
          onChange={onChange}
          error={!!errors.fullAddress}
          helperText={errors.fullAddress}
          required
          multiline
          rows={3}
        />
      </Grid>
      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Vĩ độ (Latitude)"
          name="latitude"
          value={address.latitude !== null ? address.latitude : ''}
          InputProps={{
            readOnly: true,
          }}
        />
      </Grid>
      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Kinh độ (Longitude)"
          name="longitude"
          value={address.longitude !== null ? address.longitude : ''}
          InputProps={{
            readOnly: true,
          }}
        />
      </Grid>
    </Grid>
  );
};

export default AddressForm;