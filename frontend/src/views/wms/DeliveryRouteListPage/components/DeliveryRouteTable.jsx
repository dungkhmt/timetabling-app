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

const DeliveryRouteTable = ({
  deliveryRoutes,
  loading,
  pagination,
  onChangePage,
  onChangeRowsPerPage
}) => {
  const history = useHistory();

  const navigate = useHandleNavigate();

  // Get status chip based on status ID
  const getStatusChip = (statusId) => {
    switch (statusId) {
      case 'ASSIGNED':
        return <Chip size="small" label="Đã phân công" color="warning" />;
      case 'IN_PROGRESS':
        return <Chip size="small" label="Đang giao hàng" color="info" />;
      case 'COMPLETED':
        return <Chip size="small" label="Đã hoàn thành" color="success" />;
      case 'CANCELLED':
        return <Chip size="small" label="Đã hủy" color="error" />;
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
              <TableCell>Mã chuyến</TableCell>
              <TableCell>Đợt giao hàng</TableCell>
              <TableCell>Tài xế</TableCell>
              <TableCell>Phương tiện</TableCell>
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
            ) : deliveryRoutes.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography variant="body1" py={2}>
                    Không có dữ liệu
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              deliveryRoutes.map((route, index) => (
                <TableRow key={route.id} hover sx={{cursor : "pointer"}} onClick={() => navigate(`/wms/logistics/deliveryroute/${route.id}`)}>
                  <TableCell>{pagination.page * pagination.size + index + 1}</TableCell>
                  <TableCell>{route.id}</TableCell>
                  <TableCell>{route.deliveryPlanName}</TableCell>
                  <TableCell>{route.assignToShipperName || "—"}</TableCell>
                  <TableCell>{route.assignToVehicleName || "—"}</TableCell>
                  <TableCell>{getStatusChip(route.statusId)}</TableCell>
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

export default DeliveryRouteTable;