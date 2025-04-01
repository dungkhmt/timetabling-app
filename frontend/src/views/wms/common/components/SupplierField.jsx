import React, { useEffect } from 'react';
import { Grid, Typography } from "@mui/material";
import RequireField from "./RequireField";
import EntityAutocomplete from "./EntityAutocomplete";
import { useEntityData } from "../hooks/useEntityData";
import { useOrderForm } from "../context/OrderFormContext";
import {entityTypes} from "../constants/constants";

const SupplierField = () => {
    const { order, setOrder, entities, setEntities } = useOrderForm();

    // Use the custom hook for facility data
    const { loading, handleScroll, handleDropdownOpen } = useEntityData(entityTypes.SUPPLIERS, (newData) => {
        // Process new data
        setEntities(prev => {
            const existingData = prev.suppliers || [];
            const existingIds = new Set(existingData.map(item => item.id));
            const uniqueNewItems = newData.filter(item => !existingIds.has(item.id));

            return {
                ...prev,
                suppliers: [...existingData, ...uniqueNewItems]
            };
        });
    });

    // Set first facility as default when data is first loaded
    useEffect(() => {
        if (entities.suppliers.length > 0 && !order.supplierId) {
            setOrder(prev => ({ ...prev, supplierId: entities.suppliers[0].id }));
        }
    }, [entities.suppliers, order.supplierId, setOrder]);

    return (
        <>
            <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                    Nhà cung cấp: <RequireField />
                </Typography>
            </Grid>
            <Grid item xs={8}>
                <EntityAutocomplete
                    options={entities.suppliers}
                    getOptionLabel={(option) => `${option.id} - ${option.name || ''}`}
                    value={entities.suppliers.find(f => f.id === order.supplierId) || null}
                    onChange={(_, value) => {
                        if (value) {
                            setOrder(prev => ({ ...prev, supplierId: value.id }));
                        }
                    }}
                    onOpen={handleDropdownOpen}
                    onScroll={handleScroll}
                    loading={loading}
                    placeholder="Chọn nhà cung cấp"
                />
            </Grid>
        </>
    );
};

export default SupplierField;