import React, { useState, useEffect } from "react";
import { Box } from "@mui/material";
import { toast } from "react-toastify";
import { useWms2Data } from "../../../services/useWms2Data";
import ShipperListHeader from "./components/ShipperListHeader";
import ShipperFilters from "./components/ShipperFilters";
import ShipperTable from "./components/ShipperTable";
import {withAuthorization} from "../common/components/withAuthorization";
import {MENU_CONSTANTS} from "../common/constants/screenId";

const ShipperListPage = () => {
  const { getShippers } = useWms2Data();
  
  const [shippers, setShippers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });
  
  const [filters, setFilters] = useState({
    keyword: "",
    statusId: []
  });
  
  const [showFilters, setShowFilters] = useState(true);

  // Fetch shippers on component mount and when pagination changes
  useEffect(() => {
    fetchShippers();
  }, [pagination.page, pagination.size]);

  const fetchShippers = async () => {
    setLoading(true);
    try {
      const response = await getShippers(
        pagination.page + 1, // API uses 1-indexed pagination
        pagination.size,
        filters
      );
      
      if (response && response.code === 200) {
        setShippers(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải danh sách nhân viên giao hàng");
      }
    } catch (error) {
      console.error("Error fetching shippers:", error);
      toast.error("Lỗi khi tải danh sách nhân viên giao hàng");
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
    fetchShippers();
  };

  // Reset filters
  const handleResetFilters = () => {
    setFilters({
      keyword: "",
      statusId: []
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    setTimeout(() => {
      fetchShippers();
    }, 0);
  };

  // Toggle filters visibility
  const handleToggleFilters = () => {
    setShowFilters(!showFilters);
  };

  return (
    <Box p={3}>
      <ShipperListHeader 
        onResetFilters={handleResetFilters}
        showFilters={showFilters}
        onToggleFilters={handleToggleFilters}
      />

      {showFilters && (
        <ShipperFilters
          filters={filters}
          onFilterChange={handleFilterChange}
          onApplyFilters={handleApplyFilters}
          onResetFilters={handleResetFilters}
        />
      )}

      <ShipperTable 
        shippers={shippers}
        loading={loading}
        pagination={pagination}
        onChangePage={handleChangePage}
        onChangeRowsPerPage={handleChangeRowsPerPage}
      />
    </Box>
  );
};

export default withAuthorization(ShipperListPage, MENU_CONSTANTS.LOGISTICS_SHIPPER_LIST);