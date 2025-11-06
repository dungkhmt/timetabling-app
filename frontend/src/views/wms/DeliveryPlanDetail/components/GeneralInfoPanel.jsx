import React from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Divider,
} from '@mui/material';
import { format } from 'date-fns';
import { vi } from 'date-fns/locale';

const InfoItem = ({ label, value }) => (
  <Box mb={1}>
    <Typography variant="subtitle2" color="textSecondary" gutterBottom>
      {label}
    </Typography>
    <Typography variant="body1">
      {value || "—"}
    </Typography>
  </Box>
);

const GeneralInfoPanel = ({ deliveryPlan }) => {
  return (
    <Card>
      <CardContent>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <InfoItem label="ID kế hoạch" value={deliveryPlan.id} />
            <InfoItem label="Tên kế hoạch" value={deliveryPlan.deliveryPlanName} />
            <InfoItem label="Người tạo" value={deliveryPlan.createdByUserName} />
            <InfoItem label="Cơ sở" value={deliveryPlan.facilityName} />
          </Grid>
          <Grid item xs={12} md={6}>
            <InfoItem 
              label="Ngày giao hàng" 
              value={deliveryPlan.deliveryDate ? 
                format(new Date(deliveryPlan.deliveryDate), 'dd/MM/yyyy', { locale: vi }) : null} 
            />
            <InfoItem 
              label="Tổng trọng lượng" 
              value={deliveryPlan.totalWeight ? 
                `${deliveryPlan.totalWeight.toFixed(2)} kg` : null} 
            />
            <InfoItem label="Mô tả" value={deliveryPlan.description} />
          </Grid>
        </Grid>
        <Box mt={3}>
          <Typography variant="subtitle1" gutterBottom>
            Số lượng:
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" textAlign="center">
                    {deliveryPlan.deliveryBills?.length || 0}
                  </Typography>
                  <Typography variant="body2" color="textSecondary" textAlign="center">
                    Vận đơn
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={4}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" textAlign="center">
                    {deliveryPlan.shippers?.length || 0}
                  </Typography>
                  <Typography variant="body2" color="textSecondary" textAlign="center">
                    Shipper
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={4}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" textAlign="center">
                    {deliveryPlan.existingRoutes?.length || 0}
                  </Typography>
                  <Typography variant="body2" color="textSecondary" textAlign="center">
                    Tuyến đường
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Box>
      </CardContent>
    </Card>
  );
};

export default GeneralInfoPanel;