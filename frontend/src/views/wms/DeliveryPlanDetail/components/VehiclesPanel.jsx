import React from 'react';
import { 
  Box, 
  Typography, 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow,
  Chip
} from '@mui/material';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';

const VehiclesPanel = ({ vehicles }) => {
  if (!vehicles || vehicles.length === 0) {
    return (
      <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
        <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center' }}>
          <LocalShippingIcon sx={{ mr: 1 }} />
          Phương tiện
        </Typography>
        <Box sx={{ p: 2, textAlign: 'center' }}>
          <Typography color="textSecondary">Không có phương tiện nào được chỉ định cho kế hoạch này</Typography>
        </Box>
      </Paper>
    );
  }

  // Calculate total capacity
  const totalCapacity = vehicles.reduce((sum, vehicle) => 
    sum + (parseFloat(vehicle.capacity) || 0), 0);

  return (
    <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
      <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center' }}>
        <LocalShippingIcon sx={{ mr: 1 }} />
        Phương tiện ({vehicles.length})
      </Typography>

      <Box sx={{ mb: 2 }}>
        <Typography variant="subtitle2" color="textSecondary">
          Tổng trọng tải: {totalCapacity} kg
        </Typography>
      </Box>
      
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Tên phương tiện</TableCell>
              <TableCell>Loại</TableCell>
              <TableCell>Trọng tải (kg)</TableCell>
              <TableCell>Kích thước (D×R×C)</TableCell>
              <TableCell>Trạng thái</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {vehicles.map((vehicle) => (
              <TableRow key={vehicle.id}>
                <TableCell>{vehicle.id}</TableCell>
                <TableCell>{vehicle.vehicleName}</TableCell>
                <TableCell>{vehicle.vehicleTypeId}</TableCell>
                <TableCell>{vehicle.capacity}</TableCell>
                <TableCell>
                  {vehicle.length && vehicle.width && vehicle.height 
                    ? `${vehicle.length}×${vehicle.width}×${vehicle.height}`
                    : 'N/A'}
                </TableCell>
                <TableCell>
                  <Chip
                    label={vehicle.statusId}
                    color={getStatusColor(vehicle.statusId)}
                    size="small"
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
};

// Helper function to determine status color
const getStatusColor = (statusId) => {
  switch (statusId) {
    case 'AVAILABLE':
      return 'success';
    case 'ASSIGNED':
      return 'primary';
    case 'IN_USE':
      return 'warning';
    case 'MAINTENANCE':
      return 'error';
    default:
      return 'default';
  }
};

export default VehiclesPanel;