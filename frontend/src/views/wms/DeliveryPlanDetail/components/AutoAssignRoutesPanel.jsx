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
  Chip,
  Tab,
  Tabs
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
import RouteMapComponent from './RouteMapComponent';

// Tab panel component
function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`route-tabpanel-${index}`}
      aria-labelledby={`route-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ pt: 2 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

const AutoAssignRoutesPanel = ({ 
  deliveryPlanId, 
  onAutoAssign, 
  optimizing, 
  optimizationResult,
  existingRoutes,
  facilityInfo
}) => {
  const [activeTab, setActiveTab] = useState(0);
  const [selectedRouteIndex, setSelectedRouteIndex] = useState(0);
  
  // Check if plan already has routes
  const hasExistingRoutes = existingRoutes && existingRoutes.length > 0;
  const hasOptimizationResult = optimizationResult && optimizationResult.shipperRoutes && optimizationResult.shipperRoutes.length > 0;
  
  // Routes to display (either from optimization result or existing routes)
  const routesToDisplay = optimizationResult?.shipperRoutes || existingRoutes || [];
  
  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };
  
  const handleRouteChange = (index) => {
    setSelectedRouteIndex(index);
  };
  
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
            
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
              <Tabs value={activeTab} onChange={handleTabChange} aria-label="route view tabs">
                <Tab label="Danh sách" icon={<List />} iconPosition="start" />
                <Tab label="Bản đồ" icon={<Map />} iconPosition="start" />
              </Tabs>
            </Box>
            
            <TabPanel value={activeTab} index={0}>
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
                              label={point.sequenceNumber}
                              color="primary"
                            />
                          </ListItemIcon>
                          <ListItemText
                            primary={point.customerName}
                            secondary={`${point.demand} kg`}
                          />
                        </ListItem>
                      ))}
                    </List>
                    
                    <Button
                      variant="outlined"
                      startIcon={<Map />}
                      fullWidth
                      sx={{ mt: 2 }}
                      onClick={() => {
                        setSelectedRouteIndex(index);
                        setActiveTab(1);
                      }}
                    >
                      Xem trên bản đồ
                    </Button>
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
            </TabPanel>
            
            <TabPanel value={activeTab} index={1}>
              {hasOptimizationResult && (
                <Box>
                  <Box sx={{ mb: 2 }}>
                    <Tabs
                      value={selectedRouteIndex}
                      onChange={(e, val) => handleRouteChange(val)}
                      variant="scrollable"
                      scrollButtons="auto"
                      aria-label="shipper routes tabs"
                    >
                      {routesToDisplay.map((route, index) => (
                        <Tab 
                          key={index}
                          label={
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                              <Person sx={{ mr: 0.5, fontSize: 18 }} />
                              <Typography variant="body2" noWrap>
                                {route.shipperName || `Shipper #${index + 1}`}
                              </Typography>
                            </Box>
                          }
                        />
                      ))}
                    </Tabs>
                  </Box>
                  
                  {routesToDisplay.length > 0 && (
                    <Box>
                      <Typography variant="subtitle2" gutterBottom>
                        Tuyến đường của {routesToDisplay[selectedRouteIndex]?.shipperName || `Shipper #${selectedRouteIndex + 1}`}:
                      </Typography>
                      
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                        <DirectionsCar color="primary" sx={{ mr: 1 }} />
                        <Typography variant="body2" color="textSecondary">
                          Tổng quãng đường: {(routesToDisplay[selectedRouteIndex]?.totalDistance / 1000).toFixed(2)} km
                          {routesToDisplay[selectedRouteIndex]?.totalLoad && (
                            <> • Tổng khối lượng: {routesToDisplay[selectedRouteIndex].totalLoad.toFixed(2)} kg</>
                          )}
                        </Typography>
                      </Box>
                      
                      <RouteMapComponent 
                        route={routesToDisplay[selectedRouteIndex]}
                        facilityLocation={facilityInfo}
                      />
                      
                      <Typography variant="subtitle2" gutterBottom>
                        Thứ tự giao hàng:
                      </Typography>
                      
                      <List dense>
                        {routesToDisplay[selectedRouteIndex]?.deliveryPoints?.map((point, pointIndex) => (
                          <ListItem key={pointIndex} divider={pointIndex < routesToDisplay[selectedRouteIndex].deliveryPoints.length - 1}>
                            <ListItemIcon>
                              <Chip 
                                size="small"
                                label={point.sequenceNumber}
                                color="primary"
                              />
                            </ListItemIcon>
                            <ListItemText
                              primary={point.customerName}
                              secondary={`${point.demand} kg`}
                            />
                          </ListItem>
                        ))}
                      </List>
                    </Box>
                  )}
                </Box>
              )}
            </TabPanel>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default AutoAssignRoutesPanel;