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
import OrderSummary from "./components/OrderSummary";

const CreateSaleOrder = () => {
  const { createSalesOrder } = useWms2Data();
  
  // State for form data matching CreateSaleOrderReq
  const [order, setOrder] = useState({
    id: null,
    toCustomerId: "",
    orderName: "",
    orderDate : null,
    deliveryBeforeDate: null,
    deliveryAfterDate: null,
    deliveryAddressId: "",
    deliveryFullAddress: "",
    deliveryPhone: "",
    note: "",
    saleChannelId: "",
    discount: 0, // This will always be a specific amount (VND), never percentage
    orderItems: []
  });

  // State for lookup data
  const [entities, setEntities] = useState({
    customers: [],
    products: [],
    saleChannels: [],
    addresses: []
  });

  // Calculate subtotal of all items (price * quantity, before any discounts)
  const calculateItemsSubtotal = () => {
    return order.orderItems.reduce((total, item) => {
      return total + (item.price * item.quantity);
    }, 0);
  };

  // Calculate total of all items after applying individual item discounts
  const calculateItemsTotal = () => {
    return order.orderItems.reduce((total, item) => {
      const itemSubtotal = item.price * item.quantity;
      const itemDiscountAmount = item.discount || 0; // Always a specific amount
      const itemFinalAmount = itemSubtotal - itemDiscountAmount;
      return total + Math.max(itemFinalAmount, 0); // Ensure item total is not negative
    }, 0);
  };

  // Calculate final order total after applying order-level discount
  const calculateOrderTotal = () => {
    const itemsTotal = calculateItemsTotal();
    const orderDiscountAmount = order.discount || 0; // Always a specific amount
    const finalTotal = itemsTotal - orderDiscountAmount;
    
    return Math.max(finalTotal, 0); // Ensure total is not negative
  };

  // Add product to order items
  const addProductToOrder = (product) => {
    const existingItemIndex = order.orderItems.findIndex(item => item.productId === product.id);
    
    if (existingItemIndex >= 0) {
      // Increase quantity if product already exists
      const updatedItems = [...order.orderItems];
      updatedItems[existingItemIndex].quantity += 1;
      setOrder(prev => ({ ...prev, orderItems: updatedItems }));
    } else {
      // Add new product with default values from product
      const newItem = {
        productId: product.id,
        quantity: 1,
        price: product.wholesalePrice || product.retailPrice || 0,
        unit: product.unit || "Cái",
        discount: 0, // Always a specific amount (VND), not percentage
        tax: 0, // Ignored for sale orders
        note: ""
      };
      setOrder(prev => ({ ...prev, orderItems: [...prev.orderItems, newItem] }));
    }
  };

  // Update item quantity
  const updateItemQuantity = (productId, change) => {
    const updatedItems = order.orderItems.map(item => {
      if (item.productId === productId) {
        const newQuantity = Math.max(1, item.quantity + change);
        return { ...item, quantity: newQuantity };
      }
      return item;
    }).filter(item => item.quantity > 0);
    
    setOrder(prev => ({ ...prev, orderItems: updatedItems }));
  };

  // Update item discount - always store as specific amount
  const updateItemDiscount = (productId, discountAmount) => {
    const updatedItems = order.orderItems.map(item => {
      if (item.productId === productId) {
        // Store as specific amount, ensure not negative
        return { ...item, discount: Math.max(0, discountAmount) };
      }
      return item;
    });
    
    setOrder(prev => ({ ...prev, orderItems: updatedItems }));
  };

  // Update item price
  const updateItemPrice = (productId, price) => {
    const updatedItems = order.orderItems.map(item => {
      if (item.productId === productId) {
        return { ...item, price: Math.max(0, price) };
      }
      return item;
    });
    
    setOrder(prev => ({ ...prev, orderItems: updatedItems }));
  };

  // Remove item from order
  const removeItemFromOrder = (productId) => {
    const updatedItems = order.orderItems.filter(item => item.productId !== productId);
    setOrder(prev => ({ ...prev, orderItems: updatedItems }));
  };

  const handleSubmit = () => {
    // Validate required fields according to CreateSaleOrderReq
    if (!order.toCustomerId) {
      toast.warning("Vui lòng chọn khách hàng");
      return;
    }
    if (!order.saleChannelId) {
      toast.warning("Vui lòng chọn kênh bán hàng");
      return;
    }
    if (order.orderItems.length === 0) {
      toast.warning("Vui lòng thêm sản phẩm vào đơn hàng");
      return;
    }

    // Prepare data for API call
    const orderData = {
      ...order,
      discount: order.discount, // Send as specific amount (BigDecimal in backend)
      orderItems: order.orderItems.map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        price: item.price, // BigDecimal
        unit: item.unit,
        discount: item.discount, // Send as specific amount (BigDecimal in backend)
        tax: 0, // Always 0 for sale orders (BigDecimal)
        note: item.note
      }))
    };

    console.log('Order data to send:', orderData); // For debugging
    createSalesOrder(orderData);
  };

  const orderContextValue = {
    order,
    setOrder,
    entities,
    setEntities,
    addProductToOrder,
    updateItemQuantity,
    updateItemDiscount,
    updateItemPrice,
    removeItemFromOrder,
    calculateOrderTotal,
    calculateItemsTotal,
    calculateItemsSubtotal
  };

  return (
    <OrderFormProvider value={orderContextValue}>
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

        {/* Use the new OrderSummary component */}
        <OrderSummary />

        <Box mt={3} textAlign="right">
          <Button 
            variant="contained" 
            color="primary" 
            onClick={handleSubmit}
            disabled={order.orderItems.length === 0}
            size="large"
            sx={{ px: 4, py: 1.5 }}
          >
            Lưu đơn hàng
          </Button>
        </Box>
      </Box>
    </OrderFormProvider>
  );
};

export default CreateSaleOrder;