import React, { useState, useEffect } from "react";
import { Box, Typography, CircularProgress } from "@mui/material";
import { format } from "date-fns";
import { toast } from "react-toastify";
import { useWms2Data } from "../../../services/useWms2Data";
import InventoryReportControls from "./components/InventoryReportControls";
import InventorySummaryCards from "./components/InventorySummaryCards";
import DailyMovementChart from "./components/DailyMovementChart";
import ProductChartsGrid from "./components/ProductChartsGrid";
import FacilityComparisonChart from "./components/FacilityComparisonChart";
import {MENU_CONSTANTS} from "../common/constants/screenId";
import {withAuthorization} from "../common/components/withAuthorization";

const InventoryReport = () => {
  const { getMoreFacilities, getMonthlyInventoryReport, getMonthlyFacilityReport } = useWms2Data();
  
  const [facilities, setFacilities] = useState([]);
  const [selectedFacility, setSelectedFacility] = useState("");
  const [reportData, setReportData] = useState(null);
  
  const [dateRange, setDateRange] = useState({
    startDate: new Date(new Date().setDate(1)), // First day of current month
    endDate: new Date() // Today
  });
  
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    loadFacilities();
  }, []);
  
  useEffect(() => {
    fetchReport();
  }, [selectedFacility]);
  
  const loadFacilities = async () => {
    try {
      const response = await getMoreFacilities(0,100);
      if (response && response.code === 200) {
        setFacilities(response.data.data || []);
      } else {
        toast.error("Failed to load facilities");
      }
    } catch (error) {
      console.error("Error loading facilities:", error);
      toast.error("Error loading facilities");
    }
  };
  
  const fetchReport = async () => {
    setLoading(true);
    try {
      const start = format(dateRange.startDate, 'yyyy-MM-dd');
      const end = format(dateRange.endDate, 'yyyy-MM-dd');
      
      let response;
      if (selectedFacility) {
        response = await getMonthlyFacilityReport(selectedFacility, start, end);
      } else {
        response = await getMonthlyInventoryReport(start, end);
      }
      
      if (response && response.code === 200) {
        setReportData(response.data);
      } else {
        toast.error("Failed to load report data");
        setReportData(null);
      }
    } catch (error) {
      console.error("Error fetching report:", error);
      toast.error("Error loading report data");
      setReportData(null);
    } finally {
      setLoading(false);
    }
  };
  
  const handleFacilityChange = (event) => {
    setSelectedFacility(event.target.value);
  };
  
  const handleDateRangeChange = (field, value) => {
    setDateRange(prev => ({
      ...prev,
      [field]: value
    }));
  };
  
  const handleApplyDateRange = () => {
    fetchReport();
  };

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom fontWeight="bold">
        Báo cáo Nhập Xuất Kho
      </Typography>
      
      <InventoryReportControls 
        facilities={facilities}
        selectedFacility={selectedFacility}
        dateRange={dateRange}
        onFacilityChange={handleFacilityChange}
        onDateRangeChange={handleDateRangeChange}
        onApplyDateRange={handleApplyDateRange}
      />
      
      {loading ? (
        <Box display="flex" justifyContent="center" alignItems="center" height={300}>
          <CircularProgress />
        </Box>
      ) : !reportData ? (
        <Typography align="center">Không có dữ liệu báo cáo</Typography>
      ) : (
        <>
          <InventorySummaryCards 
            totalImportQuantity={reportData.totalImportQuantity}
            totalExportQuantity={reportData.totalExportQuantity}
          />
          
          <DailyMovementChart 
            dailyMovements={reportData.dailyMovements || []}
          />
          
          <ProductChartsGrid 
            topImportedProducts={reportData.topImportedProducts || []}
            topExportedProducts={reportData.topExportedProducts || []}
          />
          
          {!selectedFacility && reportData.facilityMovements?.length > 0 && (
            <FacilityComparisonChart 
              facilityMovements={reportData.facilityMovements}
            />
          )}
        </>
      )}
    </Box>
  );
};

export default withAuthorization(InventoryReport, MENU_CONSTANTS.LOGISTICS_ADMIN_DASHBOARD);