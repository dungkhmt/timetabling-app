import React, { useState, useEffect } from "react";
import {
  Box,
  Button,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
} from "@mui/material";
import { toast } from "react-toastify";
import { useHistory } from "react-router-dom";
import ProductListHeader from "./components/ProductListHeader";
import ProductFilters from "./components/ProductFilters";
import ProductTable from "./components/ProductTable";
import { useWms2Data } from "../../../services/useWms2Data";
import {MENU_CONSTANTS} from "../common/constants/screenId";
import {withAuthorization} from "../common/components/withAuthorization";

const ProductListPage = () => {
  const [loading, setLoading] = useState(true);
  const [products, setProducts] = useState([]);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });
  const [filters, setFilters] = useState({
    keyword: "",
    statusId: [],
    categoryId: []
  });

  const history = useHistory();
  const { getProductsWithFilters } = useWms2Data();

  // Fetch products on component mount and when pagination or filters change
  useEffect(() => {
    fetchProducts();
  }, [pagination.page, pagination.size]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const response = await getProductsWithFilters(
        pagination.page,
        pagination.size,
        filters
      );

      if (response && response.code === 200) {
        setProducts(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải danh sách sản phẩm");
      }
    } catch (error) {
      console.error("Error fetching products:", error);
      toast.error("Lỗi khi tải danh sách sản phẩm");
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
    fetchProducts();
  };

  // Reset filters
  const handleResetFilters = () => {
    setFilters({
      keyword: "",
      statusId: [],
      categoryId: []
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchProducts();
  };

  // Navigate to create new product
  const handleCreateProduct = () => {
    history.push("/wms/admin/product/create");
  };

  // Navigate to product detail page
  const handleViewProductDetail = (productId) => {
    history.push(`/wms/admin/product/details/${productId}`);
  };

  return (
    <Box p={3}>
      <ProductListHeader
        onCreateProduct={handleCreateProduct}
        onResetFilters={handleResetFilters}
      />

      <ProductFilters
        filters={filters}
        onFilterChange={handleFilterChange}
        onMultipleFilterChange={handleMultipleFilterChange}
        onApplyFilters={handleApplyFilters}
      />

      <ProductTable
        products={products}
        loading={loading}
        onViewDetail={handleViewProductDetail}
        pagination={pagination}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Box>
  );
};

export default withAuthorization(ProductListPage, MENU_CONSTANTS.PRODUCT_LIST);