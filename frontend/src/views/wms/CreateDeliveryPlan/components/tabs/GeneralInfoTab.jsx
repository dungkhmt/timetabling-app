import React, { useEffect, useState } from "react";
import {
  Box,
  TextField,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
  CircularProgress,
  FormHelperText,
  Card,
  CardContent,
} from "@mui/material";
import { useDeliveryPlanForm } from "../../context/DeliveryPlanFormContext";
import { useWms2Data } from "services/useWms2Data";

const GeneralInfoTab = () => {
  const { deliveryPlan, setDeliveryPlan, entities, setEntities } = useDeliveryPlanForm();
  const { getFacilities } = useWms2Data();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const loadFacilities = async () => {
      setLoading(true);
      try {
        const response = await getFacilities();
        if (response && response.code === 200) {
          setEntities(prev => ({
            ...prev,
            facilities: response.data || []
          }));
        }
      } catch (error) {
        console.error("Error loading facilities:", error);
      } finally {
        setLoading(false);
      }
    };

    loadFacilities();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setDeliveryPlan(prev => ({ ...prev, [name]: value }));
  };

  const handleFacilityChange = (e) => {
    const facilityId = e.target.value;
    setDeliveryPlan(prev => ({
      ...prev,
      facilityId
    }));
    
    const selectedFacility = entities.facilities.find(f => f.id === facilityId);
    setEntities(prev => ({
      ...prev,
      selectedFacility
    }));
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Thông tin cơ bản
      </Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card variant="outlined">
            <CardContent>
              <Typography variant="subtitle1" gutterBottom>
                Thông tin kế hoạch giao hàng
              </Typography>
              
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Tên kế hoạch giao hàng"
                    name="deliveryPlanName"
                    value={deliveryPlan.deliveryPlanName}
                    onChange={handleInputChange}
                    required
                    helperText="Nhập tên cho kế hoạch giao hàng"
                    margin="normal"
                  />
                </Grid>
                
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Ngày giao hàng"
                    type="date"
                    name="deliveryDate"
                    value={deliveryPlan.deliveryDate}
                    onChange={handleInputChange}
                    required
                    InputLabelProps={{
                      shrink: true
                    }}
                    margin="normal"
                  />
                </Grid>
                
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Mô tả"
                    name="description"
                    value={deliveryPlan.description}
                    onChange={handleInputChange}
                    multiline
                    rows={4}
                    margin="normal"
                    helperText="Nhập mô tả chi tiết về kế hoạch giao hàng"
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={6}>
          <Card variant="outlined">
            <CardContent>
              <Typography variant="subtitle1" gutterBottom>
                Cơ sở xuất phát
              </Typography>
              
              <FormControl fullWidth margin="normal" required>
                <InputLabel id="facility-select-label">Chọn cơ sở</InputLabel>
                <Select
                  labelId="facility-select-label"
                  id="facility-select"
                  value={deliveryPlan.facilityId}
                  label="Chọn cơ sở"
                  onChange={handleFacilityChange}
                  disabled={loading}
                  startAdornment={
                    loading ? 
                      <CircularProgress size={20} color="inherit" sx={{ mr: 1 }} /> :
                      null
                  }
                >
                  {entities.facilities.map((facility) => (
                    <MenuItem key={facility.id} value={facility.id}>
                      {facility.facilityName || facility.id}
                    </MenuItem>
                  ))}
                </Select>
                <FormHelperText>Chọn cơ sở làm điểm xuất phát cho kế hoạch giao hàng</FormHelperText>
              </FormControl>
              
              {entities.selectedFacility && (
                <Box mt={2}>
                  <Typography variant="body2" color="textSecondary">
                    Địa chỉ: {entities.selectedFacility.address || "Không có thông tin"}
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default GeneralInfoTab;