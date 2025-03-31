import React, { useState, useEffect } from "react";
import { Box, CircularProgress, Typography } from "@mui/material";
import SaleOrderHeader from "./components/SaleOrderHeader";
import SaleOrderTabs from "./components/SaleOrderTabs";
import SaleOrderFilters from "./components/SaleOrderFilters";
import SaleOrderTable from "./components/SaleOrderTable";
import { useWms2Data } from 'services/useWms2Data';
import writeXlsxFile from "write-excel-file";
import { SALE_ORDER_SCHEMA } from "../common/constants/constants";

const SaleOrderListPage = () => {
  const [activeTab, setActiveTab] = useState("ALL");
  const [filters, setFilters] = useState({
    keyword: "",
    status: null,
    startCreatedAt: null,
    endCreatedAt: null,
    saleChannelId: null
  });
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pagination, setPagination] = useState({
    page: 1,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });
  
  const { getSalesOrders, getSalesOrdersForExport } = useWms2Data();

  // This effect will fetch orders whenever filters or pagination changes
  useEffect(() => {
    fetchOrders();
  }, [activeTab, pagination.page, pagination.size, filters.keyword]);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      // Prepare filter parameters based on active tab and filters
      const filterParams = {
        ...filters,
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


  // Handle page change
  const handlePageChange = (event, newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  // Handle rows per page change
  const handleChangeRowsPerPage = (event) => {
    setPagination(prev => ({
      ...prev,
      size: parseInt(event.target.value, 10),
      page: 1
    }));
  };

  // Handle applying filters
  const handleApplyFilters = () => {
    setPagination(prev => ({ ...prev, page: 1 })); // Reset to first page
    fetchOrders();
  };

  // Handle tab change
  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
    setFilters(prev => ({ ...prev, status: newValue == "ALL" ? null : newValue }));
    setPagination(prev => ({ ...prev, page: 1 })); // Reset to first page
  };

  const handleExportOrders = async () => {
    try {
      debugger;
      const result = await getSalesOrdersForExport(1, 100);
      if(result) {
        const orders = result.data.data || [];
        await exportSaleOrdersToExcel(orders);
    }
    }
    catch (error) {
      console.error("Error exporting orders:", error);
    }
  }

  const exportSaleOrdersToExcel = async (orders, fileName = 'sale_orders.xlsx') => {
    try {
      await writeXlsxFile(orders, {
        schema: SALE_ORDER_SCHEMA,
        fileName,
        sheet: 'Danh sách đơn hàng',
        headerStyle: {
          backgroundColor: '#eeeeee',
          fontWeight: 'bold',
          align: 'center',
          fontSize: 12
        }
      });
      return true;
    } catch (error) {
      console.error('Error exporting Excel file:', error);
      return false;
    }
  };

  return (
    <Box p={3}>
      <SaleOrderHeader onRefresh={fetchOrders} onExport={handleExportOrders}/>
      <SaleOrderTabs 
        value={activeTab} 
        onChange={handleTabChange}
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
