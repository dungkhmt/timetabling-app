import React, { useState, useEffect } from "react";
import { Box, CircularProgress, Typography } from "@mui/material";
import SaleOrderHeader from "./components/SaleOrderHeader";
import SaleOrderTabs from "./components/SaleOrderTabs";
import SaleOrderFilters from "./components/SaleOrderFilters";
import SaleOrderTable from "./components/SaleOrderTable";
import { useWms2Data } from 'services/useWms2Data';

const SaleOrderListPage = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [filters, setFilters] = useState({
    search: "",
    deliveryStatus: "all",
    dateCreated: "date"
  });
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });
  
  const { getSalesOrders } = useWms2Data();

  // This effect will fetch orders whenever filters or pagination changes
  useEffect(() => {
    fetchOrders();
  }, [activeTab, pagination.page, pagination.size]);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      // Prepare filter parameters based on active tab and filters
      const filterParams = {
        ...filters,
        status: getStatusByTab(activeTab)
      };
      
      const result = await getSalesOrders(
        pagination.page,
        pagination.size,
        filterParams
      );
      
      if (result && result.data) {
        setOrders(result.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: result.data.totalElements || 0,
          totalPages: result.data.totalPages || 0
        }));
      }
    } catch (error) {
      console.error("Error fetching orders:", error);
    } finally {
      setLoading(false);
    }
  };

  // Map tab index to status filter
  const getStatusByTab = (tabIndex) => {
    switch (tabIndex) {
      case 0: return "ALL";               // Tất cả
      case 1: return "CREATED";           // Chưa xử lý giao hàng
      case 2: return "WAITING_PICKUP";    // Chờ lấy hàng
      case 3: return "SHIPPING";          // Đang giao hàng
      case 4: return "UNPAID";            // Chưa thanh toán
      default: return "ALL";
    }
  };

  // Handle page change
  const handlePageChange = (event, newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  // Handle rows per page change
  const handleChangeRowsPerPage = (event) => {
    setPagination(prev => ({
      ...prev,
      size: parseInt(event.target.value, 10),
      page: 0
    }));
  };

  // Handle applying filters
  const handleApplyFilters = () => {
    setPagination(prev => ({ ...prev, page: 0 })); // Reset to first page
    fetchOrders();
  };

  return (
    <Box p={3}>
      <SaleOrderHeader onRefresh={fetchOrders} />
      <SaleOrderTabs 
        value={activeTab} 
        onChange={(_, newValue) => {
          setActiveTab(newValue);
          setPagination(prev => ({ ...prev, page: 0 })); // Reset to first page
        }} 
      />
      <SaleOrderFilters 
        filters={filters} 
        setFilters={setFilters} 
        onApplyFilters={handleApplyFilters} 
      />
      
      {loading ? (
        <Box display="flex" justifyContent="center" my={4}>
          <CircularProgress />
        </Box>
      ) : orders.length > 0 ? (
        <SaleOrderTable 
          orders={orders}
          page={pagination.page}
          rowsPerPage={pagination.size}
          totalCount={pagination.totalElements}
          onPageChange={handlePageChange}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      ) : (
        <Typography variant="body1" align="center" my={4}>
          Không tìm thấy đơn hàng nào
        </Typography>
      )}
    </Box>
  );
};

export default SaleOrderListPage;
