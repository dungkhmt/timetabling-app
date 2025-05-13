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
            <MenuItem value="HOME">Nhà riêng</MenuItem>
            <MenuItem value="WORK">Cơ quan</MenuItem>
            <MenuItem value="SHIPPING">Địa chỉ giao hàng</MenuItem>
              <MenuItem value= "OTHER">Khác</MenuItem>
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
          onChange={onChange}
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
          onChange={onChange}
          InputProps={{
            readOnly: true,
          }}
        />
      </Grid>
      <Grid item xs={12}>
        <FormControlLabel
          control={
            <Checkbox
              checked={address.isDefault}
              onChange={(e) => onChange({
                target: { name: 'isDefault', value: e.target.checked }
              })}
              name="isDefault"
              color="primary"
            />
          }
          label="Đặt làm địa chỉ mặc định"
        />
      </Grid>
    </Grid>
  );
};

export default AddressForm;