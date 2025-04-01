import React, { useState } from "react";
import {
  Box,
  Button,
  Grid,
  Typography,
} from "@mui/material";
import { toast } from "react-toastify";
import { useWms2Data } from "services/useWms2Data";
import { OrderFormProvider } from "../common/context/OrderFormContext";
import BasicInfoForm from "./components/BasicInfoForm";
import DeliveryInfoForm from "./components/DeliveryInfoForm";
import ProductSearch from "../common/components/ProductSearch";
import ProductTable from "../common/components/ProductTable";

const CreateSaleOrder = () => {
  const { createSalesOrder } = useWms2Data();
  
  // State for form data
  const [order, setOrder] = useState({
    id: null,
    facilityId: "",
    customerId: "",
    userCreatedId: "wms_director",
    deliveryAfterDate: null,
    numberOfInvoices: null,
    discountType: "",
    discountValue: 0,
    priceList: "",
    deliveryAddress: "",
    deliveryPhone: "",
    shippingMethod: "",
    shippingCarrier: "",
    orderPurpose: "SALES",
    note: "",
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
    if (!order.facilityId) {
      toast.warning("Vui lòng chọn kho hàng");
      return;
    }
    if (!order.customerId) {
      toast.warning("Vui lòng chọn khách hàng");
      return;
    }
    if (!order.deliveryAfterDate) {
      toast.warning("Vui lòng chọn ngày giao hàng");
      return;
    }
    if (order.orderItems.length === 0) {
      toast.warning("Vui lòng thêm sản phẩm vào đơn hàng");
      return;
    }

    createSalesOrder(order);
  };

  return (
    <OrderFormProvider value={{ order, setOrder, entities, setEntities }}>
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