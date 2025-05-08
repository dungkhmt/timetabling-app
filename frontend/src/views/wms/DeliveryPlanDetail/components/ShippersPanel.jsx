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
  Avatar,
  Grid,
  Card,
  CardContent,
  CardHeader,
} from '@mui/material';
import { Person, LocationOn } from '@mui/icons-material';

const getStatusChip = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return <Chip label="Sẵn sàng" color="success" size="small" />;
    case 'ASSIGNED':
      return <Chip label="Đã phân công" color="primary" size="small" />;
    case 'BUSY':
      return <Chip label="Đang bận" color="warning" size="small" />;
    case 'OFFLINE':
      return <Chip label="Ngoại tuyến" color="error" size="small" />;
    default:
      return <Chip label={status} color="default" size="small" />;
  }
};

const ShippersPanel = ({ shippers }) => {
  if (!shippers || shippers.length === 0) {
    return (
      <Box p={3} textAlign="center">
        <Typography variant="body1" color="textSecondary">
          Không có shipper nào
        </Typography>
      </Box>
    );
  }

  return (
    <Grid container spacing={3}>
      {shippers.map(shipper => (
        <Grid item key={shipper.userLoginId} xs={12} sm={6} md={4}>
          <Card variant="outlined">
            <CardHeader
              avatar={
                <Avatar>
                  <Person />
                </Avatar>
              }
              title={shipper.shipperName || "Chưa có tên"}
              subheader={shipper.userLoginId}
              action={getStatusChip(shipper.statusId)}
            />
            <CardContent>
              <Typography variant="body2" color="textSecondary">
                <Box display="flex" alignItems="center" mt={1}>
                  <LocationOn fontSize="small" color="action" sx={{ mr: 1 }} />
                  {shipper.lastLatitude && shipper.lastLongitude ? 
                    `Vị trí: ${shipper.lastLatitude}, ${shipper.lastLongitude}` : 
                    "Không có thông tin vị trí"
                  }
                </Box>
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};

export default ShippersPanel;