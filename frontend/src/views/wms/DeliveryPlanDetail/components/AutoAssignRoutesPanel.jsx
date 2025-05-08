import React, { useState } from 'react';
import {
  Box,
  Button,
  Typography,
  Alert,
  AlertTitle,
  CircularProgress,
  Card,
  CardContent,
  Divider,
  Grid,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Chip
} from '@mui/material';
import {
  ExpandMore,
  CheckCircle,
  Warning,
  RouteOutlined,
  Person,
  LocalShipping,
  Map,
  DirectionsCar
} from '@mui/icons-material';

const AutoAssignRoutesPanel = ({ 
  deliveryPlanId, 
  onAutoAssign, 
  optimizing, 
  optimizationResult,
  existingRoutes
}) => {
  // Check if plan already has routes
  const hasExistingRoutes = existingRoutes && existingRoutes.length > 0;
  
  return (
    <Box>
      {hasExistingRoutes && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          <AlertTitle>Tuyến đường đã tồn tại</AlertTitle>
          Kế hoạch giao hàng này đã có {existingRoutes.length} tuyến đường được phân công. 
          Nếu tiếp tục tối ưu, những tuyến đường hiện tại sẽ bị xóa và tạo lại.
        </Alert>
      )}
      
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Tối ưu hóa tuyến đường
          </Typography>
          <Typography variant="body2" color="textSecondary" paragraph>
            Hệ thống sẽ tự động tính toán và phân công tuyến đường tối ưu cho các shipper dựa trên các vận đơn trong kế hoạch giao hàng này.
            Quá trình sẽ phân tích địa điểm giao hàng, trọng lượng và các yếu tố khác để tạo ra tuyến đường hiệu quả nhất.
          </Typography>
          
          <Box display="flex" justifyContent="center" mt={2}>
            <Button
              variant="contained"
              color="primary"
              size="large"
              disabled={optimizing}
              onClick={onAutoAssign}
              startIcon={optimizing ? <CircularProgress size={20} color="inherit" /> : <RouteOutlined />}
            >
              {optimizing ? 'Đang tối ưu hóa...' : 'Tối ưu hóa tuyến đường'}
            </Button>
          </Box>
        </CardContent>
      </Card>
      
      {optimizationResult && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Kết quả tối ưu hóa
            </Typography>
            
            <Grid container spacing={2} sx={{ mb: 2 }}>
              <Grid item xs={4}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h5" textAlign="center">
                      {optimizationResult.totalDeliveries}
                    </Typography>
                    <Typography variant="body2" color="textSecondary" textAlign="center">
                      Điểm giao
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={4}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h5" textAlign="center">
                      {(optimizationResult.totalDistance / 1000).toFixed(2)} km
                    </Typography>
                    <Typography variant="body2" color="textSecondary" textAlign="center">
                      Tổng quãng đường
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={4}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h5" textAlign="center">
                      {optimizationResult.unassignedDeliveries}
                    </Typography>
                    <Typography variant="body2" color="textSecondary" textAlign="center">
                      Điểm chưa phân công
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
            
            <Divider sx={{ my: 2 }} />
            
            <Typography variant="subtitle1" gutterBottom>
              Chi tiết tuyến đường:
            </Typography>
            
            {optimizationResult.shipperRoutes && optimizationResult.shipperRoutes.map((route, index) => (
              <Accordion key={index} sx={{ mb: 1 }}>
                <AccordionSummary expandIcon={<ExpandMore />}>
                  <Box display="flex" alignItems="center" width="100%" justifyContent="space-between">
                    <Box display="flex" alignItems="center">
                      <Person color="primary" sx={{ mr: 1 }} />
                      <Typography>{route.shipperName || `Shipper #${index + 1}`}</Typography>
                    </Box>
                    <Typography variant="body2">
                      {route.deliveryPoints?.length || 0} điểm • {(route.totalDistance / 1000).toFixed(2)} km
                    </Typography>
                  </Box>
                </AccordionSummary>
                <AccordionDetails>
                  <Typography variant="subtitle2" gutterBottom>
                    Điểm giao hàng:
                  </Typography>
                  <List dense>
                    {route.deliveryPoints && route.deliveryPoints.map((point, pointIndex) => (
                      <ListItem key={pointIndex}>
                        <ListItemIcon>
                          <Chip 
                            size="small"
                            label={point.sequence} 
                            color="primary"
                          />
                        </ListItemIcon>
                        <ListItemText
                          primary={point.name}
                          secondary={' hello'}
                        />
                      </ListItem>
                    ))}
                  </List>
                </AccordionDetails>
              </Accordion>
            ))}
            
            {optimizationResult.unassignedDeliveries > 0 && (
              <Alert severity="warning" sx={{ mt: 2 }}>
                <AlertTitle>Điểm giao hàng chưa phân công</AlertTitle>
                Có {optimizationResult.unassignedDeliveries} điểm giao hàng chưa được phân công do giới hạn về khả năng chở hàng 
                hoặc số lượng shipper không đủ.
              </Alert>
            )}
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default AutoAssignRoutesPanel;