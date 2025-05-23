import React from 'react';
import { Grid, Card, CardContent, Typography, Box } from '@mui/material';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import AssignmentIcon from '@mui/icons-material/Assignment';
import RouteIcon from '@mui/icons-material/Route';
import ScaleIcon from '@mui/icons-material/Scale';

const DeliverySummaryCards = ({ totalBills, totalPlans, totalRoutes, totalWeight }) => {
  // Format the weight to have 2 decimal places and thousand separators
  const formattedWeight = Number(totalWeight).toLocaleString('vi-VN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
  
  const cards = [
    {
      title: 'Phiếu giao hàng',
      value: totalBills,
      icon: <AssignmentIcon sx={{ fontSize: 40 }} />,
      color: '#4caf50'
    },
    {
      title: 'Kế hoạch giao hàng',
      value: totalPlans,
      icon: <LocalShippingIcon sx={{ fontSize: 40 }} />,
      color: '#2196f3'
    },
    {
      title: 'Tuyến giao hàng',
      value: totalRoutes,
      icon: <RouteIcon sx={{ fontSize: 40 }} />,
      color: '#ff9800'
    },
    {
      title: 'Tổng trọng lượng (kg)',
      value: formattedWeight,
      icon: <ScaleIcon sx={{ fontSize: 40 }} />,
      color: '#9c27b0'
    }
  ];
  
  return (
    <Grid container spacing={3}>
      {cards.map((card, index) => (
        <Grid item xs={12} sm={6} md={3} key={index}>
          <Card 
            elevation={2}
            sx={{
              height: '100%',
              transition: 'transform 0.3s, box-shadow 0.3s',
              '&:hover': {
                transform: 'translateY(-5px)',
                boxShadow: 8
              }
            }}
          >
            <CardContent>
              <Box 
                display="flex" 
                justifyContent="space-between" 
                alignItems="center"
              >
                <Box>
                  <Typography variant="subtitle1" color="textSecondary" gutterBottom>
                    {card.title}
                  </Typography>
                  <Typography variant="h4" fontWeight="bold">
                    {card.value}
                  </Typography>
                </Box>
                <Box 
                  sx={{
                    backgroundColor: `${card.color}20`,
                    borderRadius: '50%',
                    width: 60,
                    height: 60,
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    color: card.color
                  }}
                >
                  {card.icon}
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};

export default DeliverySummaryCards;