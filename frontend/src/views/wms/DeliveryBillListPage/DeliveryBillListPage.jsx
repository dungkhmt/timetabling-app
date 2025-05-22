import React, { useState, useEffect } from "react";
import { Box } from "@mui/material";
import DeliveryBillListHeader from "./components/DeliveryBillListHeader";
import DeliveryBillFilters from "./components/DeliveryBillFilters";
import DeliveryBillTable from "./components/DeliveryBillTable";
import { useWms2Data } from "services/useWms2Data";
import { toast } from "react-toastify";
import {MENU_CONSTANTS} from "../common/constants/screenId";
import {withAuthorization} from "../common/components/withAuthorization";

// Delivery bill status definitions to be shared across components
export const DELIVERY_BILL_STATUSES = {
  "CREATED": { label: "Đã tạo", color: "info" },
  "IN_PROGRESS": { label: "Đang vận chuyển", color: "warning" },
  "COMPLETED": { label: "Hoàn thành", color: "success" },
  "CANCELLED": { label: "Đã hủy", color: "error" }
};

// Priority levels
export const PRIORITY_LEVELS = {
  1: { label: "Thấp", color: "default" },
  2: { label: "Trung bình", color: "primary" },
  3: { label: "Cao", color: "error" }
};

const DeliveryBillListPage = () => {
  const { getDeliveryBills } = useWms2Data();

  // State
  const [loading, setLoading] = useState(false);
  const [deliveryBills, setDeliveryBills] = useState([]);
  const [filters, setFilters] = useState({
    keyword: "",
    status: "",
    startDate: null,
    endDate: null,
    priority: ""
  });
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });

  // Fetch delivery bills on mount and when filters or pagination change
  useEffect(() => {
    fetchDeliveryBills();
  }, [pagination.page, pagination.size]);

  const fetchDeliveryBills = async () => {
    setLoading(true);
    try {
      const response = await getDeliveryBills(
        pagination.page, // API pagination is 1-indexed
        pagination.size,
        filters
      );

      if (response && response.code === 200) {
        setDeliveryBills(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải danh sách phiếu giao hàng");
      }
    } catch (error) {
      console.error("Error fetching delivery bills:", error);
      toast.error("Lỗi khi tải danh sách phiếu giao hàng");
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

  // Handle filter changes
  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  // Apply filters
  const handleApplyFilters = () => {
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchDeliveryBills();
  };

  // Reset filters
  const handleResetFilters = () => {
    setFilters({
      keyword: "",
      status: "",
      startDate: null,
      endDate: null,
      priority: ""
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchDeliveryBills();
  };

  return (
    <Box p={3}>
      <DeliveryBillListHeader
        onResetFilters={handleResetFilters}
      />

      <DeliveryBillFilters
        filters={filters}
        onFilterChange={handleFilterChange}
        onApplyFilters={handleApplyFilters}
        statuses={DELIVERY_BILL_STATUSES}
        priorityLevels={PRIORITY_LEVELS}
      />

      <DeliveryBillTable
        loading={loading}
        deliveryBills={deliveryBills}
        pagination={pagination}
        onChangePage={handleChangePage}
        onChangeRowsPerPage={handleChangeRowsPerPage}
        statuses={DELIVERY_BILL_STATUSES}
        priorityLevels={PRIORITY_LEVELS}
      />
    </Box>
  );
};

export default withAuthorization(DeliveryBillListPage, MENU_CONSTANTS.LOGISTICS_DELIVERY_BILL_LIST);