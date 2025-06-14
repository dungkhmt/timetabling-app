import React from "react";
import {
  Box,
  CircularProgress,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Typography
} from "@mui/material";
import VisibilityIcon from "@mui/icons-material/Visibility";
import { useHistory } from "react-router-dom";
import { format } from "date-fns";
import {useHandleNavigate} from "../../common/utils/functions";

const FacilityHistoryTable = ({
  orderBillItems,
  loading,
  pagination,
  onChangePage,
  onChangeRowsPerPage
}) => {
  const navigate = useHandleNavigate();
  
  // Format date
  const formatDateTime = (dateString) => {
    if (!dateString) return "—";
    try {
      return format(new Date(dateString), "dd/MM/yyyy HH:mm:ss");
    } catch (error) {
      return dateString;
    }
  };

  // this function to convert the orderItemBillingTypeId to a readable string
  const getBillingTypeName = (typeId) => {
    switch (typeId) {
      case 'SALES_BILLING':
        return "Xuất"
      case 'PURCHASE_BILLING':
        return "Nhập";
      default:
        return "N-A";
    }
  }

  return (
    <Paper elevation={1}>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>STT</TableCell>
              <TableCell>Mã ghi nhận</TableCell>
              <TableCell>Hành vi</TableCell>
              <TableCell>Mã sản phẩm</TableCell>
              <TableCell>Tên sản phẩm</TableCell>
              <TableCell>Số lượng</TableCell>
              <TableCell>Đơn vị</TableCell>
              <TableCell>Kho</TableCell>
              <TableCell>Thời gian</TableCell>
            </TableRow>
          </TableHead>
          
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={9} align="center">
                  <Box py={2} display="flex" justifyContent="center" alignItems="center">
                    <CircularProgress size={24} />
                    <Typography variant="body2" sx={{ ml: 1 }}>
                      Đang tải...
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ) : orderBillItems.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} align="center">
                  <Typography variant="body1" py={2}>
                    Không có dữ liệu
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              orderBillItems.map((item, index) => (
                <TableRow key={item.id} hover sx={{ cursor: "pointer" }} onClick={() => navigate(`/wms/inventory/facility-history/details/${item.id}`)}>
                  <TableCell>{pagination.page * pagination.size + index + 1}</TableCell>
                  <TableCell>{item.id}</TableCell>
                    <TableCell>{getBillingTypeName(item.orderItemBillingTypeId)}</TableCell>
                  <TableCell>{item.productId}</TableCell>
                  <TableCell>{item.productName}</TableCell>
                  <TableCell>{item.quantity}</TableCell>
                  <TableCell>{item.unit || "—"}</TableCell>
                  <TableCell>{item.facilityName}</TableCell>
                  <TableCell>{formatDateTime(item.createdStamp)}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
      
      <TablePagination
        rowsPerPageOptions={[5, 10, 25]}
        component="div"
        count={pagination.totalElements}
        rowsPerPage={pagination.size}
        page={pagination.page}
        onPageChange={onChangePage}
        onRowsPerPageChange={onChangeRowsPerPage}
        labelRowsPerPage="Số hàng trên trang:"
        labelDisplayedRows={({ from, to, count }) =>
          `${from}-${to} của ${count !== -1 ? count : `hơn ${to}`}`
        }
      />
    </Paper>
  );
};

export default FacilityHistoryTable;