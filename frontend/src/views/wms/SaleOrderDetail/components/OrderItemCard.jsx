import React from "react";
import { Card, CardContent, Typography, Divider, Grid } from "@mui/material";

const OrderItemCard = ({ item }) => {
  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="subtitle1" fontWeight="bold">
          {item.stt}. {item.name}
        </Typography>
        <Divider sx={{ my: 1 }} />
        <Grid container spacing={1}>
          <Grid item xs={6}>
            <Typography variant="body2">
              <strong>Mã SP:</strong> {item.id}
            </Typography>
          </Grid>
          <Grid item xs={6}>
            <Typography variant="body2">
              <strong>Đơn vị:</strong> {item.unit}
            </Typography>
          </Grid>
          <Grid item xs={6}>
            <Typography variant="body2">
              <strong>Số lượng:</strong> {item.quantity}
            </Typography>
          </Grid>
          <Grid item xs={6}>
            <Typography variant="body2">
              <strong>Giá bán lẻ:</strong> {item.price}
            </Typography>
          </Grid>
          <Grid item xs={6}>
            <Typography variant="body2">
              <strong>Trước VAT:</strong> {item.preBefore}
            </Typography>
          </Grid>
          <Grid item xs={6}>
            <Typography variant="body2">
              <strong>Sau VAT:</strong> {item.preAfter}
            </Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="body1" fontWeight="bold" align="right">
              Thành tiền: {item.total}
            </Typography>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default React.memo(OrderItemCard);