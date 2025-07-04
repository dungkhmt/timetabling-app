import React from 'react';
import { Grid, Typography } from "@mui/material";
import RequireField from "./RequireField";
import EntityAutocomplete from "./EntityAutocomplete";
import { useEntityData } from "../hooks/useEntityData";
import { useOrderForm } from "../context/OrderFormContext";

const CustomerField = () => {
  const { order, setOrder, entities, setEntities } = useOrderForm();
  
  // Use the custom hook for customer data
  const { loading, handleScroll, handleDropdownOpen } = useEntityData('customers', (newData) => {
    // Process new data
    setEntities(prev => {
      const existingData = prev.customers || [];
      const existingIds = new Set(existingData.map(item => item.id));
      const uniqueNewItems = newData.filter(item => !existingIds.has(item.id));
      
      return {
        ...prev,
        customers: [...existingData, ...uniqueNewItems]
      };
    });
  });

  const handleCustomerSelect = (_, customer) => {
    if (customer) {
      setOrder(prev => ({ 
        ...prev, 
        toCustomerId: customer.id,
        deliveryAddressId: customer.currentAddressId,
        deliveryPhone: customer.phone,
        deliveryFullAddress: customer.fullAddress
      }));
    }
  };

  return (
    <>
      <Grid item xs={4}>
        <Typography variant="body1" sx={{ pt: 1 }}>
          Khách hàng: <RequireField />
        </Typography>
      </Grid>
      <Grid item xs={8}>
        <EntityAutocomplete
          options={entities.customers}
          getOptionLabel={(option) => `${option.id} - ${option.name || ''}`}
          value={entities.customers.find(c => c.id === order.toCustomerId) || null}
          onChange={handleCustomerSelect}
          onOpen={handleDropdownOpen}
          onScroll={handleScroll}
          loading={loading}
          placeholder="Tìm kiếm khách hàng"
        />
      </Grid>
    </>
  );
};

export default CustomerField;