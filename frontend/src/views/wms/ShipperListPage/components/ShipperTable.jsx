import React from "react";
import {
  Box,
  Button,
  Chip,
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
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import { useHistory } from "react-router-dom";

const ShipperTable = ({
  shippers,
  loading,
  pagination,
  onChangePage,
  onChangeRowsPerPage
}) => {
  const history = useHistory();

  // View shipper details
  const handleViewShipper = (userLoginId) => {
    history.push(`/wms/logistics/shipper/details/${userLoginId}`);
  };

  // Get status chip based on status ID
  const getStatusChip = (statusId) => {
    switch (statusId) {
      case 'DRIVING':
        return <Chip size="small" label="Đang lái xe" color="info" />;
      case 'ASSIGNED':
        return <Chip size="small" label="Đã phân công" color="warning" />;
      case 'IN_TRIP':
        return <Chip size="small" label="Đang trong chuyến" color="primary" />;
      case 'ACTIVE':
        return <Chip size="small" label="Hoạt động" color="success" />;
      case 'INACTIVE':
        return <Chip size="small" label="Không hoạt động" color="error" />;
      default:
        return <Chip size="small" label={statusId || 'N/A'} />;
    }
  };

  return (
    <Paper elevation={1}>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>STT</TableCell>
              <TableCell>Mã tài khoản</TableCell>
              <TableCell>Họ và tên</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Số điện thoại</TableCell>
              <TableCell>Trạng thái</TableCell>
            </TableRow>
          </TableHead>
          
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Box py={2} display="flex" justifyContent="center" alignItems="center">
                    <CircularProgress size={24} />
                    <Typography variant="body2" sx={{ ml: 1 }}>
                      Đang tải...
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ) : shippers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography variant="body1" py={2}>
                    Không có dữ liệu
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              shippers.map((shipper, index) => (
                <TableRow key={shipper.userLoginId} hover onClick={() => handleViewShipper(shipper.userLoginId)} sx ={{ cursor: 'pointer' }}>
                  <TableCell>{pagination.page * pagination.size + index + 1}</TableCell>
                  <TableCell>{shipper.userLoginId}</TableCell>
                  <TableCell>{shipper.fullName}</TableCell>
                  <TableCell>{shipper.email}</TableCell>
                  <TableCell>{shipper.phone}</TableCell>
                  <TableCell>{getStatusChip(shipper.statusId)}</TableCell>
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

export default ShipperTable;