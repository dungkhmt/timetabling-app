import React from 'react';
import {
  Box,
  Grid,
  TextField,
  Divider,
  Typography
} from '@mui/material';
import { useProductForm } from '../context/ProductFormContext';

const PricingInfoForm = () => {
  const { product, setProduct } = useProductForm();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProduct(prev => ({ ...prev, [name]: value }));
  };

  return (
    <Box>
      <Divider sx={{ mb: 2 }} />
      <Grid container spacing={4}> 
        {/* Giá bán lẻ */}
        <Grid item xs={12} md={6} sx={{ pr: { md: 2 } }}> {/* Thêm padding-right cho màn hình trung bình trở lên */}
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Typography variant="body1" sx={{ pt: 1 }}>
                Giá bán lẻ:
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <TextField
                fullWidth
                size="small"
                name="retailPrice"
                type="number"
                value={product.retailPrice}
                onChange={handleInputChange}
                InputProps={{ inputProps: { min: 0, step: 0.01 } }}
                placeholder="VD: 100000"
              />
            </Grid>
          </Grid>
        </Grid>
        
        {/* Giá bán buôn */}
        <Grid item xs={12} md={6} sx={{ pl: { md: 2 } }}> {/* Thêm padding-left cho màn hình trung bình trở lên */}
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Typography variant="body1" sx={{ pt: 1 }}>
                Giá bán buôn:
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <TextField
                fullWidth
                size="small"
                name="wholeSalePrice"
                type="number"
                value={product.wholeSalePrice}
                onChange={handleInputChange}
                InputProps={{ inputProps: { min: 0, step: 0.01 } }}
                placeholder="VD: 90000"
              />
            </Grid>
          </Grid>
        </Grid>

        {/* Giá nhập trong một hàng */}
        <Grid item xs={12} md={6} sx={{ pr: { md: 2 } }}> {/* Thêm padding-right cho màn hình trung bình trở lên */}
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Typography variant="body1" sx={{ pt: 1 }}>
                Giá nhập:
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <TextField
                fullWidth
                size="small"
                name="costPrice"
                type="number"
                value={product.costPrice}
                onChange={handleInputChange}
                InputProps={{ inputProps: { min: 0, step: 0.01 } }}
                placeholder="VD: 80000"
              />
            </Grid>
          </Grid>
        </Grid>
      </Grid>
    </Box>
  );
};

export default PricingInfoForm;