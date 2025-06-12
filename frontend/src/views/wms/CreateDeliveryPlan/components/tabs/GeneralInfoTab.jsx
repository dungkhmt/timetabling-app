import React, { useEffect, useState, useCallback } from "react";
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
  ListSubheader,
} from "@mui/material";
import { useDeliveryPlanForm } from "../../context/DeliveryPlanFormContext";
import { useWms2Data } from "services/useWms2Data";

const GeneralInfoTab = () => {
  const { deliveryPlan, setDeliveryPlan, entities, setEntities } = useDeliveryPlanForm();
  const { getFacilitiesWithFilters } = useWms2Data();

  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [open, setOpen] = useState(false);

  // Load facilities with pagination
  const loadFacilities = useCallback(async (currentPage = 1, append = false) => {
    if (loading) return;

    setLoading(true);
    try {
      const response = await getFacilitiesWithFilters(currentPage, 20, {
        
      });
      if (response && response.code === 200) {
        setTotalPages(response.data.totalPages || 1);

        if (append) {
          // Append new facilities to existing list
          setEntities(prev => ({
            ...prev,
            facilities: [...(prev.facilities || []), ...(response.data.data || [])]
          }));
        } else {
          // Replace facilities with new list
          setEntities(prev => ({
            ...prev,
            facilities: response.data.data || []
          }));
        }
      }
    } catch (error) {
      console.error("Error loading facilities:", error);
    } finally {
      setLoading(false);
    }
  }, [getFacilitiesWithFilters, setEntities]);

  // Initial load when component mounts
  useEffect(() => {
    if (open && entities.facilities?.length === 0) {
      loadFacilities(0, false);
    }
  }, [open, entities.facilities?.length]);

  // Load more facilities when scrolling
  const handleScroll = (event) => {
    const scrollTop = event.target.scrollTop;
    const scrollHeight = event.target.scrollHeight;
    const clientHeight = event.target.clientHeight;

    // When user scrolls to bottom, load more facilities
    if (scrollHeight - scrollTop <= clientHeight * 1.5 && !loading && page < totalPages) {
      const nextPage = page + 1;
      setPage(nextPage);
      loadFacilities(nextPage, true);
    }
  };

  // Handle facility select change
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

  // Handle form input change
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setDeliveryPlan(prev => ({ ...prev, [name]: value }));
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
                      onOpen={() => setOpen(true)}
                      onClose={() => setOpen(false)}
                      MenuProps={{
                        PaperProps: {
                          style: {
                            maxHeight: 300
                          },
                          onScroll: handleScroll
                        }
                      }}
                  >
                    {entities.facilities?.length === 0 && (
                        <MenuItem disabled>
                          <Box display="flex" alignItems="center">
                            <CircularProgress size={20} sx={{ mr: 1 }} />
                            Đang tải dữ liệu...
                          </Box>
                        </MenuItem>
                    )}

                    {entities.facilities?.map((facility) => (
                        <MenuItem
                            key={facility.id}
                            value={facility.id}
                            sx={{
                              display: 'flex',
                              flexDirection: 'column',
                              alignItems: 'flex-start',
                              py: 1.5
                            }}
                        >
                          <Box sx={{ width: '100%' }}>
                            <Typography variant="body1" fontWeight="medium" sx={{ mb: 0.5 }}>
                              {facility.name || facility.id}
                            </Typography>

                            {facility.fullAddress && (
                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                  <Box
                                      component="span"
                                      sx={{
                                        width: 8,
                                        height: 8,
                                        borderRadius: '50%',
                                        bgcolor: 'primary.light',
                                        display: 'inline-block',
                                        mr: 1
                                      }}
                                  />
                                  <Typography
                                      variant="caption"
                                      color="text.secondary"
                                      sx={{
                                        display: 'inline-block',
                                        textOverflow: 'ellipsis',
                                        overflow: 'hidden'
                                      }}
                                  >
                                    {facility.fullAddress}
                                  </Typography>
                                </Box>
                            )}
                          </Box>
                        </MenuItem>
                    ))}

                    {loading && page > 1 && (
                        <ListSubheader sx={{ bgcolor: 'background.paper' }}>
                          <Box display="flex" alignItems="center" justifyContent="center" py={1}>
                            <CircularProgress size={20} sx={{ mr: 1 }} />
                            Đang tải thêm...
                          </Box>
                        </ListSubheader>
                    )}
                  </Select>
                  <FormHelperText>Chọn cơ sở làm điểm xuất phát cho kế hoạch giao hàng</FormHelperText>
                </FormControl>

                {entities.selectedFacility && (
                    <Box mt={2}>
                      <Typography variant="body2" color="textSecondary">
                        <strong>Địa chỉ:</strong> {entities.selectedFacility.fullAddress|| "Không có thông tin"}
                      </Typography>
                      {entities.selectedFacility.phone && (
                          <Typography variant="body2" color="textSecondary" mt={1}>
                            <strong>Điện thoại:</strong> {entities.selectedFacility.phone}
                          </Typography>
                      )}
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