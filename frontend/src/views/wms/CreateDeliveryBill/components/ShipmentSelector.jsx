import React, { useEffect, useState, useCallback, useRef } from "react";
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

const ShipmentSelector = () => {
  const { deliveryBill, setDeliveryBill, entities, setEntities } = useDeliveryBillForm();
  const { getOutBoundsForDeliveryBill, getOutBoundDetail } = useWms2Data();
  
  const [loading, setLoading] = useState(false);
  const [searching, setSearching] = useState(false);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [inputValue, setInputValue] = useState('');
  const [open, setOpen] = useState(false);
  
  // Fetch shipments with pagination
  const fetchShipments = useCallback(async (currentPage = 0, replace = true) => {
    if (loading) return;
    
    setLoading(true);
    try {
      const response = await getOutBoundsForDeliveryBill(currentPage, 20);
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
  }, [getOutBoundsForDeliveryBill, setEntities]);

  // Initial data load
  useEffect(() => {
    if (open) {
      fetchShipments(0, true);
    }
  }, [open]);

  // Load more data when scrolling
  const loadMoreItems = () => {
    if (page < totalPages && !loading) {
      const nextPage = page + 1;
      setPage(nextPage);
      fetchShipments(nextPage, false);
    }
  };

  // Handle shipment selection
  const handleShipmentChange = async (event, newValue) => {
    if (!newValue) {
      setEntities(prev => ({
        ...prev,
        selectedShipment: null
      }));
      
      setDeliveryBill(prev => ({
        ...prev,
        shipmentId: "",
        deliveryBillName: "",
        products: []
      }));
      return;
    }

    setSearching(true);
    try {
      const response = await getOutBoundDetail(newValue.id);
      if (response && response.code === 200) {
        const shipment = response.data;
        
        // Update selected shipment
        setEntities(prev => ({
          ...prev,
          selectedShipment: shipment
        }));
        
        // Update delivery bill info
        setDeliveryBill(prev => ({
          ...prev,
          shipmentId: shipment.id,
          deliveryBillName: `Phiếu giao: ${shipment.shipmentName || shipment.id}`
        }));
        
        // Create products with selection state if the shipment has products
        if (shipment.products && shipment.products.length > 0) {
          const productsWithSelection = shipment.products.map(product => ({
            productId: product.productId,
            productName: product.productName,
            quantity: product.quantity,
            maxQuantity: product.quantity,
            selected: true
          }));
          
          setDeliveryBill(prev => ({
            ...prev,
            products: productsWithSelection
          }));
        } else {
          setDeliveryBill(prev => ({
            ...prev,
            products: []
          }));
        }
      }
    } catch (error) {
      console.error("Error fetching shipment details:", error);
    } finally {
      setSearching(false);
    }
  };

 // Get status color based on shipment status
const getStatusColor = (status) => {
  switch (status?.toUpperCase()) {
    case "EXPORTED":
      return "primary";
    case "SHIPPED":
    case "PARTIALLY_DELIVERED":
    case "DELIVERED":
    case "IMPORTED":
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

  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Thông tin lô hàng
        </Typography>

        <Box mb={3}>
          <Autocomplete
            options={entities.shipments || []}
            getOptionLabel={(option) => 
              `${option.shipmentName || option.id} - ${option.customerName || 'Không có thông tin'}`
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
                label="Tìm kiếm lô hàng đã xuất kho"
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
                      {option.customerName || 'Không có thông tin'}
                    </Typography>
                    <Chip 
                      label={option.statusId || "EXPORTED"} 
                      size="small" 
                      color={getStatusColor(option.statusId)}
                    />
                  </Box>
                </Box>
              </li>
            )}
          />
        </Box>

        {searching ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
            <CircularProgress size={24} />
          </Box>
        ) : selectedShipment ? (
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Mã lô hàng:
              </Typography>
              <Typography variant="body1">
                {selectedShipment.id}
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Tên lô hàng:
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
                label={selectedShipment.statusId || "EXPORTED"} 
                size="small" 
                color={getStatusColor(selectedShipment.statusId)}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2" color="textSecondary">
                Khách hàng:
              </Typography>
              <Typography variant="body1">
                {selectedShipment.customerName || '-'}
              </Typography>
            </Grid>
            <Grid item xs={12}>
              <Typography variant="body2" color="textSecondary">
                Địa chỉ giao hàng:
              </Typography>
              <Typography variant="body1">
                {selectedShipment.deliveryAddress || '-'}
              </Typography>
            </Grid>
          </Grid>
        ) : (
          <Alert severity="info">
            Vui lòng chọn lô hàng đã xuất kho để tạo phiếu giao
          </Alert>
        )}
      </CardContent>
    </Card>
  );
};

export default ShipmentSelector;