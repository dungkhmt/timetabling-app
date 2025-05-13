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
  TextField,
  Typography
} from '@mui/material';
import { toast } from 'react-toastify';
import { useHistory } from 'react-router-dom';
import CustomerInfoForm from './components/CustomerInfoForm';
import AddressForm from './components/AddressForm';
import LocationPicker from './components/LocationPicker';
import {useWms2Data} from "../../../services/useWms2Data";

const CreateCustomer = () => {
  const history = useHistory();
  const [loading, setLoading] = useState(false);
  const {createCustomer} = useWms2Data();
  // Customer form state
  const [customer, setCustomer] = useState({
    name: '',
    email: '',
    phone: '',
    address: {
      addressType: 'SHIPPING',
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

  // Handle input change for customer info
  const handleCustomerChange = (e) => {
    const { name, value } = e.target;
    setCustomer(prev => ({
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
    setCustomer(prev => ({
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
    setCustomer(prev => ({
      ...prev,
      address: {
        ...prev.address,
        latitude: lat,
        longitude: lng,
        fullAddress: address || prev.address.fullAddress
      }
    }));
  };

  // Validate the form
  const validateForm = () => {
    const newErrors = {};
    let isValid = true;

    // Validate name
    if (!customer.name.trim()) {
      newErrors.name = 'Tên khách hàng không được để trống';
      isValid = false;
    }

    // Validate email with regex
    const emailRegex = /^[A-Za-z0-9+_.-]+@(.+)$/;
    if (!customer.email.trim()) {
      newErrors.email = 'Email không được để trống';
      isValid = false;
    } else if (!emailRegex.test(customer.email)) {
      newErrors.email = 'Email không hợp lệ';
      isValid = false;
    }

    // Validate phone with regex
    const phoneRegex = /^(\+\d{1,3}[- ]?)?\d{10}$/;
    if (!customer.phone.trim()) {
      newErrors.phone = 'Số điện thoại không được để trống';
      isValid = false;
    } else if (!phoneRegex.test(customer.phone)) {
      newErrors.phone = 'Số điện thoại không hợp lệ';
      isValid = false;
    }

    // Validate address
    if (!customer.address.fullAddress.trim()) {
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
      const response = await createCustomer(customer);
      
      if (response && (response.code === 200 || response.code === 201)) {
        toast.success('Tạo khách hàng thành công');
        history.push('/wms/sales/customers');
      } else {
        toast.error(`Lỗi khi tạo khách hàng: ${response?.message || 'Lỗi không xác định'}`);
      }
    } catch (error) {
      console.error('Error creating customer:', error);
      toast.error(`Lỗi khi tạo khách hàng: ${error?.message || 'Lỗi không xác định'}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="lg">
      <Paper elevation={1} sx={{ p: 3, my: 3 }}>
        <Typography variant="h5" component="h1" gutterBottom>
          Tạo mới khách hàng
        </Typography>
        <Divider sx={{ mb: 3 }} />

        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Thông tin khách hàng
                </Typography>
                <CustomerInfoForm 
                  customer={customer}
                  errors={errors}
                  onChange={handleCustomerChange}
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
                  address={customer.address}
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
                  Vị trí khách hàng
                </Typography>
                <LocationPicker
                  latitude={customer.address.latitude}
                  longitude={customer.address.longitude}
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

export default CreateCustomer;