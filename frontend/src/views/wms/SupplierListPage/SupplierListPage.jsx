import React, { useState, useEffect } from "react";
import { Box } from "@mui/material";
import { toast } from "react-toastify";
import { useHistory } from "react-router-dom";
import SupplierListHeader from "./components/SupplierListHeader";
import SupplierFilters from "./components/SupplierFilters";
import SupplierTable from "./components/SupplierTable";
import { useWms2Data } from "../../../services/useWms2Data";

const SupplierListPage = () => {
  const [loading, setLoading] = useState(true);
  const [suppliers, setSuppliers] = useState([]);
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
  const { getSuppliersWithFilters } = useWms2Data();

  // Fetch suppliers on component mount and when pagination or filters change
  useEffect(() => {
    fetchSuppliers();
  }, [pagination.page, pagination.size]);

  const fetchSuppliers = async () => {
    setLoading(true);
    try {
      const response = await getSuppliersWithFilters(
        pagination.page, // API pagination is 1-indexed
        pagination.size,
        filters
      );

      if (response && response.code === 200) {
        setSuppliers(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải danh sách nhà cung cấp");
      }
    } catch (error) {
      console.error("Error fetching suppliers:", error);
      toast.error("Lỗi khi tải danh sách nhà cung cấp");
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
    fetchSuppliers();
  };

  // Reset filters
  const handleResetFilters = () => {
    setFilters({
      keyword: "",
      statusId: []
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchSuppliers();
  };

  // Navigate to create new supplier
  const handleCreateSupplier = () => {
    history.push("/wms/purchase/suppliers/create");
  };

  // Navigate to supplier detail page
  const handleViewSupplierDetail = (supplierId) => {
    history.push(`/wms/purchase/suppliers/details/${supplierId}`);
  };

  return (
    <Box p={3}>
      <SupplierListHeader
        onCreateSupplier={handleCreateSupplier}
        onResetFilters={handleResetFilters}
      />

      <SupplierFilters
        filters={filters}
        onFilterChange={handleFilterChange}
        onMultipleFilterChange={handleMultipleFilterChange}
        onApplyFilters={handleApplyFilters}
      />

      <SupplierTable
        suppliers={suppliers}
        loading={loading}
        onViewDetail={handleViewSupplierDetail}
        pagination={pagination}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Box>
  );
};

export default SupplierListPage;