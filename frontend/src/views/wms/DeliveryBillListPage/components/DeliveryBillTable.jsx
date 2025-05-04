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

const DeliveryBillTable = ({
  loading,
  deliveryBills,
  pagination,
  onChangePage,
  onChangeRowsPerPage,
  statuses,
  priorityLevels
}) => {
  const history = useHistory();

  // Navigate to delivery bill detail page
  const handleViewDetails = (id) => {
    history.push(`/wms/delivery/bills/details/${id}`);
  };

  // Get status chip
  const getStatusChip = (status) => {
    const statusConfig = statuses[status] || { label: status, color: "default" };
    
    return (
      <Chip
        label={statusConfig.label}
        color={statusConfig.color}
        size="small"
      />
    );
  };

  // Get priority chip
  const getPriorityChip = (priority) => {
    const priorityConfig = priorityLevels[priority] || { label: "Không xác định", color: "default" };
    
    return (
      <Chip
        label={priorityConfig.label}
        color={priorityConfig.color}
        size="small"
        variant="outlined"
      />
    );
  };

  return (
    <Paper sx={{ width: "100%", overflow: "hidden" }}>
      <TableContainer sx={{ maxHeight: 440 }}>
        <Table stickyHeader aria-label="delivery bills table">
          <TableHead>
            <TableRow>
              <TableCell>Mã phiếu giao</TableCell>
              <TableCell>Tên phiếu giao</TableCell>
              <TableCell>Khách hàng</TableCell>
              <TableCell align="center">Ưu tiên</TableCell>
              <TableCell align="center">Trọng lượng</TableCell>
              <TableCell align="center">Ngày giao dự kiến</TableCell>
              <TableCell align="center">Trạng thái</TableCell>
            </TableRow>
          </TableHead>
          
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <CircularProgress size={40} />
                </TableCell>
              </TableRow>
            ) : deliveryBills.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography variant="body1">
                    Không tìm thấy phiếu giao hàng nào
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              deliveryBills.map((bill) => (
                <TableRow 
                  hover 
                  key={bill.id}
                  onClick={() => handleViewDetails(bill.id)}
                  sx={{ cursor: 'pointer' }}
                >
                  <TableCell>{bill.id}</TableCell>
                  <TableCell>{bill.deliveryBillName}</TableCell>
                  <TableCell>{bill.customerName}</TableCell>
                  <TableCell align="center">{getPriorityChip(bill.priority)}</TableCell>
                  <TableCell align="center">
                    {bill.totalWeight ? `${bill.totalWeight.toFixed(2)} kg` : '-'}
                  </TableCell>
                  <TableCell align="center">
                    {bill.expectedDeliveryDate 
                      ? format(new Date(bill.expectedDeliveryDate), 'dd/MM/yyyy', { locale: vi })
                      : '-'
                    }
                  </TableCell>
                  <TableCell align="center">{getStatusChip(bill.statusId)}</TableCell>
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

export default DeliveryBillTable;