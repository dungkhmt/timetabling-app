import React, { useState, useEffect } from "react";
import {
  Box,
  Button,
  CircularProgress,
  Paper,
  Typography
} from "@mui/material";
import { toast } from "react-toastify";
import { useHistory } from "react-router-dom";
import CustomerListHeader from "./components/CustomerListHeader";
import CustomerFilters from "./components/CustomerFilters";
import CustomerTable from "./components/CustomerTable";
import { useWms2Data } from "../../../services/useWms2Data";
import {withAuthorization} from "../common/components/withAuthorization";
import {MENU_CONSTANTS} from "../common/constants/screenId";

const CustomerListPage = () => {
  const [loading, setLoading] = useState(true);
  const [customers, setCustomers] = useState([]);
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

  const history = useHistory();
  const { getCustomersWithFilters } = useWms2Data();

  // Fetch customers on component mount and when pagination or filters change
  useEffect(() => {
    fetchCustomers();
  }, [pagination.page, pagination.size]);

  const fetchCustomers = async () => {
    setLoading(true);
    try {
      const response = await getCustomersWithFilters(
        pagination.page, // API pagination is 1-indexed
        pagination.size,
        filters
      );

      if (response && response.code === 200) {
        setCustomers(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải danh sách khách hàng");
      }
    } catch (error) {
      console.error("Error fetching customers:", error);
      toast.error("Lỗi khi tải danh sách khách hàng");
    } finally {
      setLoading(false);
    }
  };

  // Handle page change
  const handleChangePage = (event, newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  // Handle rows per page change
  const handleChangeRowsPerPage = (event) => {
    setPagination({
      page: 0,
      size: parseInt(event.target.value, 10),
      totalElements: pagination.totalElements,
      totalPages: pagination.totalPages
    });
  };

  // Handle filter changes for text fields
  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  // Handle multiple select filter changes
  const handleMultipleFilterChange = (name, values) => {
    setFilters(prev => ({ ...prev, [name]: values }));
  };

  // Apply filters
  const handleApplyFilters = () => {
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchCustomers();
  };

  // Reset filters
  const handleResetFilters = () => {
    setFilters({
      keyword: "",
      statusId: []
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchCustomers();
  };

  // Navigate to create new customer
  const handleCreateCustomer = () => {
    history.push("/wms/sales/customers/create");
  };

  // Navigate to customer detail page
  const handleViewCustomerDetail = (customerId) => {
    history.push(`/wms/sales/customers/details/${customerId}`);
  };

  return (
    <Box p={3}>
      <CustomerListHeader
        onCreateCustomer={handleCreateCustomer}
        onResetFilters={handleResetFilters}
      />

      <CustomerFilters
        filters={filters}
        onFilterChange={handleFilterChange}
        onMultipleFilterChange={handleMultipleFilterChange}
        onApplyFilters={handleApplyFilters}
      />

      <CustomerTable
        customers={customers}
        loading={loading}
        onViewDetail={handleViewCustomerDetail}
        pagination={pagination}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Box>
  );
};

export default withAuthorization(CustomerListPage, MENU_CONSTANTS.CUSTOMER_LIST);