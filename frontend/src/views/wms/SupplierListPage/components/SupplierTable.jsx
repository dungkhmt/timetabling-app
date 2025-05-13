import React from "react";
import {
  Box,
  Button,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
  Chip
} from "@mui/material";
import VisibilityIcon from "@mui/icons-material/Visibility";

const SupplierTable = ({
  suppliers,
  loading,
  onViewDetail,
  pagination,
  onPageChange,
  onRowsPerPageChange
}) => {
  const getStatusChip = (statusId) => {
    switch (statusId) {
      case "ACTIVE":
        return <Chip label="Hoạt động" color="success" size="small" />;
      case "INACTIVE":
        return <Chip label="Không hoạt động" color="error" size="small" />;
      default:
        return <Chip label={statusId || "N/A"} size="small" />;
    }
  };

  return (
    <Paper elevation={1}>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>STT</TableCell>
              <TableCell>ID</TableCell>
              <TableCell>Tên nhà cung cấp</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Số điện thoại</TableCell>
              <TableCell>Địa chỉ</TableCell>
              <TableCell>Trạng thái</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Box display="flex" justifyContent="center" alignItems="center" py={2}>
                    <CircularProgress size={24} />
                    <Typography variant="body2" sx={{ ml: 1 }}>
                      Đang tải...
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ) : suppliers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Typography variant="body1" py={2}>Không có dữ liệu</Typography>
                </TableCell>
              </TableRow>
            ) : (
              suppliers.map((supplier, index) => (
                <TableRow key={supplier.id} hover sx = {{ cursor: "pointer" }} onClick={() => onViewDetail(supplier.id)}>
                  <TableCell>
                    {pagination.page * pagination.size + index + 1}
                  </TableCell>
                  <TableCell>{supplier.id}</TableCell>
                  <TableCell>{supplier.name}</TableCell>
                  <TableCell>{supplier.email || "—"}</TableCell>
                  <TableCell>{supplier.phone || "—"}</TableCell>
                  <TableCell>{supplier.address || "—"}</TableCell>
                  <TableCell>{getStatusChip(supplier.statusId)}</TableCell>
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
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        labelRowsPerPage="Số hàng mỗi trang:"
        labelDisplayedRows={({ from, to, count }) =>
          `${from}-${to} của ${count !== -1 ? count : `hơn ${to}`}`
        }
      />
    </Paper>
  );
};

export default SupplierTable;