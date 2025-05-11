import React, {useContext, useEffect, useState} from 'react';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography
} from '@mui/material';
import {useWms2Data} from "../../../../../services/useWms2Data";
import { useDeliveryPlanForm } from '../../context/DeliveryPlanFormContext';

const VehiclesTab = () => {
  const { deliveryPlan, setDeliveryPlan, entities, setEntities } = useDeliveryPlanForm();
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [filters, setFilters] = useState({
    keyword: "",
    statusId: "AVAILABLE"
  });
  const [error, setError] = useState("");
  const [totalCapacity, setTotalCapacity] = useState(0);

  const {getVehicles} = useWms2Data();

  // Load vehicles from API when component mounts or filters change
  useEffect(() => {
    fetchVehicles();
  }, [page, rowsPerPage, filters]);

  // Calculate total capacity when selected vehicles change
  useEffect(() => {
    let capacity = 0;
    entities.selectedVehicles.forEach(vehicle => {
      capacity += parseFloat(vehicle.capacity || 0);
    });
    setTotalCapacity(capacity);
  }, [entities.selectedVehicles]);

  const fetchVehicles = async () => {
    setLoading(true);
    try {
      const response = await getVehicles(page, rowsPerPage, filters);
      if (response && response.code === 200) {
        setEntities({
          ...entities,
          vehicles: response.data.data || [],
          totalVehicles: response.data.totalElements || 0
        });
      }
    } catch (error) {
      console.error("Error fetching vehicles:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleFilterChange = (e) => {
    setFilters({
      ...filters,
      keyword: e.target.value
    });
    setPage(0);
  };

  // Check if a vehicle is selected
  const isSelected = (id) => {
    return deliveryPlan.vehicleIds.indexOf(id) !== -1;
  };

  // Handle vehicle selection
  const handleVehicleSelection = (vehicle) => {
    const selectedVehicleIds = [...deliveryPlan.vehicleIds];
    const selectedVehicles = [...entities.selectedVehicles];
    const vehicleIndex = selectedVehicleIds.indexOf(vehicle.id);
    
    if (vehicleIndex === -1) {
      // Add vehicle
      selectedVehicleIds.push(vehicle.id);
      selectedVehicles.push(vehicle);
    } else {
      // Remove vehicle
      selectedVehicleIds.splice(vehicleIndex, 1);
      selectedVehicles.splice(vehicleIndex, 1);
    }
    
    setDeliveryPlan({
      ...deliveryPlan,
      vehicleIds: selectedVehicleIds
    });
    
    setEntities({
      ...entities,
      selectedVehicles: selectedVehicles
    });
  };

  // Check if we have enough vehicles and capacity
  const checkVehicleRequirements = () => {
    if (deliveryPlan.vehicleIds.length < deliveryPlan.shipperIds.length) {
      setError(`Not enough vehicles selected. You need at least ${deliveryPlan.shipperIds.length} vehicles for ${deliveryPlan.shipperIds.length} shippers.`);
      return;
    }
    
    if (totalCapacity < parseFloat(deliveryPlan.totalWeight || 0)) {
      setError(`Insufficient vehicle capacity. Total capacity (${totalCapacity}) is less than total delivery weight (${deliveryPlan.totalWeight}).`);
      return;
    }
    
    setError("");
  };

  // Run the check when dependencies change
  useEffect(() => {
    if (deliveryPlan.shipperIds && deliveryPlan.shipperIds.length > 0 && deliveryPlan.totalWeight) {
      checkVehicleRequirements();
    }
  }, [deliveryPlan.vehicleIds, deliveryPlan.shipperIds, deliveryPlan.totalWeight, totalCapacity]);

  return (
    <Box p={2}>
      <Typography variant="h6" gutterBottom>
        Select Vehicles
      </Typography>
      
      {error && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      <Box display="flex" mb={2}>
        <TextField
          label="Search vehicles"
          variant="outlined"
          size="small"
          value={filters.keyword}
          onChange={handleFilterChange}
          fullWidth
          sx={{ mr: 2 }}
        />
        <Button 
          variant="contained" 
          color="primary"
          onClick={fetchVehicles}
        >
          Search
        </Button>
      </Box>
      
      <Box mb={2}>
        <Typography variant="subtitle2">
          Selected Vehicles: {deliveryPlan.vehicleIds.length} | 
          Total Capacity: {totalCapacity} | 
          Required for Delivery: {deliveryPlan.totalWeight || '0'}
        </Typography>
      </Box>
      
      <Paper elevation={1}>
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox"></TableCell>
                <TableCell>Vehicle Name</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Capacity</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Dimensions (L×W×H)</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    <CircularProgress size={24} />
                  </TableCell>
                </TableRow>
              ) : (
                entities.vehicles.map((vehicle) => {
                  const isItemSelected = isSelected(vehicle.id);
                  return (
                    <TableRow
                      hover
                      onClick={() => handleVehicleSelection(vehicle)}
                      role="checkbox"
                      aria-checked={isItemSelected}
                      tabIndex={-1}
                      key={vehicle.id}
                      selected={isItemSelected}
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          color="primary"
                          checked={isItemSelected}
                        />
                      </TableCell>
                      <TableCell>{vehicle.vehicleName}</TableCell>
                      <TableCell>{vehicle.vehicleTypeId}</TableCell>
                      <TableCell>{vehicle.capacity}</TableCell>
                      <TableCell>{vehicle.statusId}</TableCell>
                      <TableCell>
                        {vehicle.length}×{vehicle.width}×{vehicle.height}
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
              {!loading && entities.vehicles.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    No vehicles found
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={entities.totalVehicles}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </Paper>
    </Box>
  );
};

export default VehiclesTab;