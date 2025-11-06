import React, { useState } from "react";
import {
  Box,
  Button,
  Grid,
  Typography,
} from "@mui/material";
import { toast } from "react-toastify";
import { useHistory } from "react-router-dom";
import { useWms2Data } from "services/useWms2Data";
import { DeliveryBillFormProvider } from "../common/context/DeliveryBillFormContext";
import FacilitySelector from "./components/FacilitySelector";
import ShipmentSelector from "./components/ShipmentSelector";
import DeliveryBillInfoForm from "./components/DeliveryBillInfoForm";
import ShipmentProductTable from "./components/ShipmentProductTable";
import { useHandleNavigate } from "../common/utils/functions";

const CreateDeliveryBill = () => {
  const { createDeliveryBill } = useWms2Data();
  const navigate = useHandleNavigate();
  
  // State for form data
  const [deliveryBill, setDeliveryBill] = useState({
    facilityId: "",
    shipmentId: "",
    deliveryBillName: "",
    deliveryAddressId: "",
    priority: 1,
    note: "",
    expectedDeliveryDate: new Date().toISOString().split('T')[0],
    products: []
  });

  // State for lookup data
  const [entities, setEntities] = useState({
    facilities: [],
    selectedFacility: null,
    shipments: [],
    selectedShipment: null
  });

  const handleSubmit = () => {
    // Validate required fields
    if (!deliveryBill.facilityId) {
      toast.warning("Vui lòng chọn kho hàng");
      return;
    }
    if (!deliveryBill.shipmentId) {
      toast.warning("Vui lòng chọn phiếu xuất");
      return;
    }
    if (!deliveryBill.deliveryBillName) {
      toast.warning("Vui lòng nhập tên phiếu giao hàng");
      return;
    }
    if (!deliveryBill.expectedDeliveryDate) {
      toast.warning("Vui lòng chọn ngày giao hàng dự kiến");
      return;
    }
    
    const selectedProducts = deliveryBill.products.filter(p => p.selected && p.quantity > 0);
    if (selectedProducts.length === 0) {
      toast.warning("Vui lòng chọn ít nhất một sản phẩm");
      return;
    }

    // Create payload according to CreateDeliveryBill DTO
    const payload = {
      facilityId: deliveryBill.facilityId,
      shipmentId: deliveryBill.shipmentId,
      deliveryBillName: deliveryBill.deliveryBillName,
      deliveryAddressId: deliveryBill.deliveryAddressId,
      priority: deliveryBill.priority,
      note: deliveryBill.note,
      expectedDeliveryDate: deliveryBill.expectedDeliveryDate,
      products: selectedProducts.map(product => ({
        productId: product.productId,
        quantity: product.quantity,
        price: product.price || 0,
        weight: product.weight || 0,
        unit: product.unit || ""
      }))
    };

    // Submit form
    createDeliveryBill(payload)
      .then(response => {
        if (response && (response.code === 200 || response.code === 201)) {
          toast.success("Tạo phiếu giao hàng thành công");
          navigate(`/wms/logistics/deliverybill`);
        } else {
          toast.error("Lỗi khi tạo phiếu giao hàng: " + (response?.message || "Lỗi không xác định"));
        }
      })
      .catch(error => {
        console.error("Error creating delivery bill:", error);
        toast.error("Lỗi khi tạo phiếu giao hàng: " + (error.message || "Lỗi không xác định"));
      });
  };

  return (
    <DeliveryBillFormProvider value={{ deliveryBill, setDeliveryBill, entities, setEntities }}>
      <Box p={3}>
        <Typography sx={{fontWeight: 700}} variant="h5" gutterBottom>
          Tạo phiếu giao hàng
        </Typography>

        <Grid container spacing={3}>
          <Grid item xs={12} md={4}>
            <FacilitySelector />
          </Grid>

          <Grid item xs={12} md={4}>
            <ShipmentSelector />
          </Grid>

          <Grid item xs={12} md={4}>
            <DeliveryBillInfoForm />
          </Grid>
        </Grid>

        <Box mt={3}>
          <ShipmentProductTable />
        </Box>

        <Box mt={3} display="flex" justifyContent="flex-end">
          <Button 
            variant="outlined" 
            color="primary" 
            onClick={() => navigate("/wms/shipments")}
            sx={{ mr: 2 }}
          >
            Hủy
          </Button>
          <Button 
            variant="contained" 
            color="primary" 
            onClick={handleSubmit}
          >
            Lưu
          </Button>
        </Box>
      </Box>
    </DeliveryBillFormProvider>
  );
};

export default CreateDeliveryBill;