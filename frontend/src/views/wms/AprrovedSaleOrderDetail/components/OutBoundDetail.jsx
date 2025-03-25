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
import { ShipmentProvider, useShipment } from "../context/ShipmentContext";

const OutBoundDetailContent = () => {
  const { shipmentId } = useParams(); // Lấy ID từ URL
  const { outboundData, loading, error, fetchData } = useShipment();
  console.log(outboundData);

  // Hiển thị loading
  if (loading && !outboundData) {
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
  if (error && !outboundData) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        {error}
      </Alert>
    );
  }

  // Hiển thị khi không có dữ liệu
  if (!outboundData) {
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
          Thông tin chi tiết phiếu xuất: {outboundData.id}
        </Typography>

        {/* Các nút hành động */}
        <OutBoundDetailActions
          shipmentId={outboundData.id}
          status={outboundData.statusId}
          onActionComplete={() => {
            fetchData(shipmentId);
          }}
        />
      </Stack>

      <Divider sx={{ my: 2 }} />

      {/* Thông tin chi tiết phiếu xuất */}
      <OutBoundDetailInfo data={outboundData} />

      {/* Danh sách sản phẩm */}
      <OutBoundDetailProducts products={outboundData.products} />

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
    <ShipmentProvider>
      <OutBoundDetailContent />
    </ShipmentProvider>
  );
};
export default memo(OutBoundDetail);
