import React, { useState } from "react";
import {
  Box,
  Button,
  Grid,
  Typography,
} from "@mui/material";
import { toast } from "react-toastify";
import { useWms2Data } from "services/useWms2Data";
import { OrderFormProvider } from "./context/OrderFormContext";
import BasicInfoForm from "./components/BasicInfoForm";
import DeliveryInfoForm from "./components/DeliveryInfoForm";
import ProductSearch from "./components/ProductSearch";
import ProductTable from "./components/ProductTable";

const CreateSaleOrder = () => {
  const { createSalesOrder } = useWms2Data();
  
  // State for form data
  const [salesOrder, setSalesOrder] = useState({
    id: null,
    facilityId: "",
    customerId: "",
    userCreatedId: "wms_director",
    deliveryDate: new Date().toISOString().split('T')[0],
    invoiceNumber: null,
    discountType: "",
    discountValue: 0,
    priceList: "",
    deliveryAddress: "",
    deliveryPhone: "",
    shippingMethod: "",
    shippingCarrier: "",
    orderPurpose: "SALES",
    notes: "",
    requireVatInvoice: false,
    orderItems: []
  });

  // State for lookup data
  const [entities, setEntities] = useState({
    facilities: [],
    customers: [],
    products: []
  });

  const handleSubmit = () => {
    // Validate required fields
    if (!salesOrder.facilityId) {
      toast.warning("Vui lòng chọn kho hàng");
      return;
    }
    if (!salesOrder.customerId) {
      toast.warning("Vui lòng chọn khách hàng");
      return;
    }
    if (!salesOrder.deliveryDate) {
      toast.warning("Vui lòng chọn ngày giao hàng");
      return;
    }
    if (salesOrder.orderItems.length === 0) {
      toast.warning("Vui lòng thêm sản phẩm vào đơn hàng");
      return;
    }

    createSalesOrder(salesOrder);
  };

  return (
    <OrderFormProvider value={{ salesOrder, setSalesOrder, entities, setEntities }}>
      <Box p={3}>
        <Typography sx={{fontWeight: 700}} variant="h5" gutterBottom>
          Tạo đơn hàng bán
        </Typography>

        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <BasicInfoForm />
          </Grid>

          <Grid item xs={12} md={6}>
            <DeliveryInfoForm />
          </Grid>
        </Grid>

        <Box mt={3}>
          <ProductSearch />
          <ProductTable />
        </Box>

        <Box mt={3} textAlign="right">
          <Button 
            variant="contained" 
            color="primary" 
            onClick={handleSubmit}
          >
            {"Lưu"}
          </Button>
        </Box>
      </Box>
    </OrderFormProvider>
  );
};

export default CreateSaleOrder;