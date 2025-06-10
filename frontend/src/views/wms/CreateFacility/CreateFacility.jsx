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
  Typography,
  FormControlLabel,
  Checkbox
} from '@mui/material';
import { toast } from 'react-toastify';
import { useHistory } from 'react-router-dom';
import LocationPicker from '../common/components/LocationPicker';
import { useWms2Data } from "../../../services/useWms2Data";
import FacilityInfoForm from './components/FacilityInfoForm';
import AddressForm from './components/AddressForm';

const CreateFacility = () => {
  const history = useHistory();
  const [loading, setLoading] = useState(false);
  const { createFacility } = useWms2Data();
  
  // Facility form state
  const [facility, setFacility] = useState({
    name: '',
    isDefault: false,
    phone: '',
    postalCode: '',
    address: {
      addressType: 'FACILITY',
      latitude: null,
      longitude: null,
      fullAddress: ''
    }
  });

  // Error state for validation
  const [errors, setErrors] = useState({
    name: '',
    phone: '',
    postalCode: '',
    fullAddress: ''
  });

  // Handle input change for facility info
  const handleFacilityChange = (e) => {
    const { name, value, checked } = e.target;
    const newValue = name === 'isDefault' ? checked : value;
    
    setFacility(prev => ({
      ...prev,
      [name]: newValue
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
    setFacility(prev => ({
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
    setFacility(prev => ({
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
    if (!facility.name.trim()) {
      newErrors.name = 'Tên cơ sở không được để trống';
      isValid = false;
    }

    // Validate phone (optional)
    const phoneRegex = /^(\+\d{1,3}[- ]?)?\d{10}$/;
    if (facility.phone.trim() && !phoneRegex.test(facility.phone)) {
      newErrors.phone = 'Số điện thoại không hợp lệ';
      isValid = false;
    }

    // Validate address
    if (!facility.address.fullAddress.trim()) {
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
      const response = await createFacility(facility);
      
      if (response && (response.code === 200 || response.code === 201)) {
        toast.success('Tạo cơ sở thành công');
        history.push('/wms/admin/facility');
      } else {
        toast.error(`Lỗi khi tạo cơ sở: ${response?.message || 'Lỗi không xác định'}`);
      }
    } catch (error) {
      console.error('Error creating facility:', error);
      toast.error(`Lỗi khi tạo cơ sở: ${error?.message || 'Lỗi không xác định'}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="lg">
      <Paper elevation={1} sx={{ p: 3, my: 3 }}>
        <Typography variant="h5" component="h1" gutterBottom>
          Tạo mới cơ sở
        </Typography>
        <Divider sx={{ mb: 3 }} />

        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Thông tin cơ sở
                </Typography>
                <FacilityInfoForm 
                  facility={facility}
                  errors={errors}
                  onChange={handleFacilityChange}
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
                  address={facility.address}
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
                  Vị trí cơ sở
                </Typography>
                <LocationPicker
                  latitude={facility.address.latitude}
                  longitude={facility.address.longitude}
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

export default CreateFacility;