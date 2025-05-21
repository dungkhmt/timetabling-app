import React, { useState, useEffect } from "react";
import { Box, Tabs, Tab, Paper } from "@mui/material";
import { useWms2Data } from "../../../services/useWms2Data";
import { toast } from "react-toastify";
import DeliveryRouteListHeader from "./components/DeliveryRouteListHeader";
import DeliveryRouteFilters from "./components/DeliveryRouteFilters";
import DeliveryRouteTable from "./components/DeliveryRouteTable";

// Constants for delivery route status
const DELIVERY_ROUTE_STATUSES = [
  { id: "", name: "Tất cả" },
  { id: "ASSIGNED", name: "Đã phân công" },
  { id: "IN_PROGRESS", name: "Đang giao hàng" },
  { id: "COMPLETED", name: "Đã hoàn thành" },
  { id: "CANCELLED", name: "Đã hủy" }
];

const DeliveryRouteListPage = () => {
  const { getAllDeliveryRoutes } = useWms2Data();
  
  const [deliveryRoutes, setDeliveryRoutes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });
  
  // Selected tab for status filtering
  const [selectedTab, setSelectedTab] = useState(0);
  
  const [filters, setFilters] = useState({
    keyword: "",
    statusId: ""  // Changed from array to string to match backend
  });
  
  const [showFilters, setShowFilters] = useState(true);

  // Handle tab change
  const handleTabChange = (event, newValue) => {
    setSelectedTab(newValue);
    
    // Update status filter based on selected tab
    const newStatusFilter = DELIVERY_ROUTE_STATUSES[newValue].id;
    
    setFilters(prev => ({
      ...prev,
      statusId: newStatusFilter
    }));
    
    // Reset pagination to first page
    setPagination(prev => ({
      ...prev,
      page: 0
    }));
    
    // Fetch with new status filter
    setTimeout(() => {
      fetchDeliveryRoutes(newStatusFilter);
    }, 0);
  };

  // Fetch delivery routes on component mount and when pagination changes
  useEffect(() => {
    fetchDeliveryRoutes();
  }, [pagination.page, pagination.size]);

  const fetchDeliveryRoutes = async (statusOverride = null) => {
    setLoading(true);
    try {
      // Use status override if provided, otherwise use current filters
      const filterPayload = {
        ...filters,
        statusId: statusOverride !== null ? statusOverride : filters.statusId
      };
      
      const response = await getAllDeliveryRoutes(
        pagination.page, // API uses 1-indexed pagination
        pagination.size,
        filterPayload
      );
      
      if (response && response.code === 200) {
        setDeliveryRoutes(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải danh sách chuyến giao hàng");
      }
    } catch (error) {
      console.error("Error fetching delivery routes:", error);
      toast.error("Lỗi khi tải danh sách chuyến giao hàng");
    } finally {
      setLoading(false);
    }
  };

  // Handle page change
  const handleChangePage = (event, newPage) => {
    setPagination(prev => ({
      ...prev,
      page: newPage
    }));
  };

  // Handle rows per page change
  const handleChangeRowsPerPage = event => {
    setPagination({
      page: 0,
      size: parseInt(event.target.value, 10),
      totalElements: pagination.totalElements,
      totalPages: pagination.totalPages
    });
  };

  // Handle filter changes
  const handleFilterChange = (name, value) => {
    setFilters(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Apply filters
  const handleApplyFilters = () => {
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchDeliveryRoutes();
    
    // Find and select the tab that matches the status in filters, or select "All" if none
    const statusTabIndex = DELIVERY_ROUTE_STATUSES.findIndex(
      status => status.id === filters.statusId
    );
    setSelectedTab(statusTabIndex >= 0 ? statusTabIndex : 0);
  };

  // Reset filters
  const handleResetFilters = () => {
    setFilters({
      keyword: "",
      statusId: ""
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    setSelectedTab(0); // Reset to "ALL" tab
    setTimeout(() => {
      fetchDeliveryRoutes();
    }, 0);
  };

  // Toggle filters visibility
  const handleToggleFilters = () => {
    setShowFilters(!showFilters);
  };

  return (
    <Box p={3}>
      <DeliveryRouteListHeader 
        onResetFilters={handleResetFilters}
        showFilters={showFilters}
        onToggleFilters={handleToggleFilters}
      />

      {/* Status Tabs */}
      <Paper elevation={1} sx={{ mb: 3 }}>
        <Tabs 
          value={selectedTab} 
          onChange={handleTabChange}
          indicatorColor="primary"
          textColor="primary"
          variant="scrollable"
          scrollButtons="auto"
          aria-label="delivery route status tabs"
        >
          {DELIVERY_ROUTE_STATUSES.map((status) => (
            <Tab key={status.id} label={status.name} />
          ))}
        </Tabs>
      </Paper>

      {showFilters && (
        <DeliveryRouteFilters
          filters={filters}
          onFilterChange={handleFilterChange}
          onApplyFilters={handleApplyFilters}
          onResetFilters={handleResetFilters}
          hideStatusFilter={true} // Hide status filter since we're using tabs
        />
      )}

      <DeliveryRouteTable 
        deliveryRoutes={deliveryRoutes}
        loading={loading}
        pagination={pagination}
        onChangePage={handleChangePage}
        onChangeRowsPerPage={handleChangeRowsPerPage}
      />
    </Box>
  );
};

export default DeliveryRouteListPage;