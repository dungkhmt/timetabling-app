import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  useTheme
} from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import CancelIcon from '@mui/icons-material/Cancel';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import MonetizationOnIcon from '@mui/icons-material/MonetizationOn';
import { ORDER_TYPE_ID } from '../../common/constants/constants';

const OrderSummaryCards = ({ reportData, orderType }) => {
  const theme = useTheme();

  // Format currency for display
  const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(value || 0);
  };

  return (
    <Grid container spacing={3} mb={3}>
      <Grid item xs={12} sm={6} lg={2}>
        <SummaryCard 
          icon={<ShoppingCartIcon fontSize="large" color="primary" />}
          value={reportData.totalOrders}
          label="Tổng đơn hàng"
          color={theme.palette.primary.main}
        />
      </Grid>

      <Grid item xs={12} sm={6} lg={2}>
        <SummaryCard 
          icon={<LocalShippingIcon fontSize="large" style={{ color: '#52c41a' }} />}
          value={reportData.totalApprovedOrders}
          label="Đã duyệt"
          color="#52c41a"
        />
      </Grid>

      <Grid item xs={12} sm={6} lg={2}>
        <SummaryCard 
          icon={<HourglassEmptyIcon fontSize="large" style={{ color: '#faad14' }} />}
          value={reportData.totalWaitingOrders}
          label="Chờ duyệt"
          color="#faad14"
        />
      </Grid>

      <Grid item xs={12} sm={6} lg={2}>
        <SummaryCard 
          icon={<CancelIcon fontSize="large" style={{ color: '#f5222d' }} />}
          value={reportData.totalCanceledOrders}
          label="Đã hủy"
          color="#f5222d"
        />
      </Grid>

      <Grid item xs={12} sm={6} lg={4}>
        <Card sx={{ height: '100%', backgroundColor: theme.palette.background.default }}>
          <CardContent sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
            <Box display="flex" alignItems="center" mb={1}>
              <MonetizationOnIcon fontSize="large" style={{ color: theme.palette.success.main }} />
            </Box>
            <Typography variant="h4" align="center" fontWeight="bold" color="success.main">
              {formatCurrency(reportData.totalProfit)}
            </Typography>
            <Typography variant="body2" color="textSecondary" align="center">
              {orderType === ORDER_TYPE_ID.PURCHASE_ORDER ? "Tổng giá trị đơn hàng" : "Tổng doanh thu"}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};

// Simple card component for metrics
const SummaryCard = ({ icon, value, label, color }) => {
  const theme = useTheme();
  
  return (
    <Card sx={{ height: '100%', backgroundColor: theme.palette.background.default }}>
      <CardContent sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
        <Box display="flex" alignItems="center" mb={1}>
          {icon}
        </Box>
        <Typography variant="h4" align="center" fontWeight="bold" style={{ color: color }}>
          {value}
        </Typography>
        <Typography variant="body2" color="textSecondary" align="center">
          {label}
        </Typography>
      </CardContent>
    </Card>
  );
};

export default OrderSummaryCards;