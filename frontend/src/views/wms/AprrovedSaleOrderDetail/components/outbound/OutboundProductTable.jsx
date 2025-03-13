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
  Typography,
  Box,
} from "@mui/material";
import { useApprovedOrderDetail } from "../../context/OrderDetailContext";
import OutboundRow from "./OutboundRow";

const OutBoundTable = ({ items }) => {
  const theme = useTheme();
  const isSmall = useMediaQuery(theme.breakpoints.down("sm"));
  const { getMoreInventoryItemsApi } = useApprovedOrderDetail();
  
  const [facilities, setFacilities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [itemRows, setItemRows] = useState([]);
  
  // Khởi tạo danh sách hàng
  useEffect(() => {
    if (items && items.length > 0) {
      // Mỗi item được chuyển thành một dòng với ID duy nhất
      const initialRows = items.map(item => ({
        ...item,
        rowId: `${item.id}-main`, // ID duy nhất cho mỗi dòng
        isMainRow: true, // Đánh dấu đây là dòng chính
        parentId: null, // Không có parent vì là dòng chính
        splitQuantity: item.quantity, // Ban đầu, số lượng của dòng là toàn bộ
        remainingQuantity: item.quantity // Số lượng còn lại cần xuất
      }));
      setItemRows(initialRows);
    }
  }, [items]);
  
  // Tải danh sách kho
  useEffect(() => {
    const fetchWarehouses = async () => {
      try {
        setLoading(true);
        const response = await getMoreInventoryItemsApi(0, 100);
        if (response?.data) {
          setFacilities(response.data);
        }
      } catch (error) {
        console.error("Error fetching warehouses:", error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchWarehouses();
  }, [getMoreInventoryItemsApi]);

  // Xử lý khi tách dòng (thêm dòng phụ)
  const handleSplitRow = (parentRowId, originalItem, remainingQuantity, splitQuantity) => {
    const newRowId = `${originalItem.id}-split-${Date.now()}`;
    
    const newRow = {
      ...originalItem,
      rowId: newRowId,
      isMainRow: false,
      parentId: parentRowId,
      splitQuantity: splitQuantity,
      remainingQuantity: splitQuantity
    };
    
    // Cập nhật số lượng còn lại trong dòng chính
    const updatedRows = itemRows.map(row => {
      if (row.rowId === parentRowId) {
        return {
          ...row,
          remainingQuantity: remainingQuantity - splitQuantity
        };
      }
      return row;
    });
    
    // Thêm dòng mới
    setItemRows([...updatedRows, newRow]);
  };

  // Cập nhật số lượng đã xuất của một dòng
  const updateRowQuantity = (rowId, newRemainingQuantity) => {
    setItemRows(prevRows => 
      prevRows.map(row => 
        row.rowId === rowId 
          ? { ...row, remainingQuantity: newRemainingQuantity } 
          : row
      )
    );
  };

  // Nếu không có dữ liệu
  if (!items || items.length === 0) {
    return (
      <Box sx={{ p: 2, textAlign: "center" }}>
        <Typography color="text.secondary">Không có phiếu nào</Typography>
      </Box>
    );
  }

  return (
    <>
      <Box mb={2}>
        <Typography variant="subtitle2" color="text.secondary">
          Chọn kho xuất hàng cho mỗi phiếu
        </Typography>
      </Box>
      
      <TableContainer component={Paper} variant="outlined">
        <Table size={isSmall ? "small" : "medium"}>
          <TableHead sx={{ bgcolor: 'action.hover' }}>
            <TableRow>
              <TableCell>Mã phiếu</TableCell>
              <TableCell>Trạng thái</TableCell>
              <TableCell>Loại</TableCell>
              <TableCell>Số lượng</TableCell>
              <TableCell>Giao trước ngày</TableCell>
              <TableCell>Kho xuất hàng</TableCell>
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