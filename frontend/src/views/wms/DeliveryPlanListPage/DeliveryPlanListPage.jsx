import React, { useState, useEffect } from "react";
import { Box } from "@mui/material";
import DeliveryPlanListHeader from "./components/DeliveryPlanListHeader";
import DeliveryPlanFilters from "./components/DeliveryPlanFilters";
import DeliveryPlanTable from "./components/DeliveryPlanTable";
import { useWms2Data } from "services/useWms2Data";
import { toast } from "react-toastify";
import {MENU_CONSTANTS} from "../common/constants/screenId";
import {withAuthorization} from "../common/components/withAuthorization";

// Delivery plan status definitions to be shared across components
export const DELIVERY_PLAN_STATUSES = {
  "DRAFT": { label: "Nháp", color: "default" },
  "PENDING": { label: "Chờ duyệt", color: "info" },
  "APPROVED": { label: "Đã duyệt", color: "success" },
  "IN_PROGRESS": { label: "Đang thực hiện", color: "warning" },
  "COMPLETED": { label: "Hoàn thành", color: "success" },
  "CANCELLED": { label: "Đã hủy", color: "error" }
};

const DeliveryPlanListPage = () => {
  const { getDeliveryPlans } = useWms2Data();

  // State
  const [loading, setLoading] = useState(false);
  const [deliveryPlans, setDeliveryPlans] = useState([]);
  const [filters, setFilters] = useState({
    keyword: "",
    status: "",
    startDate: null,
    endDate: null,
    facilityId: ""
  });
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });

  // Fetch delivery plans on mount and when filters or pagination change
  useEffect(() => {
    fetchDeliveryPlans();
  }, [pagination.page, pagination.size]);

  const fetchDeliveryPlans = async () => {
    setLoading(true);
    try {
      const response = await getDeliveryPlans(
        pagination.page,
        pagination.size,
        filters
      );

      if (response && response.code === 200) {
        setDeliveryPlans(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải danh sách kế hoạch giao hàng");
      }
    } catch (error) {
      console.error("Error fetching delivery plans:", error);
      toast.error("Lỗi khi tải danh sách kế hoạch giao hàng");
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
    fetchDeliveryPlans();
  };

  // Reset filters
  const handleResetFilters = () => {
    setFilters({
      keyword: "",
      status: "",
      startDate: null,
      endDate: null,
      facilityId: ""
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchDeliveryPlans();
  };

  return (
    <Box p={3}>
      <DeliveryPlanListHeader
        onResetFilters={handleResetFilters}
      />

      <DeliveryPlanFilters
        filters={filters}
        onFilterChange={handleFilterChange}
        onApplyFilters={handleApplyFilters}
        statuses={DELIVERY_PLAN_STATUSES}
      />

      <DeliveryPlanTable
        loading={loading}
        deliveryPlans={deliveryPlans}
        pagination={pagination}
        onChangePage={handleChangePage}
        onChangeRowsPerPage={handleChangeRowsPerPage}
        statuses={DELIVERY_PLAN_STATUSES}
      />
    </Box>
  );
};

export default withAuthorization(DeliveryPlanListPage, MENU_CONSTANTS.LOGISTICS_DELIVERY_LIST);