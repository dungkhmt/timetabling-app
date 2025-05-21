import React, { useState, useEffect } from "react";
import { Box, Tabs, Tab, Paper } from "@mui/material";
import { useWms2Data } from "../../../services/useWms2Data";
import { toast } from "react-toastify";
import VehicleListHeader from "./components/VehicleListHeader";
import VehicleFilters from "./components/VehicleFilters";
import VehicleTable from "./components/VehicleTable";
import {MENU_CONSTANTS} from "../common/constants/screenId";
import {withAuthorization} from "../common/components/withAuthorization";

// Constants for vehicle status
const VEHICLE_STATUSES = [
  { id: "", name: "Tất cả" },
  { id: "AVAILABLE", name: "Có sẵn" },
  { id: "ASSIGNED", name: "Đã phân công" },
  { id: "IN_USE", name: "Đang sử dụng" },
  { id: "UNDER_MAINTENANCE", name: "Đang bảo trì" }
];

const VehicleListPage = () => {
  const { getVehicles } = useWms2Data();
  
  const [vehicles, setVehicles] = useState([]);
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
    statusId: ""
  });
  
  const [showFilters, setShowFilters] = useState(true);

  // Handle tab change
  const handleTabChange = (event, newValue) => {
    setSelectedTab(newValue);
    
    // Update status filter based on selected tab
    const newStatusFilter = VEHICLE_STATUSES[newValue].id;
    
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
      fetchVehicles(newStatusFilter);
    }, 0);
  };

  // Fetch vehicles on component mount and when pagination changes
  useEffect(() => {
    fetchVehicles();
  }, [pagination.page, pagination.size]);

  const fetchVehicles = async (statusOverride = null) => {
    setLoading(true);
    try {
      // Use status override if provided, otherwise use current filters
      const filterPayload = {
        ...filters,
        statusId: statusOverride !== null ? statusOverride : filters.statusId
      };
      
      const response = await getVehicles(
        pagination.page + 1, // API uses 1-indexed pagination
        pagination.size,
        filterPayload
      );
      
      if (response && response.code === 200) {
        setVehicles(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải danh sách phương tiện");
      }
    } catch (error) {
      console.error("Error fetching vehicles:", error);
      toast.error("Lỗi khi tải danh sách phương tiện");
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
    fetchVehicles();
    
    // Find and select the tab that matches the status in filters, or select "All" if none
    const statusTabIndex = VEHICLE_STATUSES.findIndex(
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
      fetchVehicles();
    }, 0);
  };

  // Toggle filters visibility
  const handleToggleFilters = () => {
    setShowFilters(!showFilters);
  };

  return (
    <Box p={3}>
      <VehicleListHeader 
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
          aria-label="vehicle status tabs"
        >
          {VEHICLE_STATUSES.map((status) => (
            <Tab key={status.id} label={status.name} />
          ))}
        </Tabs>
      </Paper>

      {showFilters && (
        <VehicleFilters
          filters={filters}
          onFilterChange={handleFilterChange}
          onApplyFilters={handleApplyFilters}
          onResetFilters={handleResetFilters}
          hideStatusFilter={true} // Hide status filter since we're using tabs
        />
      )}

      <VehicleTable 
        vehicles={vehicles}
        loading={loading}
        pagination={pagination}
        onChangePage={handleChangePage}
        onChangeRowsPerPage={handleChangeRowsPerPage}
      />
    </Box>
  );
};

export default withAuthorization(VehicleListPage, MENU_CONSTANTS.LOGISTICS_VEHICLE_LIST);