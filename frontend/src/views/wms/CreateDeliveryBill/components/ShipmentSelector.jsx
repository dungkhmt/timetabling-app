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
  Grid,
  Chip
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import { useDeliveryBillForm } from "../../common/context/DeliveryBillFormContext";
import { useWms2Data } from "services/useWms2Data";
import { getShipmentStatus } from "views/wms/common/utils/functions";

const ShipmentSelector = () => {
  const { deliveryBill, setDeliveryBill, entities, setEntities } = useDeliveryBillForm();
  const { getOutBoundsForDeliveryBill } = useWms2Data();
  
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [inputValue, setInputValue] = useState('');
  const [open, setOpen] = useState(false);
  
  // Fetch shipments with pagination (only when facility is selected)
  const fetchShipments = useCallback(async (currentPage = 0, replace = true) => {
    if (loading || !deliveryBill.facilityId) return;
    
    setLoading(true);
    try {
      const response = await getOutBoundsForDeliveryBill(currentPage, 20, deliveryBill.facilityId);
      if (response && response.code === 200) {
        setTotalPages(response.data.totalPages);
        
        if (replace) {
          setEntities(prev => ({
            ...prev,
            shipments: response.data.data || []
          }));
        } else {
          setEntities(prev => ({
            ...prev,
            shipments: [...(prev.shipments || []), ...(response.data.data || [])]
          }));
        }
      }
    } catch (error) {
      console.error("Error fetching shipments:", error);
    } finally {
      setLoading(false);
    }
  }, [getOutBoundsForDeliveryBill, deliveryBill.facilityId, setEntities]);

  // Fetch data when facility changes or dropdown opens
  useEffect(() => {
    if (open && deliveryBill.facilityId) {
      fetchShipments(0, true);
    }
  }, [open, deliveryBill.facilityId]);

  // Load more data when scrolling
  const loadMoreItems = () => {
    if (page < totalPages && !loading) {
      const nextPage = page + 1;
      setPage(nextPage);
      fetchShipments(nextPage, false);
    }
  };

  // Handle shipment selection
  const handleShipmentChange = (event, newValue) => {
    if (!newValue) {
      setEntities(prev => ({
        ...prev,
        selectedShipment: null
      }));
      
      setDeliveryBill(prev => ({
        ...prev,
        shipmentId: "",
        deliveryBillName: "",
        deliveryAddressId: "",
        products: []
      }));
      return;
    }

    // Update selected shipment
    setEntities(prev => ({
      ...prev,
      selectedShipment: newValue
    }));
    
    // Update delivery bill info
    setDeliveryBill(prev => ({
      ...prev,
      shipmentId: newValue.id,
      deliveryBillName: `Phiếu giao: ${newValue.shipmentName || newValue.id}`,
      deliveryAddressId: newValue.deliveryAddressId || "",
      // Transform shipment items to delivery bill products
      products: newValue.shipmentItems ? newValue.shipmentItems.map(item => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        maxQuantity: item.quantity,
        price: item.price || 0,
        weight: item.weight || 0,
        unit: item.unit || "",
        selected: true
      })) : []
    }));
  };

  // Get status color based on shipment status
  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case "EXPORTED":
        return "primary";
      case "SHIPPED":
      case "PARTIALLY_DELIVERED":
      case "DELIVERED":
        return "success";
      case "PENDING":
      case "CREATED":
        return "warning";
      case "CANCELLED":
        return "error";
      default:
        return "default";
    }
  };

  const selectedShipment = entities.selectedShipment;
  const selectedFacility = entities.selectedFacility;

  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Chọn phiếu xuất
        </Typography>

        {!selectedFacility && (
          <Alert severity="warning">
            Vui lòng chọn kho hàng trước
          </Alert>
        )}

        {selectedFacility && (
          <Box mb={3}>
            <Autocomplete
              options={entities.shipments || []}
              getOptionLabel={(option) => 
                `${option.shipmentName || option.id} - ${option.toCustomerName || 'Không có thông tin'}`
              }
              loading={loading}
              value={selectedShipment || null}
              onChange={handleShipmentChange}
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
              disabled={!selectedFacility}
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
                  label="Tìm kiếm phiếu xuất"
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
                    <Typography variant="body1">{option.shipmentName || option.id}</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                      <Typography variant="body2" color="text.secondary">
                        {option.toCustomerName || 'Không có thông tin'}
                      </Typography>
                      <Chip 
                        label={getShipmentStatus(option.statusId) || "EXPORTED"} 
                        size="small" 
                        color={getStatusColor(option.statusId)}
                      />
                    </Box>
                  </Box>
                </li>
              )}
            />
          </Box>
        )}

        {selectedShipment ? (
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Mã phiếu xuất:
              </Typography>
              <Typography variant="body1">
                {selectedShipment.id.substring(0, 8) || '-'}
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Tên phiếu xuất:
              </Typography>
              <Typography variant="body1">
                {selectedShipment.shipmentName || '-'}
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Trạng thái:
              </Typography>
              <Chip 
                label={getShipmentStatus(selectedShipment.statusId) || "EXPORTED"} 
                size="small" 
                color={getStatusColor(selectedShipment.statusId)}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Khách hàng:
              </Typography>
              <Typography variant="body1">
                {selectedShipment.toCustomerName || '-'}
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Tổng số lượng:
              </Typography>
              <Typography variant="body1">
                {selectedShipment.totalQuantity || 0}
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Tổng khối lượng:
              </Typography>
              <Typography variant="body1">
                {selectedShipment.totalWeight || 0} kg
              </Typography>
            </Grid>
            <Grid item xs={12}>
              <Typography variant="body2" color="textSecondary">
                Địa chỉ giao hàng:
              </Typography>
              <Typography variant="body1">
                {selectedShipment.deliveryFullAddress || '-'}
              </Typography>
            </Grid>
          </Grid>
        ) : selectedFacility ? (
          <Alert severity="info">
            Vui lòng chọn phiếu xuất để tạo phiếu giao hàng
          </Alert>
        ) : null}
      </CardContent>
    </Card>
  );
};

export default ShipmentSelector;