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

const FacilityTable = ({
  facilities,
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

  const getDefaultChip = (isDefault) => {
    return isDefault ? 
      <Chip label="Mặc định" color="primary" size="small" /> : 
      <Chip label="Không mặc định" variant="outlined" size="small" />;
  };

  return (
    <Paper elevation={1}>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>STT</TableCell>
              <TableCell>Tên cơ sở</TableCell>
              <TableCell>Số điện thoại</TableCell>
              <TableCell>Mã bưu chính</TableCell>
              <TableCell>Địa chỉ</TableCell>
              <TableCell>Mặc định</TableCell>
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
            ) : facilities.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Typography variant="body1" py={2}>Không có dữ liệu</Typography>
                </TableCell>
              </TableRow>
            ) : (
              facilities.map((facility, index) => (
                <TableRow key={facility.id} hover sx ={{ cursor: "pointer" }} onClick={() => onViewDetail(facility.id)}>
                  <TableCell>
                    {pagination.page * pagination.size + index + 1}
                  </TableCell>
                  <TableCell>{facility.name}</TableCell>
                  <TableCell>{facility.phone || "—"}</TableCell>
                  <TableCell>{facility.postalCode || "—"}</TableCell>
                  <TableCell>{facility.fullAddress || "—"}</TableCell>
                  <TableCell>{getDefaultChip(facility.isDefault)}</TableCell>
                  <TableCell>{getStatusChip(facility.statusId)}</TableCell>
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

export default FacilityTable;