import React, { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Grid,
  Box,
  IconButton,
  LinearProgress,
  Divider,
  Alert
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import WarehouseIcon from "@mui/icons-material/Warehouse";
import { useApprovedOrderDetail } from "../context/OrderDetailContext";
import OutboundFormFields from "./outbound/OutboundFormFields";
import OutboundProductTable from "./outbound/OutboundProductTable";

const CreateOutboundDialog = ({ open, onClose, orderData }) => {
  const { getMoreInventoryItemsApi, createOutBoundOrderApi } = useApprovedOrderDetail();
  const [facilities, setFacilities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [facilitiesLoaded, setFacilitiesLoaded] = useState(false);
  const [selectedItems, setSelectedItems] = useState({});
  
  // Form data state
  const [formData, setFormData] = useState({
    warehouseId: "",
    shipDate: new Date(),
    note: "",
    items: []
  });

  const [errors, setErrors] = useState({});

  // Fetch warehouses when dialog opens
  const fetchMoreInventoryItems = async () => {
    if (facilitiesLoaded) return;
    
    try {
      const response = await getMoreInventoryItemsApi(0, 100);
      setFacilities(response?.data || []);
      setFacilitiesLoaded(true);
    } catch (error) {
      console.error("Error fetching facilities:", error);
      setErrors(prev => ({ ...prev, facilities: "Không thể tải danh sách kho hàng" }));
    }
  };

  // Initialize order items when dialog opens or orderData changes
  useEffect(() => {
    if (open) {
      // Load facilities when dialog opens
      fetchMoreInventoryItems();
      
      // Reset form when dialog opens
      setFormData({
        warehouseId: "",
        shipDate: new Date(),
        note: "",
        items: []
      });
      
      // Initialize selected items
      const initialSelectedState = {};
      
      // Process order items if available
      if (orderData && orderData.orderItems) {
        // Convert to items format needed for the form
        const processedItems = orderData.orderItems.map(item => {
          // Default: select all items
          initialSelectedState[item.orderItemSeqId] = true;
          
          return {
            orderItemSeqId: item.orderItemSeqId,
            productId: item.productId,
            productName: item.productName || item.product?.name || "Không xác định",
            orderedQuantity: item.quantity,
            outboundQuantity: item.quantity, // Default to full quantity
            remainingQuantity: 0,
            selected: true
          };
        });
        
        setFormData(prev => ({
          ...prev,
          items: processedItems
        }));
        
        setSelectedItems(initialSelectedState);
      }
    }
  }, [open, orderData]);

  // Handle form field changes
  const handleFormChange = (name, value) => {
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error for this field if any
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: undefined }));
    }
  };

  // Handle table actions (selection and quantity changes)
  const handleTableActions = {
    setSelectedItems,
    updateFormItems: (updatedItems) => {
      setFormData(prev => ({
        ...prev,
        items: updatedItems
      }));
    },
  };

  // Check if any item is selected
  const hasSelectedItems = () => {
    return Object.values(selectedItems).some(value => value);
  };

  // Validation function
  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.warehouseId) {
      newErrors.warehouseId = "Vui lòng chọn kho xuất hàng";
    }
    
    if (!formData.shipDate) {
      newErrors.shipDate = "Vui lòng chọn ngày xuất kho";
    }
    
    if (!hasSelectedItems()) {
      newErrors.selection = "Vui lòng chọn ít nhất một sản phẩm để xuất";
    }
    
    // Check if any selected item has invalid quantity
    const hasInvalidItems = formData.items
      .filter(item => selectedItems[item.orderItemSeqId])
      .some(item => item.outboundQuantity <= 0 || item.outboundQuantity > item.orderedQuantity);
    
    if (hasInvalidItems) {
      newErrors.items = "Số lượng xuất kho không hợp lệ";
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Submit handler
  const handleSubmit = async () => {
    if (!validateForm()) return;
    
    setLoading(true);
    try {
      const outboundData = {
        orderId: orderData.id,
        warehouseId: formData.warehouseId,
        shipDate: formData.shipDate.toISOString().split('T')[0],
        note: formData.note,
        items: formData.items
          .filter(item => selectedItems[item.orderItemSeqId] && item.outboundQuantity > 0)
          .map(item => ({
            orderItemSeqId: item.orderItemSeqId,
            productId: item.productId,
            quantity: item.outboundQuantity
          }))
      };
      
      await createOutBoundOrderApi(outboundData);
      onClose(true); // close with refresh flag
    } catch (err) {
      console.error("Error creating outbound:", err);
      setErrors(prev => ({ 
        ...prev, 
        global: "Không thể tạo phiếu xuất kho: " + (err.message || "Lỗi không xác định")
      }));
    } finally {
      setLoading(false);
    }
  };

  // Count selected items
  const selectedItemsCount = Object.values(selectedItems).filter(Boolean).length;

  return (
    <Dialog
      open={open}
      onClose={() => onClose(false)}
      fullWidth
      maxWidth="md"
      PaperProps={{ sx: { borderRadius: 2 } }}
    >
      <DialogTitle sx={{ px: 3, py: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box display="flex" alignItems="center">
          <WarehouseIcon sx={{ mr: 1, color: 'primary.main' }} />
          <Typography variant="h6">Tạo phiếu xuất kho</Typography>
        </Box>
        <IconButton onClick={() => onClose(false)} size="small">
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      
      {loading && <LinearProgress />}
      
      <DialogContent sx={{ px: 3, py: 2 }}>
        {errors.global && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {errors.global}
          </Alert>
        )}
        
        <Grid container spacing={2}>
          {/* Form Fields Component */}
          <OutboundFormFields 
            formData={formData}
            facilities={facilities}
            errors={errors}
            onChange={handleFormChange}
            onFetchFacilities={fetchMoreInventoryItems}
          />
          
          {/* Product Table Component */}
          <Grid item xs={12}>
            <OutboundProductTable 
              items={formData.items}
              selectedItems={selectedItems}
              selectedItemsCount={selectedItemsCount}
              errors={{
                selection: errors.selection,
                items: errors.items
              }}
              onActions={handleTableActions}
            />
          </Grid>
        </Grid>
      </DialogContent>
      
      <Divider />
      
      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={() => onClose(false)} disabled={loading}>
          Hủy
        </Button>
        <Button 
          onClick={handleSubmit} 
          variant="contained" 
          disabled={loading || !hasSelectedItems()}
        >
          Tạo phiếu xuất
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default CreateOutboundDialog;