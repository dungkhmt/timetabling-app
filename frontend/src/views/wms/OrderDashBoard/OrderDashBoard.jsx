import React, { useState, useEffect } from "react";
import {
  Box,
  Paper,
  CircularProgress,
  Divider,
  Typography
} from "@mui/material";
import { useWms2Data } from "../../../services/useWms2Data";
import { toast } from "react-toastify";
import { ORDER_TYPE_ID } from "../common/constants/constants";
import OrderSummaryCards from "./components/OrderSummaryCards";
import DailyOrderChart from "./components/DailyOrderChart";
import OrderStatusPieChart from "./components/OrderStatusPieChart";
import { Grid } from "@mui/material";
import PropTypes from "prop-types";
import { withAuthorization } from "../common/components/withAuthorization";
import { MENU_CONSTANTS } from "../common/constants/screenId";

const OrderDashBoardBase = ({ orderTypeId = ORDER_TYPE_ID.PURCHASE_ORDER }) => {
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(true);

  const { getOrderReport } = useWms2Data();

  // Fetch data when component mounts or orderTypeId changes
  useEffect(() => {
    fetchReportData();
  }, [orderTypeId]);

  const fetchReportData = async () => {
    setLoading(true);
    try {
      const response = await getOrderReport(orderTypeId);
      if (response && response.code === 200) {
        setReportData(response.data);
      } else {
        const errorMsg = orderTypeId === ORDER_TYPE_ID.PURCHASE_ORDER
          ? "Không thể tải báo cáo đơn hàng mua"
          : "Không thể tải báo cáo đơn hàng bán";
        toast.error(errorMsg);
      }
    } catch (error) {
      console.error("Error fetching report data:", error);
      toast.error(`Lỗi khi tải báo cáo: ${error.message || "Không xác định"}`);
    } finally {
      setLoading(false);
    }
  };

  // Render loading state
  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="50vh">
        <CircularProgress />
      </Box>
    );
  }

  if (!reportData) return null;

  const dashboardTitle = orderTypeId === ORDER_TYPE_ID.PURCHASE_ORDER
    ? "Báo cáo đơn hàng mua"
    : "Báo cáo đơn hàng bán";

  return (
    <Box p={3}>
      <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          {dashboardTitle} - Tháng {new Date().getMonth() + 1}/{new Date().getFullYear()}
        </Typography>
        <Typography variant="body2" color="textSecondary" gutterBottom>
          Thống kê đơn hàng từ đầu tháng đến hiện tại
        </Typography>

        <Divider sx={{ my: 2 }} />

        <OrderSummaryCards
          reportData={reportData}
          orderType={orderTypeId}
        />

        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            <Paper elevation={2} sx={{ p: 2 }}>
              <DailyOrderChart
                dailyOrderCounts={reportData.dailyOrderCounts}
                orderType={orderTypeId}
              />
            </Paper>
          </Grid>

          <Grid item xs={12} md={4}>
            <Paper elevation={2} sx={{ p: 2 }}>
              <OrderStatusPieChart
                reportData={reportData}
                orderType={orderTypeId}
              />
            </Paper>
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
};

const OrderDashBoard = (props) => {
  const { orderTypeId = ORDER_TYPE_ID.PURCHASE_ORDER } = props;
  const menuId = orderTypeId === ORDER_TYPE_ID.PURCHASE_ORDER
      ? MENU_CONSTANTS.PURCHASE_DASHBOARD
      : MENU_CONSTANTS.SALES_DASHBOARD;

  const AuthorizedComponent = withAuthorization(OrderDashBoardBase, menuId);
  return <AuthorizedComponent {...props} />;
};

export default OrderDashBoard;