import React from "react";
import { useHistory } from "react-router-dom";
import {
  Paper,
  TableContainer,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  TablePagination,
  CircularProgress,
  Typography,
  Chip
} from "@mui/material";
import { format } from "date-fns";
import { vi } from "date-fns/locale";

const DeliveryPlanTable = ({
  loading,
  deliveryPlans,
  pagination,
  onChangePage,
  onChangeRowsPerPage,
  statuses
}) => {
  const history = useHistory();

  // Navigate to delivery plan detail page
  const handleViewDetails = (id) => {
    history.push(`/wms/logistics/delivery/details/${id}`);
  };

  // Get status chip
  const getStatusChip = (status) => {
    const statusConfig = statuses[status] || { label: status, color: "default" };
    
    return (
      <Chip
        label={statusConfig.label}
        color={statusConfig.color}
        size="small"
        variant="filled"
      />
    );
  };

  return (
    <Paper sx={{ width: "100%", overflow: "hidden" }}>
      <TableContainer sx={{ maxHeight: 440 }}>
        <Table stickyHeader aria-label="delivery plans table">
          <TableHead>
            <TableRow>
              <TableCell>Mã kế hoạch</TableCell>
              <TableCell>Tên kế hoạch</TableCell>
              <TableCell>Người tạo</TableCell>
              <TableCell>Cơ sở</TableCell>
              <TableCell align="center">Trọng lượng</TableCell>
              <TableCell align="center">Ngày giao hàng</TableCell>
              <TableCell align="center">Ngày tạo</TableCell>
              <TableCell align="center">Trạng thái</TableCell>
            </TableRow>
          </TableHead>
          
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <CircularProgress size={40} />
                </TableCell>
              </TableRow>
            ) : deliveryPlans.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Typography variant="body1">
                    Không tìm thấy kế hoạch giao hàng nào
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              deliveryPlans.map((plan) => (
                <TableRow 
                  hover 
                  key={plan.id}
                  onClick={() => handleViewDetails(plan.id)}
                  sx={{ cursor: 'pointer' }}
                >
                  <TableCell>{plan.id}</TableCell>
                  <TableCell>{plan.delveryPlanName}</TableCell>
                  <TableCell>{plan.createdByUserName}</TableCell>
                  <TableCell>{plan.facilityName}</TableCell>
                  <TableCell align="center">
                    {plan.totalWeight ? `${plan.totalWeight.toFixed(2)} kg` : '-'}
                  </TableCell>
                  <TableCell align="center">
                    {plan.deliveryDate 
                      ? format(new Date(plan.deliveryDate), 'dd/MM/yyyy', { locale: vi })
                      : '-'
                    }
                  </TableCell>
                  <TableCell align="center">
                    {plan.createdStamp 
                      ? format(new Date(plan.createdStamp), 'dd/MM/yyyy HH:mm', { locale: vi })
                      : '-'
                    }
                  </TableCell>
                  <TableCell align="center">{getStatusChip(plan.statusId)}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
      
      <TablePagination
        rowsPerPageOptions={[5, 10, 25, 50]}
        component="div"
        count={pagination.totalElements}
        rowsPerPage={pagination.size}
        page={pagination.page}
        onPageChange={onChangePage}
        onRowsPerPageChange={onChangeRowsPerPage}
        labelRowsPerPage="Dòng mỗi trang:"
        labelDisplayedRows={({ from, to, count }) => `${from}-${to} của ${count}`}
      />
    </Paper>
  );
};

export default DeliveryPlanTable;