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
import { useHistory } from "react-router-dom";
import {useHandleNavigate} from "../../common/utils/functions";

const VehicleTable = ({
  vehicles,
  loading,
  pagination,
  onChangePage,
  onChangeRowsPerPage
}) => {
  const navigate = useHandleNavigate();

  // Get status chip based on status ID
  const getStatusChip = (statusId) => {
    switch (statusId) {
      case 'AVAILABLE':
        return <Chip size="small" label="Có sẵn" color="success" />;
      case 'ASSIGNED':
        return <Chip size="small" label="Đã phân công" color="warning" />;
      case 'IN_USE':
        return <Chip size="small" label="Đang sử dụng" color="info" />;
      case 'UNDER_MAINTENANCE':
        return <Chip size="small" label="Đang bảo trì" color="error" />;
      default:
        return <Chip size="small" label={statusId || 'N/A'} />;
    }
  };

  // Format dimensions as string
  const formatDimensions = (length, width, height) => {
    if (!length && !width && !height) return "—";
    return `${length || 0} × ${width || 0} × ${height || 0} cm`;
  };

  return (
    <Paper elevation={1}>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>STT</TableCell>
              <TableCell>Mã phương tiện</TableCell>
              <TableCell>Tên phương tiện</TableCell>
              <TableCell>Loại phương tiện</TableCell>
              <TableCell>Tải trọng (kg)</TableCell>
              <TableCell>Kích thước (DxRxC)</TableCell>
              <TableCell>Trạng thái</TableCell>
            </TableRow>
          </TableHead>
          
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Box py={2} display="flex" justifyContent="center" alignItems="center">
                    <CircularProgress size={24} />
                    <Typography variant="body2" sx={{ ml: 1 }}>
                      Đang tải...
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ) : vehicles.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Typography variant="body1" py={2}>
                    Không có dữ liệu
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              vehicles.map((vehicle, index) => (
                <TableRow key={vehicle.id} hover sx={{ cursor: "pointer" }} onClick={() => navigate(`/wms/logistics/vehicle/details/${vehicle.id}`)}>
                  <TableCell>{pagination.page * pagination.size + index + 1}</TableCell>
                  <TableCell>{vehicle.id}</TableCell>
                  <TableCell>{vehicle.vehicleName}</TableCell>
                  <TableCell>{vehicle.vehicleTypeId}</TableCell>
                  <TableCell>{vehicle.capacity ? vehicle.capacity : "—"}</TableCell>
                  <TableCell>
                    {formatDimensions(vehicle.length, vehicle.width, vehicle.height)}
                  </TableCell>
                  <TableCell>{getStatusChip(vehicle.statusId)}</TableCell>
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

export default VehicleTable;