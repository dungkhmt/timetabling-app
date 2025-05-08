import React, { useState } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  Typography,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Grid,
  Card,
  CardContent,
  Divider
} from '@mui/material';
import { 
  ExpandMore, 
  DirectionsCar,
  Person,
  AssignmentTurnedIn
} from '@mui/icons-material';

const getStatusChip = (status) => {
  switch (status) {
    case 'ASSIGNED':
      return <Chip label="Đã phân công" color="primary" size="small" />;
    case 'IN_PROGRESS':
      return <Chip label="Đang thực hiện" color="warning" size="small" />;
    case 'COMPLETED':
      return <Chip label="Hoàn thành" color="success" size="small" />;
    case 'CANCELLED':
      return <Chip label="Đã hủy" color="error" size="small" />;
    default:
      return <Chip label={status} color="default" size="small" />;
  }
};

const RoutesPanel = ({ routes }) => {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  if (!routes || routes.length === 0) {
    return (
      <Box p={3} textAlign="center">
        <Typography variant="body1" color="textSecondary">
          Chưa có tuyến đường nào được tạo. Vui lòng sử dụng tab "Tối ưu tuyến đường" để tạo tuyến.
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Grid container spacing={3}>
        {routes.map(route => (
          <Grid item key={route.id} xs={12} md={6}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                  <Typography variant="h6" component="div">
                    Tuyến đường #{route.id.substring(0, 8)}...
                  </Typography>
                  {getStatusChip(route.statusId)}
                </Box>
                
                <Box display="flex" alignItems="center" mb={1}>
                  <Person color="primary" sx={{ mr: 1 }} />
                  <Typography variant="body1">
                    {route.assignToShipperName || "Chưa phân công"}
                  </Typography>
                </Box>
                
                <Box display="flex" alignItems="center" mb={2}>
                  <DirectionsCar color="secondary" sx={{ mr: 1 }} />
                  <Typography variant="body1">
                    {route.assignToVehicleName || "Chưa phân công xe"}
                  </Typography>
                </Box>
                
                <Divider sx={{ my: 1 }} />
                
                <Box display="flex" justifyContent="flex-end" mt={1}>
                  <AssignmentTurnedIn color="action" />
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      
      <TablePagination
        component="div"
        count={routes.length}
        page={page}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
        labelRowsPerPage="Số hàng mỗi trang:"
        labelDisplayedRows={({ from, to, count }) => `${from}-${to} của ${count}`}
      />
    </Box>
  );
};

export default RoutesPanel;