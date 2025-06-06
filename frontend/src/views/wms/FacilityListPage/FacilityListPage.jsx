import React, { useState, useEffect } from "react";
import { Box } from "@mui/material";
import { toast } from "react-toastify";
import FacilityListHeader from "./components/FacilityListHeader";
import FacilityFilters from "./components/FacilityFilters";
import FacilityTable from "./components/FacilityTable";
import FacilityMapModal from "./components/FacilityMapModal";
import { useWms2Data } from "../../../services/useWms2Data";
import {MENU_CONSTANTS} from "../common/constants/screenId";
import {withAuthorization} from "../common/components/withAuthorization";
import { useHandleNavigate } from "../common/utils/functions";

const FacilityListPage = () => {
  const [loading, setLoading] = useState(true);
  const [facilities, setFacilities] = useState([]);
  const [mapModalOpen, setMapModalOpen] = useState(false);
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

  const navigate = useHandleNavigate();
  const { getFacilitiesWithFilters } = useWms2Data();

  // Fetch facilities on component mount and when pagination changes
  useEffect(() => {
    fetchFacilities();
  }, [pagination.page, pagination.size]);

  const fetchFacilities = async () => {
    setLoading(true);
    try {
      const response = await getFacilitiesWithFilters(
        pagination.page, // API pagination is 1-indexed
        pagination.size,
        filters
      );

      if (response && response.code === 200) {
        setFacilities(response.data.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }));
      } else {
        toast.error("Không thể tải danh sách cơ sở");
      }
    } catch (error) {
      console.error("Error fetching facilities:", error);
      toast.error("Lỗi khi tải danh sách cơ sở");
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
    fetchFacilities();
  };

  // Reset filters
  const handleResetFilters = () => {
    setFilters({
      keyword: "",
      statusId: []
    });
    setPagination(prev => ({ ...prev, page: 0 }));
    fetchFacilities();
  };

  // Navigate to create new facility
  const handleCreateFacility = () => {
    navigate("/wms/admin/facility/create");
  };

  // Navigate to facility detail page
  const handleViewFacilityDetail = (facilityId) => {
    navigate(`/wms/admin/facility/details/${facilityId}`);
  };

  // Handle view map
  const handleViewMap = () => {
    setMapModalOpen(true);
  };

  // Close map modal
  const handleCloseMapModal = () => {
    setMapModalOpen(false);
  };

  return (
    <Box p={3}>
      <FacilityListHeader
        onCreateFacility={handleCreateFacility}
        onResetFilters={handleResetFilters}
        onViewMap={handleViewMap}
      />

      <FacilityFilters
        filters={filters}
        onFilterChange={handleFilterChange}
        onMultipleFilterChange={handleMultipleFilterChange}
        onApplyFilters={handleApplyFilters}
      />

      <FacilityTable
        facilities={facilities}
        loading={loading}
        onViewDetail={handleViewFacilityDetail}
        pagination={pagination}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />

      <FacilityMapModal
        open={mapModalOpen}
        onClose={handleCloseMapModal}
        facilities={facilities}
      />
    </Box>
  );
};

export default withAuthorization(FacilityListPage, MENU_CONSTANTS.FACILITY_LIST);