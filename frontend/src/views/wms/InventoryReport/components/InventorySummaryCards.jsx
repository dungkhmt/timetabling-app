import React from "react";
import { 
  Grid, 
  Card, 
  CardContent, 
  Typography 
} from "@mui/material";

const InventorySummaryCards = ({ totalImportQuantity, totalExportQuantity }) => {
  return (
    <Grid container spacing={3} sx={{ mb: 3 }}>
      <Grid item xs={12} md={6}>
        <Card 
          elevation={3}
          sx={{
            background: 'linear-gradient(45deg, #4caf50 30%, #81c784 90%)',
            color: 'white',
            transition: 'transform 0.3s',
            '&:hover': { transform: 'translateY(-5px)' }
          }}
        >
          <CardContent>
            <Typography variant="h5" fontWeight="bold" gutterBottom>
              Tổng nhập kho
            </Typography>
            <Typography variant="h3">
              {totalImportQuantity.toLocaleString()}
            </Typography>
            <Typography variant="body2" sx={{ opacity: 0.8, mt: 1 }}>
              Sản phẩm
            </Typography>
          </CardContent>
        </Card>
      </Grid>
      
      <Grid item xs={12} md={6}>
        <Card 
          elevation={3}
          sx={{
            background: 'linear-gradient(45deg, #f44336 30%, #e57373 90%)',
            color: 'white',
            transition: 'transform 0.3s',
            '&:hover': { transform: 'translateY(-5px)' }
          }}
        >
          <CardContent>
            <Typography variant="h5" fontWeight="bold" gutterBottom>
              Tổng xuất kho
            </Typography>
            <Typography variant="h3">
              {totalExportQuantity.toLocaleString()}
            </Typography>
            <Typography variant="body2" sx={{ opacity: 0.8, mt: 1 }}>
              Sản phẩm
            </Typography>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};

export default InventorySummaryCards;