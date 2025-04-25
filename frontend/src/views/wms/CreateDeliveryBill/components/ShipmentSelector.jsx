import React, { useEffect, useState } from "react";
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
  const { getOutboundShipments, getOutBoundDetail } = useWms2Data();
  
  const [loading, setLoading] = useState(false);
  const [searching, setSearching] = useState(false);

  // Fetch shipments when component mounts
  useEffect(() => {
    const fetchShipments = async () => {
      setLoading(true);
      try {
        const response = await getOutboundShipments();
        if (response && response.code === 200) {
          setEntities(prev => ({
            ...prev,
            shipments: response.data || []
          }));
        }
      } catch (error) {
        console.error("Error fetching shipments:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchShipments();
  }, [getOutboundShipments, setEntities]);

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
      case "APPROVED":
      case "CONFIRMED":
        return "primary";
      case "READY":
      case "READY_FOR_DELIVERY":
        return "success";
      case "PENDING":
        return "warning";
      case "CANCELLED":
      case "REJECTED":
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
              `${option.shipmentName || option.id} - ${option.customerName || 'Không có khách hàng'}`
            }
            loading={loading}
            value={selectedShipment || null}
            onChange={handleShipmentChange}
            renderInput={(params) => (
              <TextField
                {...params}
                label="Tìm kiếm lô hàng"
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
                      {(loading || searching) ? <CircularProgress color="inherit" size={20} /> : null}
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
                      {option.customerName || 'Không có khách hàng'}
                    </Typography>
                    <Chip 
                      label={option.status} 
                      size="small" 
                      color={getStatusColor(option.status)}
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
                label={selectedShipment.status} 
                size="small" 
                color={getStatusColor(selectedShipment.status)}
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
            Vui lòng chọn lô hàng để tạo phiếu giao
          </Alert>
        )}
      </CardContent>
    </Card>
  );
};

export default ShipmentSelector;