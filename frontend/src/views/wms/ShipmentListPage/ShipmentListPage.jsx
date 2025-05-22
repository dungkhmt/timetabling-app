import React, { useState, useEffect } from "react";
import { Box } from "@mui/material";
import { toast } from "react-toastify";
import { useWms2Data } from "../../../services/useWms2Data";
import { SHIPMENT_TYPE_ID } from "../common/constants/constants";
import ShipmentListHeader from "./components/ShipmentListHeader";
import ShipmentFilters from "./components/ShipmentFilters";
import ShipmentTable from "./components/ShipmentTable";
import {MENU_CONSTANTS} from "../common/constants/screenId";
import {withAuthorization} from "../common/components/withAuthorization";

var MENU_ID = MENU_CONSTANTS.LOGISTICS_PURCHASE_SHIPMENT_LIST
const ShipmentListPageBase = ({ shipmentTypeId = SHIPMENT_TYPE_ID.INBOUND }) => {
  // SET MENU_ID
  MENU_ID = shipmentTypeId === SHIPMENT_TYPE_ID.INBOUND ? MENU_CONSTANTS.LOGISTICS_PURCHASE_SHIPMENT_LIST : MENU_CONSTANTS.LOGISTICS_SALES_SHIPMENT_LIST;
  const { getAllShipments } = useWms2Data();
  
  const [shipments, setShipments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });
  
  const [filters, setFilters] = useState({
    shipmentTypeId: shipmentTypeId,
    keyword: "",
    statusId: [],
    expectedDeliveryDate: null
  });
  
  const [showFilters, setShowFilters] = useState(true);

  // Fetch shipments on component mount and when pagination or filters change
  useEffect(() => {
    fetchShipments();
  }, [pagination.page, pagination.size, shipmentTypeId]);

  const fetchShipments = async () => {
    setLoading(true);
    try {
      // Ensure shipmentTypeId is always set correctly
      const filterPayload = {
        ...filters,
        shipmentTypeId: shipmentTypeId
      };
      
      const response = await getAllShipments(
        pagination.page, 
        pagination.size,
        filterPayload
      );
      
      if (response && response.code === 200) {
        setShipments(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải danh sách lô hàng");
      }
    } catch (error) {
      console.error("Error fetching shipments:", error);
      toast.error("Lỗi khi tải danh sách lô hàng");
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
    fetchShipments();
  };

  // Reset filters
  const handleResetFilters = () => {
    setFilters({
      shipmentTypeId: shipmentTypeId,
      keyword: "",
      statusId: [],
      expectedDeliveryDate: null
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    setTimeout(() => {
      fetchShipments();
    }, 0);
  };

  // Toggle filters visibility
  const handleToggleFilters = () => {
    setShowFilters(!showFilters);
  };

  return (
    <Box p={3}>
      <ShipmentListHeader 
        shipmentTypeId={shipmentTypeId}
        onResetFilters={handleResetFilters}
        showFilters={showFilters}
        onToggleFilters={handleToggleFilters}
      />

      {showFilters && (
        <ShipmentFilters
          filters={filters}
          onFilterChange={handleFilterChange}
          onApplyFilters={handleApplyFilters}
          onResetFilters={handleResetFilters}
          shipmentTypeId={shipmentTypeId}
        />
      )}

      <ShipmentTable 
        shipmentTypeId={shipmentTypeId}
        shipments={shipments}
        loading={loading}
        pagination={pagination}
        onChangePage={handleChangePage}
        onChangeRowsPerPage={handleChangeRowsPerPage}
      />
    </Box>
  );
};

const ShipmentListPage = (props) => {
    const { shipmentTypeId = SHIPMENT_TYPE_ID.INBOUND  } = props;
    const menuId = shipmentTypeId === SHIPMENT_TYPE_ID.INBOUND ? MENU_CONSTANTS.LOGISTICS_PURCHASE_SHIPMENT_LIST : MENU_CONSTANTS.LOGISTICS_SALES_SHIPMENT_LIST;
    const AuthorizedComponent = withAuthorization(ShipmentListPageBase, menuId);
    return <AuthorizedComponent {...props} />;
}

export default ShipmentListPage;