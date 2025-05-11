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
  Typography,
  Card,
  CardContent,
} from "@mui/material";
import { Search, Refresh } from "@mui/icons-material";
import { useDeliveryPlanForm } from "../../context/DeliveryPlanFormContext";
import { useWms2Data } from "services/useWms2Data";
import { format } from "date-fns";
import { vi } from "date-fns/locale";

// Status color mappings
const DELIVERY_BILL_STATUSES = {
  "CREATED": { label: "Đã tạo", color: "info" },
  "IN_PROGRESS": { label: "Đang vận chuyển", color: "warning" },
  "COMPLETED": { label: "Hoàn thành", color: "success" },
  "CANCELLED": { label: "Đã hủy", color: "error" }
};

const DeliveryBillsTab = () => {
  const { deliveryPlan, setDeliveryPlan, entities, setEntities } = useDeliveryPlanForm();
  const { getDeliveryBills } = useWms2Data();
  
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState("");
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0
  });

  const fetchDeliveryBills = async () => {
    setLoading(true);
    try {
      const filters = {
        keyword: search,
        status: "CREATED" // Only show created delivery bills that haven't been assigned yet
      };
      
      const response = await getDeliveryBills(pagination.page, pagination.size, filters);
      
      if (response && response.code === 200) {
        setEntities(prev => ({
          ...prev,
          deliveryBills: response.data.data || []
        }));
        
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0
        }));
      }
    } catch (error) {
      console.error("Error fetching delivery bills:", error);
    } finally {
      setLoading(false);
    }
  };

  // Load delivery bills when component mounts or pagination changes
  useEffect(() => {
    fetchDeliveryBills();
  }, [pagination.page, pagination.size]);

  // Calculate totalWeight when selectedDeliveryBills change
  useEffect(() => {
    let weight = 0;
    entities.selectedDeliveryBills.forEach(bill => {
      weight += parseFloat(bill.totalWeight || 0);
    });
    
    // Update the deliveryPlan with totalWeight for validation in other tabs
    setDeliveryPlan({
      ...deliveryPlan,
      totalWeight: weight
    });
  }, [entities.selectedDeliveryBills]);

  const handleSearchChange = (e) => {
    setSearch(e.target.value);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchDeliveryBills();
  };

  const handleRefresh = () => {
    setSearch("");
    setPagination({ page: 0, size: 10, totalElements: 0 });
    fetchDeliveryBills();
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

  const handleBillSelect = (billId) => {
    // Toggle selection
    if (deliveryPlan.deliveryBillIds.includes(billId)) {
      // Remove from selection
      setDeliveryPlan(prev => ({
        ...prev,
        deliveryBillIds: prev.deliveryBillIds.filter(id => id !== billId)
      }));
      
      setEntities(prev => ({
        ...prev,
        selectedDeliveryBills: prev.selectedDeliveryBills.filter(bill => bill.id !== billId)
      }));
    } else {
      // Add to selection
      setDeliveryPlan(prev => ({
        ...prev,
        deliveryBillIds: [...prev.deliveryBillIds, billId]
      }));
      
      const selectedBill = entities.deliveryBills.find(bill => bill.id === billId);
      if (selectedBill) {
        setEntities(prev => ({
          ...prev,
          selectedDeliveryBills: [...prev.selectedDeliveryBills, selectedBill]
        }));
      }
    }
  };

  const getStatusChip = (status) => {
    const statusConfig = DELIVERY_BILL_STATUSES[status] || { label: status, color: "default" };
    
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
        Chọn phiếu giao hàng cần phân công
      </Typography>
      
      <Card variant="outlined">
        <CardContent>
          {/* Search bar */}
          <Box mb={2} display="flex" justifyContent="space-between" alignItems="center">
            <Box component="form" onSubmit={handleSearchSubmit} sx={{ width: '70%' }}>
              <TextField
                fullWidth
                size="small"
                placeholder="Tìm kiếm theo tên, mã phiếu, khách hàng..."
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
              Đã chọn: {deliveryPlan.deliveryBillIds.length} phiếu giao hàng
            </Typography>
          </Box>

          {/* Total weight */}
          <Box mb={2}>
            <Typography variant="subtitle2">
              Selected Delivery Bills: {deliveryPlan.deliveryBillIds.length} | 
              Total Weight: {deliveryPlan.totalWeight || '0'}
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
                        deliveryPlan.deliveryBillIds.length > 0 && 
                        deliveryPlan.deliveryBillIds.length < entities.deliveryBills.length
                      }
                      checked={
                        entities.deliveryBills.length > 0 &&
                        deliveryPlan.deliveryBillIds.length === entities.deliveryBills.length
                      }
                      onChange={(e) => {
                        if (e.target.checked) {
                          const allIds = entities.deliveryBills.map(bill => bill.id);
                          setDeliveryPlan(prev => ({
                            ...prev,
                            deliveryBillIds: allIds
                          }));
                          setEntities(prev => ({
                            ...prev,
                            selectedDeliveryBills: [...entities.deliveryBills]
                          }));
                        } else {
                          setDeliveryPlan(prev => ({
                            ...prev,
                            deliveryBillIds: []
                          }));
                          setEntities(prev => ({
                            ...prev,
                            selectedDeliveryBills: []
                          }));
                        }
                      }}
                    />
                  </TableCell>
                  <TableCell>Mã phiếu</TableCell>
                  <TableCell>Tên phiếu</TableCell>
                  <TableCell>Khách hàng</TableCell>
                  <TableCell align="center">Trọng lượng</TableCell>
                  <TableCell align="center">Ngày giao dự kiến</TableCell>
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
                ) : entities.deliveryBills.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      Không có phiếu giao hàng nào
                    </TableCell>
                  </TableRow>
                ) : (
                  entities.deliveryBills.map((bill) => (
                    <TableRow 
                      key={bill.id}
                      hover
                      onClick={() => handleBillSelect(bill.id)}
                      selected={deliveryPlan.deliveryBillIds.includes(bill.id)}
                      sx={{ cursor: 'pointer' }}
                    >
                      <TableCell padding="checkbox">
                        <Checkbox 
                          checked={deliveryPlan.deliveryBillIds.includes(bill.id)}
                          onClick={(e) => e.stopPropagation()}
                          onChange={() => handleBillSelect(bill.id)}
                        />
                      </TableCell>
                      <TableCell>{bill.id}</TableCell>
                      <TableCell>{bill.deliveryBillName}</TableCell>
                      <TableCell>{bill.customerName || '-'}</TableCell>
                      <TableCell align="center">
                        {bill.totalWeight ? `${bill.totalWeight.toFixed(2)} kg` : '-'}
                      </TableCell>
                      <TableCell align="center">
                        {bill.expectedDeliveryDate 
                          ? format(new Date(bill.expectedDeliveryDate), 'dd/MM/yyyy', { locale: vi })
                          : '-'
                        }
                      </TableCell>
                      <TableCell align="center">{getStatusChip(bill.statusId)}</TableCell>
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

export default DeliveryBillsTab;