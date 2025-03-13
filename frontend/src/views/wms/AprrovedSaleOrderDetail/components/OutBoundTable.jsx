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

const OutBoundTable = ({ items = [], facilities = [], onExport }) => {
  const theme = useTheme();
  const isSmall = useMediaQuery(theme.breakpoints.down("sm"));
  const [itemRows, setItemRows] = useState([]);
  const [exportedItems, setExportedItems] = useState([]);
  
  // Khởi tạo rows từ items
  useEffect(() => {
    if (items?.length > 0) {
      const initialRows = items.map((item, index) => ({
        ...item,
        rowId: `${item.id || item.productId}-main-${index}`,
        isMainRow: true,
        parentId: null,
        splitQuantity: item.quantity || 0,
        remainingQuantity: item.quantity || 0,
      }));
      setItemRows(initialRows);
    }
  }, [items]);

  // Xử lý tách row khi số lượng không đủ
  const handleSplitRow = (parentRowId, originalRow) => {
    // Tạo ID mới cho row được tách
    const splitRowId = `${originalRow.id || originalRow.productId}-split-${Date.now()}`;
    
    // Lấy số lượng tách từ originalRow (đã được xác định trong dialog)
    const splitQuantity = originalRow.quantity;
    
    // Tìm row cha
    const parentRow = itemRows.find(row => row.rowId === parentRowId);
    if (!parentRow) return;
    
    // Tạo row mới từ thông tin của row cha
    const newRow = {
      ...parentRow,
      rowId: splitRowId,
      isMainRow: false,
      parentId: parentRowId,
      splitQuantity: splitQuantity,
      remainingQuantity: splitQuantity
    };
    
    // Cập nhật số lượng còn lại của row cha
    const updatedRows = itemRows.map(row => {
      if (row.rowId === parentRowId) {
        return {
          ...row,
          remainingQuantity: row.remainingQuantity - splitQuantity
        };
      }
      return row;
    });
    
    // Thêm row mới vào danh sách
    setItemRows([...updatedRows, newRow]);
  };

  // Cập nhật số lượng còn lại của row
  const updateRowQuantity = (rowId, newRemainingQuantity) => {
    setItemRows(prevRows => 
      prevRows.map(row => 
        row.rowId === rowId 
          ? { ...row, remainingQuantity: newRemainingQuantity } 
          : row
      )
    );
  };

  // Xử lý xuất kho
  const handleExport = (rowId, exportData) => {
    // Đánh dấu row này đã được xuất
    updateRowQuantity(rowId, 0);
    
    // Thêm vào danh sách đã xuất
    setExportedItems(prev => [...prev, { rowId, ...exportData }]);
    
    // Gọi callback để thông báo ra bên ngoài
    if (onExport) {
      onExport(exportData);
    }
  };

  // Kiểm tra nếu không có dữ liệu
  if (!items || items.length === 0) {
    return (
      <Box sx={{ p: 3, textAlign: "center" }}>
        <Typography color="text.secondary">Không có phiếu nào</Typography>
      </Box>
    );
  }

  // Hiển thị thông báo nếu có phiếu đã xuất
  const renderExportedAlert = () => {
    if (exportedItems.length > 0) {
      return (
        <Alert severity="success" sx={{ mb: 2 }}>
          Đã xuất {exportedItems.length} phiếu thành công
        </Alert>
      );
    }
    return null;
  };

  return (
    <>
      {renderExportedAlert()}
      <TableContainer component={Paper}>
        <Table size={isSmall ? "small" : "medium"}>
          <TableHead>
            <TableRow>
              <TableCell>Mã phiếu/SP</TableCell>
              <TableCell>Trạng thái</TableCell>
              <TableCell>Đơn vị</TableCell>
              <TableCell>Số lượng</TableCell>
              <TableCell>Giao trước ngày</TableCell>
              <TableCell>Kho xuất</TableCell>
              <TableCell>Thao tác</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {itemRows.map((row) => (
              <OutboundRow 
                key={row.rowId}
                row={row}
                facilities={facilities}
                onSplitRow={handleSplitRow}
                updateRowQuantity={updateRowQuantity}
                onExport={handleExport}
                isSmall={isSmall}
              />
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
};

export default React.memo(OutBoundTable);