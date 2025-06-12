import React, { useEffect, useState, useCallback } from "react";
import {
  Box,
  Typography,
  Card,
  CardContent,
  Autocomplete,
  TextField,
  CircularProgress,
  InputAdornment,
  Alert,
  Grid
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import { useDeliveryBillForm } from "../../common/context/DeliveryBillFormContext";
import { useWms2Data } from "services/useWms2Data";

const FacilitySelector = () => {
  const { deliveryBill, setDeliveryBill, entities, setEntities } = useDeliveryBillForm();
  const { getFacilitiesWithFilters } = useWms2Data();
  
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [inputValue, setInputValue] = useState('');
  const [open, setOpen] = useState(false);
  
  // Fetch facilities with pagination
  const fetchFacilities = useCallback(async (currentPage = 0, replace = true) => {
    if (loading) return;
    
    setLoading(true);
    try {
      const response = await getFacilitiesWithFilters(currentPage, 20, {});
      if (response && response.code === 200) {
        setTotalPages(response.data.totalPages);
        
        if (replace) {
          setEntities(prev => ({
            ...prev,
            facilities: response.data.data || []
          }));
        } else {
          setEntities(prev => ({
            ...prev,
            facilities: [...(prev.facilities || []), ...(response.data.data || [])]
          }));
        }
      }
    } catch (error) {
      console.error("Error fetching facilities:", error);
    } finally {
      setLoading(false);
    }
  }, [getFacilitiesWithFilters, setEntities]);

  // Initial data load
  useEffect(() => {
    if (open) {
      fetchFacilities(0, true);
    }
  }, [open]);

  // Load more data when scrolling
  const loadMoreItems = () => {
    if (page < totalPages && !loading) {
      const nextPage = page + 1;
      setPage(nextPage);
      fetchFacilities(nextPage, false);
    }
  };

  // Handle facility selection
  const handleFacilityChange = (event, newValue) => {
    if (!newValue) {
      setEntities(prev => ({
        ...prev,
        selectedFacility: null,
        shipments: [],
        selectedShipment: null
      }));
      
      setDeliveryBill(prev => ({
        ...prev,
        facilityId: "",
        shipmentId: "",
        deliveryBillName: "",
        deliveryAddressId: "",
        products: []
      }));
      return;
    }

    // Update selected facility
    setEntities(prev => ({
      ...prev,
      selectedFacility: newValue,
      shipments: [], // Clear shipments when facility changes
      selectedShipment: null
    }));
    
    // Update delivery bill info
    setDeliveryBill(prev => ({
      ...prev,
      facilityId: newValue.id,
      shipmentId: "",
      deliveryBillName: "",
      deliveryAddressId: "",
      products: []
    }));
  };

  const selectedFacility = entities.selectedFacility;

  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Chọn kho hàng
        </Typography>

        <Box mb={3}>
          <Autocomplete
            options={entities.facilities || []}
            getOptionLabel={(option) => 
              `${option.name || option.id} - ${option.fullAddress || 'Không có địa chỉ'}`
            }
            loading={loading}
            value={selectedFacility || null}
            onChange={handleFacilityChange}
            onInputChange={(event, newInputValue) => {
              setInputValue(newInputValue);
            }}
            open={open}
            onOpen={() => {
              setOpen(true);
            }}
            onClose={() => {
              setOpen(false);
            }}
            isOptionEqualToValue={(option, value) => option.id === value.id}
            filterOptions={(options, state) => options}
            ListboxProps={{
              onScroll: (event) => {
                const listboxNode = event.currentTarget;
                if (
                  listboxNode.scrollTop + listboxNode.clientHeight >=
                  listboxNode.scrollHeight - 50
                ) {
                  loadMoreItems();
                }
              },
            }}
            renderInput={(params) => (
              <TextField
                {...params}
                label="Tìm kiếm kho hàng"
                variant="outlined"
                size="small"
                InputProps={{
                  ...params.InputProps,
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon />
                    </InputAdornment>
                  ),
                  endAdornment: (
                    <>
                      {loading ? <CircularProgress color="inherit" size={20} /> : null}
                      {params.InputProps.endAdornment}
                    </>
                  )
                }}
              />
            )}
            renderOption={(props, option) => (
              <li {...props}>
                <Box>
                  <Typography variant="body1">{option.name || option.id}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {option.fullAddress || 'Không có địa chỉ'}
                  </Typography>
                </Box>
              </li>
            )}
          />
        </Box>

        {selectedFacility ? (
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Mã kho:
              </Typography>
              <Typography variant="body1">
                {selectedFacility.id}
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Tên kho:
              </Typography>
              <Typography variant="body1">
                {selectedFacility.name || '-'}
              </Typography>
            </Grid>
            <Grid item xs={12}>
              <Typography variant="body2" color="textSecondary">
                Địa chỉ:
              </Typography>
              <Typography variant="body1">
                {selectedFacility.fullAddress || '-'}
              </Typography>
            </Grid>
          </Grid>
        ) : (
          <Alert severity="info">
            Vui lòng chọn kho hàng để xem danh sách phiếu xuất
          </Alert>
        )}
      </CardContent>
    </Card>
  );
};

export default FacilitySelector;