import React, { useState, useEffect } from "react";
import { Box, CircularProgress, Typography } from "@mui/material";
import writeXlsxFile from "write-excel-file";

const OrderListBase = ({
  type, // "sale" hoặc "purchase"
  tabs,
  filters: initialFilters,
  schema,
  children,
  getOrdersFunction,
  getOrdersForExportFunction,
  Header,
  Tabs,
  Filters,
  Table,
}) => {
  const [activeTab, setActiveTab] = useState("ALL");
  const [filters, setFilters] = useState(initialFilters);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pagination, setPagination] = useState({
    page: 1,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });

  // Fetch orders effect
  useEffect(() => {
    fetchOrders();
  }, [activeTab, pagination.page, pagination.size, filters.keyword]);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const filterParams = {
        ...filters,
      };
      
      const result = await getOrdersFunction(
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
      console.error(`Error fetching ${type} orders:`, error);
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
    setPagination(prev => ({ ...prev, page: 1 }));
    fetchOrders();
  };

  // Handle tab change
  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
    setFilters(prev => ({ ...prev, status: newValue === "ALL" ? null : newValue }));
    setPagination(prev => ({ ...prev, page: 1 }));
  };

  const handleExportOrders = async () => {
    try {
      const result = await getOrdersForExportFunction(1, 100);
      if(result) {
        const orders = result.data.data || [];
        await exportOrdersToExcel(orders);
      }
    }
    catch (error) {
      console.error(`Error exporting ${type} orders:`, error);
    }
  }

  const exportOrdersToExcel = async (orders, fileName = `${type}_orders.xlsx`) => {
    try {
      await writeXlsxFile(orders, {
        schema: schema,
        fileName,
        sheet: type === "sale" ? 'Danh sách đơn hàng bán' : 'Danh sách đơn hàng mua',
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

  const HeaderComponent = Header;
  const TabsComponent = Tabs;
  const FiltersComponent = Filters;
  const TableComponent = Table;

  return (
    <Box p={3}>
      <HeaderComponent onRefresh={fetchOrders} onExport={handleExportOrders} type={type} />
      
      <TabsComponent 
        value={activeTab} 
        onChange={handleTabChange}
        tabs={tabs}
      />
      
      <FiltersComponent 
        filters={filters} 
        setFilters={setFilters} 
        onApplyFilters={handleApplyFilters}
        type={type}
      />
      
      {loading ? (
        <Box display="flex" justifyContent="center" my={4}>
          <CircularProgress />
        </Box>
      ) : orders.length > 0 ? (
        <TableComponent 
          orders={orders}
          page={pagination.page}
          rowsPerPage={pagination.size}
          totalCount={pagination.totalElements}
          onPageChange={handlePageChange}
          onRowsPerPageChange={handleChangeRowsPerPage}
          type={type}
        />
      ) : (
        <Typography variant="body1" align="center" my={4}>
          {type === "sale" ? "Không tìm thấy đơn hàng bán nào" : "Không tìm thấy đơn hàng mua nào"}
        </Typography>
      )}
      
      {children && children({
        orders,
        loading,
        pagination,
        filters,
        activeTab,
        handlePageChange,
        handleChangeRowsPerPage,
        handleApplyFilters,
        handleTabChange,
        fetchOrders
      })}
    </Box>
  );
};

export default OrderListBase;