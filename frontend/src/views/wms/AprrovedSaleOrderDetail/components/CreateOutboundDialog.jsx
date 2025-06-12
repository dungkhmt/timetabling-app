import React, { useState, useEffect, useCallback } from "react";
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
  Divider,
  Alert,
} from "@mui/material";
import { Warehouse as WarehouseIcon, Close as CloseIcon } from "@mui/icons-material";
import { useApprovedOrderDetail } from "../context/OrderDetailContext";
import OutboundFormFields from "./outbound/OutboundFormFields";
import OutboundProductTable from "./outbound/OutboundProductTable";
import { useOrderDetail } from "views/wms/common/context/OrderDetailContext";

const CreateOutboundDialog = ({ open, onClose }) => {
  const {getMoreInventoryItemsForOutboundApi , createOutBoundOrderApi } = useApprovedOrderDetail();
  const { orderData } = useOrderDetail();
  const [inventoryItems, setInventoryItems] = useState([]);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState({
    id: "",
    orderId: "",
    note: "",
    shipmentName: "",
    expectedDeliveryDate: null,
    products: [],
  });

  const fetchInventoryItems = useCallback(async () => {
    if (!orderData?.id) return;
    try {
      const response = await getMoreInventoryItemsForOutboundApi(0, 1000, orderData.id);
      
      // Transform new API response to flat structure for easier processing
      const transformedData = [];
      if (response && Array.isArray(response.data)) {
        response.data.forEach(item => {
          if (item.facilityForOrderRes && Array.isArray(item.facilityForOrderRes)) {
            item.facilityForOrderRes.forEach(facility => {
              transformedData.push({
                productId: item.productId,
                facilityId: facility.facilityId,
                facilityName: facility.facilityName,
                quantity: facility.quantity
              });
            });
          }
        });
      }
      
      setInventoryItems(transformedData);
    } catch (error) {
      console.error("Error fetching inventory items:", error);
      setErrors((prev) => ({
        ...prev,
        global: "Không thể tải danh sách tồn kho",
      }));
    }
  }, [orderData?.id]);

  useEffect(() => {
    if (open && orderData) {
      // Initialize products with all required fields matching CreateOutBoundProductReq
      const initialProducts = orderData.orderItems.map((item) => ({
        productId: item.productId,
        facilityId: "", // Changed from inventoryItemId
        quantity: item.quantity,
        orderItemId: item.id, // Changed from orderItemSeqId
      }));

      setFormData({
        id: "",
        orderId: orderData.id,
        note: "",
        shipmentName: `Phiếu xuất ${orderData.orderName || orderData.id}`,
        expectedDeliveryDate: orderData.deliveryAfterDate || null,
        products: initialProducts,
      });
      setErrors({});
      fetchInventoryItems();
    }
  }, [open, orderData, fetchInventoryItems]);

  const handleFormChange = (name, value) => {
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  const handleProductChange = (orderItemId, field, value) => {
    setFormData((prev) => {
      const updatedProducts = prev.products.map((product) =>
        product.orderItemId === orderItemId
          ? { ...product, [field]: value }
          : product
      );
      return { ...prev, products: updatedProducts };
    });
    setErrors((prev) => ({
      ...prev,
      [`${field}_${orderItemId}`]: undefined,
    }));
  };

  const handleDeleteProduct = (orderItemId) => {
    setFormData((prev) => ({
      ...prev,
      products: prev.products.filter((p) => p.orderItemId !== orderItemId),
    }));
    // Clear related errors
    setErrors((prev) => {
      const newErrors = { ...prev };
      Object.keys(newErrors).forEach((key) => {
        if (key.endsWith(`_${orderItemId}`)) {
          delete newErrors[key];
        }
      });
      return newErrors;
    });
  };

  const validateForm = () => {
    const newErrors = {};

    // Validate basic fields according to CreateOutBoundReq
    if (!formData.orderId) newErrors.orderId = "Order ID is required";
    if (!formData.shipmentName?.trim()) newErrors.shipmentName = "Tên phiếu xuất là bắt buộc";

    // Validate products
    if (formData.products.length === 0) {
      newErrors.global = "Phải có ít nhất một sản phẩm trong phiếu xuất";
      return newErrors;
    }

    formData.products.forEach((product) => {
      const itemId = product.orderItemId;
      
      // Required validations according to CreateOutBoundProductReq
      if (!product.productId) {
        newErrors[`product_${itemId}`] = "Product ID is required";
      }
      if (!product.facilityId) {
        newErrors[`warehouse_${itemId}`] = "Vui lòng chọn kho";
      }
      if (!product.orderItemId) {
        newErrors[`orderItem_${itemId}`] = "Order Item ID is required";
      }
      if (!product.quantity || product.quantity <= 0) {
        newErrors[`quantity_${itemId}`] = "Số lượng phải lớn hơn 0";
      }
    });

    return newErrors;
  };

  const handleSubmit = async () => {
    const validationErrors = validateForm();
    
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    try {
      // Transform data to match CreateOutBoundReq exactly
      const submitData = {
        id: formData.id || undefined, // Optional
        orderId: formData.orderId, // Required @NotBlank
        note: formData.note || undefined, // Optional
        shipmentName: formData.shipmentName, // Optional but used
        expectedDeliveryDate: formData.expectedDeliveryDate, // Optional
        products: formData.products.map(product => ({
          productId: product.productId, // Required @NotBlank
          facilityId: product.facilityId, // Required @NotBlank
          orderItemId: product.orderItemId, // Required @NotBlank
          quantity: parseInt(product.quantity, 10), // Required @NotNull
        }))
      };

      console.log('Submitting outbound data:', submitData); // Debug log
      await createOutBoundOrderApi(submitData);
      onClose(true);
    } catch (error) {
      console.error('Submit error:', error); // Debug log
      setErrors({ global: error.message || "Không thể tạo phiếu xuất" });
    } finally {
      setLoading(false);
    }
  };

  const availableOrderItems = orderData?.orderItems?.filter(item => 
    formData.products.some(p => p.orderItemId === item.id)
  ) || [];

  return (
    <Dialog
      open={open}
      onClose={() => onClose(false)}
      fullWidth
      maxWidth="xl"
      PaperProps={{ sx: { borderRadius: 2, maxHeight: '90vh' } }}
    >
      <DialogTitle sx={{ px: 3, py: 2, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <Box display="flex" alignItems="center">
          <WarehouseIcon sx={{ mr: 1, color: "primary.main" }} />
          <Typography variant="h6">Tạo phiếu xuất kho</Typography>
        </Box>
        <IconButton onClick={() => onClose(false)} size="small">
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent sx={{ px: 3, py: 2 }}>
        {errors.global && (
          <Alert severity="error" sx={{ mb: 2 }}>{errors.global}</Alert>
        )}
        
        <Grid container spacing={3}>
          {/* Form fields */}
          <Grid item xs={12}>
            <OutboundFormFields
              formData={formData}
              errors={errors}
              onChange={handleFormChange}
            />
          </Grid>
          
          <Grid item xs={12}>
            <Divider sx={{ my: 1 }} />
          </Grid>
          
          {/* Product table */}
          <Grid item xs={12}>
            <OutboundProductTable
              orderItems={availableOrderItems}
              inventoryItems={inventoryItems}
              products={formData.products}
              onProductChange={handleProductChange}
              onDeleteProduct={handleDeleteProduct}
              errors={errors}
            />
          </Grid>
        </Grid>
      </DialogContent>

      <Divider />

      <DialogActions sx={{ px: 3, py: 2, justifyContent: 'space-between' }}>
        <Typography variant="body2" color="text.secondary">
          Tổng sản phẩm: {formData.products.length}
        </Typography>
        <Box>
          <Button onClick={() => onClose(false)} disabled={loading} sx={{ mr: 1 }}>
            Hủy
          </Button>
          <Button variant="contained" onClick={handleSubmit} disabled={loading}>
            {loading ? "Đang tạo..." : "Tạo phiếu xuất"}
          </Button>
        </Box>
      </DialogActions>
    </Dialog>
  );
};

export default CreateOutboundDialog;