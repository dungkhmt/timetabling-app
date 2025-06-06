import React, { useState, useEffect, memo } from "react";
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Divider,
  Stack,
} from "@mui/material";
import { useParams } from "react-router-dom";
import OutBoundDetailInfo from "./OutBoundDetailInfo";
import OutBoundDetailProducts from "./OutBoundDetailProducts";
import OutBoundDetailActions from "./OutBoundDetailActions";
import { ShipmentProvider, useShipment } from "../../common/context/ShipmentContext";
import {SHIPMENT_TYPE_ID} from "../../common/constants/constants";

const OutBoundDetailContent = () => {
  const { shipmentId } = useParams(); // Lấy ID từ URL
  const { shipmentData, loading, error, fetchData } = useShipment();
  console.log(shipmentData);

  // Hiển thị loading
  if (loading && !shipmentData) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="300px"
      >
        <CircularProgress />
      </Box>
    );
  }

  // Hiển thị lỗi
  if (error && !shipmentData) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        {error}
      </Alert>
    );
  }

  // Hiển thị khi không có dữ liệu
  if (!shipmentData) {
    return (
      <Alert severity="info" sx={{ mb: 2 }}>
        Không tìm thấy thông tin phiếu xuất
      </Alert>
    );
  }

  return (
    <Box p={3}>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        mb={2}
      >
        <Typography variant="h5" fontWeight="bold" gutterBottom>
          Thông tin chi tiết phiếu xuất: {shipmentData.id.substring(0,8)}
        </Typography>

        {/* Các nút hành động */}
        <OutBoundDetailActions
          shipmentId={shipmentData.id}
          status={shipmentData.statusId}
          onActionComplete={() => {
            fetchData(shipmentId);
          }}
        />
      </Stack>

      <Divider sx={{ my: 2 }} />

      {/* Thông tin chi tiết phiếu xuất */}
      <OutBoundDetailInfo data={shipmentData} />

      {/* Danh sách sản phẩm */}
      <OutBoundDetailProducts products={shipmentData.products} />

      {/* Hiển thị thông báo khi đang thực hiện hành động */}
      {loading && (
        <Alert severity="info" sx={{ mt: 2 }}>
          Đang xử lý...
        </Alert>
      )}
    </Box>
  );
};

const OutBoundDetail = () => {
  return (
    <ShipmentProvider shipmentType={SHIPMENT_TYPE_ID.OUTBOUND}>
      <OutBoundDetailContent />
    </ShipmentProvider>
  );
};
export default memo(OutBoundDetail);
