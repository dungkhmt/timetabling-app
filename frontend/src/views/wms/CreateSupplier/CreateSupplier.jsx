import React, { useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Container,
  Divider,
  Grid,
  Paper,
  Typography
} from '@mui/material';
import { toast } from 'react-toastify';
import { useHistory } from 'react-router-dom';
import SupplierInfoForm from './components/SupplierInfoForm';
import AddressForm from './components/AddressForm';
import LocationPicker from '../common/components/LocationPicker';
import { useWms2Data } from "../../../services/useWms2Data";

const CreateSupplier = () => {
  const history = useHistory();
  const [loading, setLoading] = useState(false);
  const { createSupplier } = useWms2Data();
  
  // Supplier form state
  const [supplier, setSupplier] = useState({
    name: '',
    email: '',
    phone: '',
    address: {
      addressType: 'BUSINESS',
      latitude: null,
      longitude: null,
      isDefault: true,
      fullAddress: ''
    }
  });

  // Error state for validation
  const [errors, setErrors] = useState({
    name: '',
    email: '',
    phone: '',
    fullAddress: ''
  });

  // Handle input change for supplier info
  const handleSupplierChange = (e) => {
    const { name, value } = e.target;
    setSupplier(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when field is updated
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  // Handle change for address fields
  const handleAddressChange = (e) => {
    const { name, value } = e.target;
    setSupplier(prev => ({
      ...prev,
      address: {
        ...prev.address,
        [name]: value
      }
    }));
    
    // Clear error when field is updated
    if (name === 'fullAddress' && errors.fullAddress) {
      setErrors(prev => ({
        ...prev,
        fullAddress: ''
      }));
    }
  };

  // Handle location selection from the map
  const handleLocationSelect = (lat, lng, address) => {
    setSupplier(prev => ({
      ...prev,
      address: {
        ...prev.address,
        latitude: lat,
        longitude: lng,
        fullAddress: address || prev.address.fullAddress
      }
    }));
    
    // Clear address error if we have a valid address now
    if (address && errors.fullAddress) {
      setErrors(prev => ({
        ...prev,
        fullAddress: ''
      }));
    }
  };

  // Validate the form
  const validateForm = () => {
    const newErrors = {};
    let isValid = true;

    // Validate name
    if (!supplier.name.trim()) {
      newErrors.name = 'Tên nhà cung cấp không được để trống';
      isValid = false;
    }

    // Validate email with regex
    const emailRegex = /^[A-Za-z0-9+_.-]+@(.+)$/;
    if (supplier.email.trim() && !emailRegex.test(supplier.email)) {
      newErrors.email = 'Email không hợp lệ';
      isValid = false;
    }

    // Validate phone with regex
    const phoneRegex = /^(\+\d{1,3}[- ]?)?\d{10}$/;
    if (!supplier.phone.trim()) {
      newErrors.phone = 'Số điện thoại không được để trống';
      isValid = false;
    } else if (!phoneRegex.test(supplier.phone)) {
      newErrors.phone = 'Số điện thoại không hợp lệ';
      isValid = false;
    }

    // Validate address
    if (!supplier.address.fullAddress.trim()) {
      newErrors.fullAddress = 'Địa chỉ không được để trống';
      isValid = false;
    }

    setErrors(newErrors);
    return isValid;
  };

  // Handle form submission
  const handleSubmit = async () => {
    if (!validateForm()) {
      toast.error('Vui lòng kiểm tra lại thông tin');
      return;
    }

    setLoading(true);

    try {
      const response = await createSupplier(supplier);
      
      if (response && (response.code === 200 || response.code === 201)) {
        toast.success('Tạo nhà cung cấp thành công');
        history.push('/wms/purchase/suppliers');
      } else {
        toast.error(`Lỗi khi tạo nhà cung cấp: ${response?.message || 'Lỗi không xác định'}`);
      }
    } catch (error) {
      console.error('Error creating supplier:', error);
      toast.error(`Lỗi khi tạo nhà cung cấp: ${error?.message || 'Lỗi không xác định'}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="lg">
      <Paper elevation={1} sx={{ p: 3, my: 3 }}>
        <Typography variant="h5" component="h1" gutterBottom>
          Tạo mới nhà cung cấp
        </Typography>
        <Divider sx={{ mb: 3 }} />

        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Thông tin nhà cung cấp
                </Typography>
                <SupplierInfoForm 
                  supplier={supplier}
                  errors={errors}
                  onChange={handleSupplierChange}
                />
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={6}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Thông tin địa chỉ
                </Typography>
                <AddressForm 
                  address={supplier.address}
                  errors={errors}
                  onChange={handleAddressChange}
                />
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Vị trí nhà cung cấp
                </Typography>
                <LocationPicker
                  latitude={supplier.address.latitude}
                  longitude={supplier.address.longitude}
                  onLocationSelect={handleLocationSelect}
                />
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        <Box mt={3} display="flex" justifyContent="flex-end" gap={2}>
          <Button 
            variant="outlined" 
            onClick={() => history.goBack()}
            disabled={loading}
          >
            Hủy
          </Button>
          <Button 
            variant="contained" 
            color="primary" 
            onClick={handleSubmit}
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : null}
          >
            {loading ? 'Đang xử lý...' : 'Lưu'}
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default CreateSupplier;