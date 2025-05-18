import React, { useEffect, useState } from "react";
import {
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Checkbox,
  TablePagination,
  Chip,
  CircularProgress,
  TextField,
  InputAdornment,
  IconButton,
  Avatar,
  Typography,
  Card,
  CardContent,
} from "@mui/material";
import { Search, Refresh, Person } from "@mui/icons-material";
import { useDeliveryPlanForm } from "../../context/DeliveryPlanFormContext";
import { useWms2Data } from "services/useWms2Data";

const SHIPPER_STATUSES = {
  DRIVING: { label: "Đang lái", color: "success" },
  ASSIGNED: { label: "Đã phân công", color: "primary" },
  IN_TRIP: { label: "Đang giao hàng", color: "warning" },
  ACTIVE: { label: "Sẵn sàng", color: "success" },
  INACTIVE: { label: "Không khả dụng", color: "error" }
};

const ShippersTab = () => {
  const { deliveryPlan, setDeliveryPlan, entities, setEntities } = useDeliveryPlanForm();
  const { getShippers } = useWms2Data();
  
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState("");
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0
  });

  const fetchShippers = async () => {
    setLoading(true);
    try {
      const filters = {
        keyword: search,
        statusId: ["ACTIVE"] // Only show available shippers
      };
      
      const response = await getShippers(pagination.page, pagination.size, filters);
      
      if (response && response.code === 200) {
        setEntities(prev => ({
          ...prev,
          shippers: response.data.data || []
        }));
        
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0
        }));
      }
    } catch (error) {
      console.error("Error fetching shippers:", error);
    } finally {
      setLoading(false);
    }
  };

  // Load shippers when component mounts or pagination changes
  useEffect(() => {
    fetchShippers();
  }, [pagination.page, pagination.size]);

  const handleSearchChange = (e) => {
    setSearch(e.target.value);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchShippers();
  };

  const handleRefresh = () => {
    setSearch("");
    setPagination({ page: 0, size: 10, totalElements: 0 });
    fetchShippers();
  };

  const handleChangePage = (event, newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  const handleChangeRowsPerPage = (event) => {
    setPagination({
      page: 0,
      size: parseInt(event.target.value, 10),
      totalElements: pagination.totalElements
    });
  };

  const handleShipperSelect = (shipperId) => {
    // Toggle selection
    if (deliveryPlan.shipperIds.includes(shipperId)) {
      // Remove from selection
      setDeliveryPlan(prev => ({
        ...prev,
        shipperIds: prev.shipperIds.filter(id => id !== shipperId)
      }));
      
      setEntities(prev => ({
        ...prev,
        selectedShippers: prev.selectedShippers.filter(shipper => shipper.userLoginId !== shipperId)
      }));
    } else {
      // Add to selection
      setDeliveryPlan(prev => ({
        ...prev,
        shipperIds: [...prev.shipperIds, shipperId]
      }));
      
      const selectedShipper = entities.shippers.find(shipper => shipper.userLoginId === shipperId);
      if (selectedShipper) {
        setEntities(prev => ({
          ...prev,
          selectedShippers: [...prev.selectedShippers, selectedShipper]
        }));
      }
    }
  };

  const getStatusChip = (status) => {
    const statusConfig = SHIPPER_STATUSES[status] || { label: status, color: "default" };
    
    return (
      <Chip
        label={statusConfig.label}
        color={statusConfig.color}
        size="small"
      />
    );
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Chọn người giao hàng
      </Typography>
      
      <Card variant="outlined">
        <CardContent>
          {/* Search bar */}
          <Box mb={2} display="flex" justifyContent="space-between" alignItems="center">
            <Box component="form" onSubmit={handleSearchSubmit} sx={{ width: '70%' }}>
              <TextField
                fullWidth
                size="small"
                placeholder="Tìm kiếm theo tên, số điện thoại..."
                value={search}
                onChange={handleSearchChange}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Search />
                    </InputAdornment>
                  )
                }}
              />
            </Box>
            
            <IconButton onClick={handleRefresh} color="primary">
              <Refresh />
            </IconButton>
          </Box>
          
          {/* Selected count */}
          <Box mb={2}>
            <Typography variant="body2" fontWeight="bold">
              Đã chọn: {deliveryPlan.shipperIds.length} người giao hàng
            </Typography>
          </Box>
          
          {/* Table */}
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell padding="checkbox">
                    <Checkbox
                      indeterminate={
                        deliveryPlan.shipperIds.length > 0 && 
                        deliveryPlan.shipperIds.length < entities.shippers.length
                      }
                      checked={
                        entities.shippers.length > 0 &&
                        deliveryPlan.shipperIds.length === entities.shippers.length
                      }
                      onChange={(e) => {
                        if (e.target.checked) {
                          const allIds = entities.shippers.map(shipper => shipper.userLoginId);
                          setDeliveryPlan(prev => ({
                            ...prev,
                            shipperIds: allIds
                          }));
                          setEntities(prev => ({
                            ...prev,
                            selectedShippers: [...entities.shippers]
                          }));
                        } else {
                          setDeliveryPlan(prev => ({
                            ...prev,
                            shipperIds: []
                          }));
                          setEntities(prev => ({
                            ...prev,
                            selectedShippers: []
                          }));
                        }
                      }}
                    />
                  </TableCell>
                  <TableCell></TableCell>
                  <TableCell>Tên đăng nhập</TableCell>
                  <TableCell>Họ Tên</TableCell>
                  <TableCell align="center">Số điện thoại</TableCell>
                  <TableCell align="center">Trạng thái</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      <CircularProgress size={24} />
                    </TableCell>
                  </TableRow>
                ) : entities.shippers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      Không có người giao hàng nào khả dụng
                    </TableCell>
                  </TableRow>
                ) : (
                  entities.shippers.map((shipper) => (
                    <TableRow 
                      key={shipper.userLoginId}
                      hover
                      onClick={() => handleShipperSelect(shipper.userLoginId)}
                      selected={deliveryPlan.shipperIds.includes(shipper.userLoginId)}
                      sx={{ cursor: 'pointer' }}
                    >
                      <TableCell padding="checkbox">
                        <Checkbox 
                          checked={deliveryPlan.shipperIds.includes(shipper.userLoginId)}
                          onClick={(e) => e.stopPropagation()}
                          onChange={() => handleShipperSelect(shipper.userLoginId)}
                        />
                      </TableCell>
                      <TableCell>
                        <Avatar sx={{ width: 30, height: 30, bgcolor: 'primary.main' }}>
                          <Person fontSize="small" />
                        </Avatar>
                      </TableCell>
                      <TableCell>{shipper.userLoginId}</TableCell>
                      <TableCell>{shipper.fullName || shipper.username || '-'}</TableCell>
                      <TableCell align="center">{shipper.phone || '-'}</TableCell>
                      <TableCell align="center">{getStatusChip(shipper.statusId)}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
            
            <TablePagination
              component="div"
              count={pagination.totalElements}
              page={pagination.page}
              onPageChange={handleChangePage}
              rowsPerPage={pagination.size}
              onRowsPerPageChange={handleChangeRowsPerPage}
              rowsPerPageOptions={[5, 10, 25, 50]}
              labelRowsPerPage="Dòng mỗi trang:"
              labelDisplayedRows={({ from, to, count }) => `${from}-${to} của ${count}`}
            />
          </TableContainer>
        </CardContent>
      </Card>
    </Box>
  );
};

export default ShippersTab;