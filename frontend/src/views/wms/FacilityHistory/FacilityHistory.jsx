import React, { useState, useEffect } from "react";
import { Box } from "@mui/material";
import { toast } from "react-toastify";
import { useWms2Data } from "../../../services/useWms2Data";
import FacilityHistoryHeader from "./components/FacilityHistoryHeader";
import FacilityHistoryFilters from "./components/FacilityHistoryFilters";
import FacilityHistoryTable from "./components/FacilityHistoryTable";
import {MENU_CONSTANTS} from "../common/constants/screenId";
import {withAuthorization} from "../common/components/withAuthorization";

const FacilityHistory = () => {
  const { getAllOrderBillItems } = useWms2Data();
  
  const [orderBillItems, setOrderBillItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });
  
  const [filters, setFilters] = useState({
    keyword: "",
    startCreatedAt: null,
    endCreatedAt: null
  });
  
  const [showFilters, setShowFilters] = useState(true);

  // Fetch order bill items on component mount and when pagination changes
  useEffect(() => {
    fetchOrderBillItems();
  }, [pagination.page, pagination.size]);

  const fetchOrderBillItems = async () => {
    setLoading(true);
    try {
      const response = await getAllOrderBillItems(
        pagination.page, // API uses 1-indexed pagination
        pagination.size,
        filters
      );
      
      if (response && response.code === 200) {
        setOrderBillItems(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải lịch sử sản phẩm");
      }
    } catch (error) {
      console.error("Error fetching order bill items:", error);
      toast.error("Lỗi khi tải lịch sử sản phẩm");
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
    fetchOrderBillItems();
  };

  // Reset filters
  const handleResetFilters = () => {
    setFilters({
      keyword: "",
      startCreatedAt: null,
      endCreatedAt: null
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    setTimeout(() => {
      fetchOrderBillItems();
    }, 0);
  };

  // Toggle filters visibility
  const handleToggleFilters = () => {
    setShowFilters(!showFilters);
  };

  return (
    <Box p={3}>
      <FacilityHistoryHeader 
        onResetFilters={handleResetFilters}
        showFilters={showFilters}
        onToggleFilters={handleToggleFilters}
      />

      {showFilters && (
        <FacilityHistoryFilters
          filters={filters}
          onFilterChange={handleFilterChange}
          onApplyFilters={handleApplyFilters}
          onResetFilters={handleResetFilters}
        />
      )}

      <FacilityHistoryTable 
        orderBillItems={orderBillItems}
        loading={loading}
        pagination={pagination}
        onChangePage={handleChangePage}
        onChangeRowsPerPage={handleChangeRowsPerPage}
      />
    </Box>
  );
};

export default withAuthorization(FacilityHistory, MENU_CONSTANTS.INVENTORY_DETAIL_LIST);