import React, { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Alert,
  Grid,
  Typography,
  Box,
  IconButton,
  Divider,
} from "@mui/material";
import { Warehouse as WarehouseIcon, Close as CloseIcon } from "@mui/icons-material";
import InBoundFormFields from "./inbound/InBoundFormFields";
import InBoundProductTable from "./inbound/InBoundProductTable";
import { useOrderDetail } from "views/wms/common/context/OrderDetailContext";
import { useApprovedOrderDetail } from "../context/OrderDetailContext";

const CreateInBoundDialog = ({ open, onClose }) => {
  const { getMoreInventoryItemsApi, createInBoundOrderApi } = useApprovedOrderDetail();
  const { orderData } = useOrderDetail();
  const [formData, setFormData] = useState({
    id: "",
    orderId: "",
    note: "",
    shipmentName: "",
    expectedDeliveryDate: null,
    products: [],
  });
  const [inventoryItems, setInventoryItems] = useState([]); // Changed structure
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const fetchInventoryItems = async () => {
    try {
      const response = await getMoreInventoryItemsApi(0, 1000, orderData.id);
      debugger;  
      // Transform new API response to flat structure for easier processing
      const transformedData = [];
      if (response&& Array.isArray(response.data)) {
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
      setErrors({ global: "Không thể tải danh sách kho hàng" });
    }
  };

  useEffect(() => {
    if (open && orderData) {
      // Initialize products with all required fields matching CreateInBoundProductReq
      const initialProducts = orderData.orderItems.map((item) => ({
        productId: item.productId,
        facilityId: "", // Changed from inventoryItemId
        quantity: item.quantity,
        orderItemId: item.id, // Changed from orderItemSeqId
        // Optional fields for lot management
        lotId: "",
        manufacturingDate: "",
        expirationDate: "",
      }));

      setFormData({
        id: "",
        orderId: orderData.id,
        note: "",
        shipmentName: `Phiếu nhập ${orderData.orderName || orderData.id}`,
        expectedDeliveryDate: orderData.deliveryAfterDate || null,
        products: initialProducts,
      });
      setErrors({});
      fetchInventoryItems();
    }
  }, [open, orderData]);

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

    // Validate basic fields according to CreateInBoundReq
    if (!formData.orderId) newErrors.orderId = "Order ID is required";
    if (!formData.shipmentName?.trim()) newErrors.shipmentName = "Tên phiếu nhập là bắt buộc";
    if (!formData.expectedDeliveryDate) newErrors.expectedDeliveryDate = "Ngày dự kiến giao hàng là bắt buộc";

    // Validate products
    if (formData.products.length === 0) {
      newErrors.global = "Phải có ít nhất một sản phẩm trong phiếu nhập";
      return newErrors;
    }

    formData.products.forEach((product) => {
      const itemId = product.orderItemId;
      
      // Required validations according to CreateInBoundProductReq
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

      // Date validations (optional fields)
      if (product.manufacturingDate && product.expirationDate) {
        const mfgDate = new Date(product.manufacturingDate);
        const expDate = new Date(product.expirationDate);
        if (mfgDate > expDate) {
          newErrors[`date_${itemId}`] = "Ngày sản xuất phải trước ngày hết hạn";
        }
      }

      // Manufacturing date should not be in the future
      if (product.manufacturingDate) {
        const mfgDate = new Date(product.manufacturingDate);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (mfgDate > today) {
          newErrors[`date_${itemId}`] = "Ngày sản xuất không được ở tương lai";
        }
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
      // Transform data to match CreateInBoundReq exactly
      const submitData = {
        id: formData.id || undefined, // Optional
        orderId: formData.orderId, // Required @NotBlank
        note: formData.note || undefined, // Optional
        shipmentName: formData.shipmentName, // Optional but used
        expectedDeliveryDate: formData.expectedDeliveryDate, // Required @NotNull
        products: formData.products.map(product => ({
          productId: product.productId, // Required @NotBlank
          facilityId: product.facilityId, // Required @NotBlank
          orderItemId: product.orderItemId, // Required @NotBlank
          quantity: parseInt(product.quantity, 10), // Required @NotNull
          // Optional fields - only include if they have values
          ...(product.lotId && { lotId: product.lotId }),
          ...(product.manufacturingDate && { manufacturingDate: product.manufacturingDate }),
          ...(product.expirationDate && { expirationDate: product.expirationDate }),
        }))
      };

      console.log('Submitting data:', submitData); // Debug log
      await createInBoundOrderApi(submitData);
      onClose(true);
    } catch (error) {
      console.error('Submit error:', error); // Debug log
      setErrors({ global: error.message || "Không thể tạo phiếu nhập" });
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
          <Typography variant="h6">Tạo phiếu nhập kho</Typography>
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
            <InBoundFormFields
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
            <InBoundProductTable
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
            {loading ? "Đang tạo..." : "Tạo phiếu nhập"}
          </Button>
        </Box>
      </DialogActions>
    </Dialog>
  );
};

export default CreateInBoundDialog;