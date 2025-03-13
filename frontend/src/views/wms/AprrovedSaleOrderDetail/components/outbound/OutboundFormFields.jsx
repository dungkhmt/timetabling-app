import React from "react";
import {
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography
} from "@mui/material";
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';

/**
 * Component chứa các trường nhập liệu cho phiếu xuất kho
 */
const OutboundFormFields = ({ formData, facilities, errors, onChange, onFetchFacilities }) => {
  
  // Handle input changes for regular inputs
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    onChange(name, value);
  };
  
  // Handle date change (special case)
  const handleDateChange = (newDate) => {
    onChange('shipDate', newDate);
  };
  
  return (
    <>
      {/* Warehouse selection */}
      <Grid item xs={12} md={6}>
      </Grid>
      
      {/* Ship date */}
      <Grid item xs={12} md={6}>
        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <DatePicker
            label="Ngày xuất kho"
            value={formData.shipDate}
            onChange={handleDateChange}
            renderInput={(params) => (
              <TextField 
                {...params} 
                fullWidth 
                error={!!errors.shipDate}
                helperText={errors.shipDate}
              />
            )}
          />
        </LocalizationProvider>
      </Grid>
      
      {/* Note */}
      <Grid item xs={12}>
        <TextField
          name="note"
          label="Ghi chú"
          multiline
          rows={2}
          fullWidth
          value={formData.note}
          onChange={handleInputChange}
        />
      </Grid>
    </>
  );
};

export default React.memo(OutboundFormFields);