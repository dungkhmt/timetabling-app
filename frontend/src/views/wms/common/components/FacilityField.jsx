import React, { useEffect } from 'react';
import { Grid, Typography } from "@mui/material";
import RequireField from "./RequireField";
import EntityAutocomplete from "./EntityAutocomplete";
import { useEntityData } from "../hooks/useEntityData";
import { useOrderForm } from "../context/OrderFormContext";

const FacilityField = () => {
  const { order, setOrder, entities, setEntities } = useOrderForm();
  
  // Use the custom hook for facility data
  const { loading, handleScroll, handleDropdownOpen } = useEntityData('facilities', (newData) => {
    // Process new data
    setEntities(prev => {
      const existingData = prev.facilities || [];
      const existingIds = new Set(existingData.map(item => item.id));
      const uniqueNewItems = newData.filter(item => !existingIds.has(item.id));
      
      return {
        ...prev,
        facilities: [...existingData, ...uniqueNewItems]
      };
    });
  });

  // Set first facility as default when data is first loaded
  useEffect(() => {
    if (entities.facilities.length > 0 && !order.facilityId) {
      setOrder(prev => ({ ...prev, facilityId: entities.facilities[0].id }));
    }
  }, [entities.facilities, order.facilityId, setOrder]);

  return (
    <>
      <Grid item xs={4}>
        <Typography variant="body1" sx={{ pt: 1 }}>
          Kho hàng: <RequireField />
        </Typography>
      </Grid>
      <Grid item xs={8}>
        <EntityAutocomplete
          options={entities.facilities}
          getOptionLabel={(option) => `${option.id} - ${option.name || ''}`}
          value={entities.facilities.find(f => f.id === order.facilityId) || null}
          onChange={(_, value) => {
            if (value) {
              setOrder(prev => ({ ...prev, facilityId: value.id }));
            }
          }}
          onOpen={handleDropdownOpen}
          onScroll={handleScroll}
          loading={loading}
          placeholder="Chọn kho hàng"
        />
      </Grid>
    </>
  );
};

export default FacilityField;