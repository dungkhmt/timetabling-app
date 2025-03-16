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
import CloseIcon from "@mui/icons-material/Close";
import WarehouseIcon from "@mui/icons-material/Warehouse";
import { useApprovedOrderDetail } from "../context/OrderDetailContext";
import OutboundFormFields from "./outbound/OutboundFormFields";
import OutboundProductTable from "./outbound/OutboundProductTable";

const CreateOutboundDialog = ({ open, onClose }) => {
  const { getMoreInventoryItemsApi, createOutBoundOrderApi, orderData } = useApprovedOrderDetail();
  const [inventoryItems, setInventoryItems] = useState([]);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState({
    id: "",
    orderId: "",
    note: "",
    products: [],
  });

  console.log("orderData", orderData);

  const fetchInventoryItems = useCallback(async () => {
    if (!orderData?.id) return;
    try {
      const response = await getMoreInventoryItemsApi(0, 10, orderData.id);
      setInventoryItems(response?.data || []);
    } catch (error) {
      console.error("Error fetching inventory items:", error);
      setErrors((prev) => ({
        ...prev,
        inventory: "Không thể tải danh sách tồn kho",
      }));
    }
  }, [getMoreInventoryItemsApi, orderData?.id]);

  useEffect(() => {
    if (open && orderData) {
      const initialProducts = orderData.orderItems.map((item) => ({
        productId: item.productId,
        inventoryItemId: "", // Kho mặc định là rỗng
        quantity: item.quantity, // Số lượng ban đầu từ order
        orderId: orderData.id,
        orderItemSeqId: item.orderItemSeqId,
      }));
      setFormData({
        id: "",
        orderId: orderData.id,
        note: "",
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

  const handleProductChange = (orderItemSeqId, field, value) => {
    setFormData((prev) => {
      const updatedProducts = prev.products.map((product) =>
        product.orderItemSeqId === orderItemSeqId
          ? { ...product, [field]: value }
          : product
      );
      return { ...prev, products: updatedProducts };
    });
    setErrors((prev) => ({
      ...prev,
      [`${field}_${orderItemSeqId}`]: undefined,
    }));
  };

  const handleSubmit = async () => {
    const newErrors = {};
    if (!formData.orderId) newErrors.orderId = "Order ID is required";
    formData.products.forEach((product) => {
      if (!product.inventoryItemId) {
        newErrors[`warehouse_${product.orderItemSeqId}`] = "Vui lòng chọn kho";
      }
      if (!product.quantity || product.quantity <= 0) {
        newErrors[`quantity_${product.orderItemSeqId}`] = "Số lượng phải lớn hơn 0";
      }
    });

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    try {
      await createOutBoundOrderApi(formData, orderData.createdByUser);
      onClose(true);
    } catch (error) {
      setErrors({ global: error.message || "Không thể tạo phiếu xuất" });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog
      open={open}
      onClose={() => onClose(false)}
      fullWidth
      maxWidth="md"
      PaperProps={{ sx: { borderRadius: 2 } }}
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
        <Grid container spacing={2}>
          <OutboundFormFields
            formData={formData}
            errors={errors}
            onChange={handleFormChange}
          />
          <Grid item xs={12}>
            <OutboundProductTable
              orderItems={orderData?.orderItems || []}
              inventoryItems={inventoryItems}
              products={formData.products}
              onProductChange={handleProductChange}
              errors={errors}
            />
          </Grid>
        </Grid>
      </DialogContent>

      <Divider />

      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={() => onClose(false)} disabled={loading}>
          Hủy
        </Button>
        <Button variant="contained" onClick={handleSubmit} disabled={loading}>
          {loading ? "Đang tạo..." : "Tạo phiếu xuất"}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default CreateOutboundDialog;