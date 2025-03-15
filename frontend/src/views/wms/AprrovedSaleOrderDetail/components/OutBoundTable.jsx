import React, { useState, useEffect } from "react";
import { 
  TableContainer, 
  Table, 
  TableHead, 
  TableBody, 
  TableRow, 
  TableCell, 
  Paper,
  useTheme,
  useMediaQuery,
  Box,
  Typography,
  Alert,
} from "@mui/material";
import OutboundRow from "./outbound/OutboundRow";
import { useApprovedOrderDetail } from "../context/OrderDetailContext";

const OutBoundTable = ({ items = [] }) => {
  const theme = useTheme();
  const isSmall = useMediaQuery(theme.breakpoints.down("sm"));
  const [exportedItems, setExportedItems] = useState([]);
  const [facilities, setFacilities] = useState([]);
  const { getMoreInventoryItemsApi, createOutBoundOrderApi } = useApprovedOrderDetail();

  // Tải danh sách kho
  useEffect(() => {
    const fetchFacilities = async () => {
      try {
        const response = await getMoreInventoryItemsApi(0, 100);
        if (response?.data) {
          setFacilities(response.data);
        }
      } catch (error) {
        console.error("Error fetching facilities:", error);
      }
    };

    fetchFacilities();
  }, [getMoreInventoryItemsApi]);

  // Xử lý xuất kho
  const handleExport = async (exportData) => {
    try {
      // Thêm vào danh sách đã xuất
      setExportedItems(prev => [...prev, exportData]);

      // Chuẩn bị dữ liệu API
      const outboundData = {
        warehouseId: exportData.warehouseId,
        items: [{
          orderItemSeqId: exportData.orderItemSeqId,
          productId: exportData.productId,
          quantity: exportData.quantity
        }]
      };

      // Gọi API tạo phiếu xuất kho
      await createOutBoundOrderApi(outboundData);

    } catch (err) {
      console.error("Error creating outbound:", err);
      alert(`Xuất kho thất bại: ${err.message || "Lỗi không xác định"}`);
      // Xóa khỏi danh sách đã xuất nếu lỗi
      setExportedItems(prev => 
        prev.filter(item => item.orderItemSeqId !== exportData.orderItemSeqId)
      );
    }
  };

  // Kiểm tra nếu không có dữ liệu
  if (!items || items.length === 0) {
    return (
      <Box sx={{ p: 3, textAlign: "center" }}>
        <Typography color="text.secondary">Không có sản phẩm nào</Typography>
      </Box>
    );
  }

  return (
    <>
      {exportedItems.length > 0 && (
        <Alert severity="success" sx={{ mb: 2 }}>
          Đã xuất {exportedItems.length} sản phẩm thành công
        </Alert>
      )}

      <TableContainer component={Paper} variant="outlined">
        <Table size={isSmall ? "small" : "medium"}>
          <TableHead sx={{ bgcolor: 'action.hover' }}>
            <TableRow>
              <TableCell>Mã sản phẩm</TableCell>
              <TableCell>Tên sản phẩm</TableCell>
              <TableCell>Đơn vị</TableCell>
              <TableCell align="center">Số lượng</TableCell>
              <TableCell>Kho xuất</TableCell>
              <TableCell>Thao tác</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {items
              .filter(item => !exportedItems.some(exported => exported.orderItemSeqId === item.orderItemSeqId))
              .map((item) => (
                <OutboundRow 
                  key={item.orderItemSeqId || item.id}
                  row={item}
                  facilities={facilities}
                  onExport={handleExport}
                  isSmall={isSmall}
                />
              ))}
          </TableBody>
        </Table>
      </TableContainer>

      {exportedItems.length > 0 && (
        <Box mt={4}>
          <Typography variant="subtitle1" gutterBottom>
            Sản phẩm đã xuất kho:
          </Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead sx={{ bgcolor: 'success.light' }}>
                <TableRow>
                  <TableCell>Mã sản phẩm</TableCell>
                  <TableCell>Tên sản phẩm</TableCell>
                  <TableCell>Số lượng</TableCell>
                  <TableCell>Kho xuất</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {exportedItems.map((item, index) => (
                  <TableRow key={index}>
                    <TableCell>{item.productId}</TableCell>
                    <TableCell>{item.productName}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>
                      {facilities.find(f => f.id === item.warehouseId)?.name || item.warehouseId}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      )}
    </>
  );
};

export default React.memo(OutBoundTable);