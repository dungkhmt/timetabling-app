import React, { useState } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  Typography
} from '@mui/material';
import { format } from 'date-fns';
import { vi } from 'date-fns/locale';

const getStatusChip = (status) => {
  switch (status) {
    case 'CREATED':
      return <Chip label="Đã tạo" color="primary" size="small" />;
    case 'IN_PROGRESS':
      return <Chip label="Đang xử lý" color="warning" size="small" />;
    case 'ASSIGNED':
      return <Chip label="Đã phân công" color="info" size="small" />;
    case 'COMPLETED':
      return <Chip label="Hoàn thành" color="success" size="small" />;
    case 'CANCELLED':
      return <Chip label="Đã hủy" color="error" size="small" />;
    default:
      return <Chip label={status} color="default" size="small" />;
  }
};

const DeliveryBillsPanel = ({ deliveryBills }) => {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  if (!deliveryBills || deliveryBills.length === 0) {
    return (
      <Box p={3} textAlign="center">
        <Typography variant="body1" color="textSecondary">
          Không có vận đơn nào
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Tên vận đơn</TableCell>
              <TableCell>Tên khách hàng</TableCell>
              <TableCell align="center">Trọng lượng</TableCell>
              <TableCell align="center">Ngày giao dự kiến</TableCell>
              <TableCell align="center">Trạng thái</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {deliveryBills
              .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
              .map((bill) => (
                <TableRow key={bill.id} hover>
                  <TableCell>{bill.id}</TableCell>
                  <TableCell>{bill.deliveryBillName}</TableCell>
                  <TableCell>{bill.toCustomerName || '-'}</TableCell>
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
              ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        component="div"
        count={deliveryBills.length}
        page={page}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
        labelRowsPerPage="Số hàng mỗi trang:"
        labelDisplayedRows={({ from, to, count }) => `${from}-${to} của ${count}`}
      />
    </Box>
  );
};

export default DeliveryBillsPanel;