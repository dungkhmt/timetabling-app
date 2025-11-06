import React from "react";
import {
    Box,
    Chip,
    CircularProgress,
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
import {format} from "date-fns";
import {useHistory} from "react-router-dom";
import {SHIPMENT_TYPE_ID} from "../../common/constants/constants";

const ShipmentTable = ({
  shipmentTypeId,
  shipments,
  loading,
  pagination,
  onChangePage,
  onChangeRowsPerPage
}) => {
  const history = useHistory();

  // View shipment details
  const handleViewShipment = shipmentId => {
    const baseRoute = shipmentTypeId === SHIPMENT_TYPE_ID.INBOUND 
      ? "/wms/logistics/purchaseshipment" 
      : "/wms/logistics/saleshipment";
    history.push(`${baseRoute}/details/${shipmentId}`);
  };

  // Get status chip based on status ID
  const getStatusChip = statusId => {
    switch (statusId) {
      case 'CREATED':
        return <Chip size="small" label="Đã tạo" color="default" />;
      case 'PENDING':
        return <Chip size="small" label="Chờ xử lý" color="warning" />;
      case 'FULLY_TO_BE_DELIVERED':
        return <Chip size="small" label="Chờ giao hàng" color="info" />;
      case 'EXPORTED':
        return <Chip size="small" label="Đã xuất kho" color="secondary" />;
      case 'SHIPPED':
        return <Chip size="small" label="Đang vận chuyển" color="primary" />;
      case 'PARTIALLY_DELIVERED':
        return <Chip size="small" label="Giao một phần" color="secondary" />;
      case 'DELIVERED':
        return <Chip size="small" label="Đã giao hàng" color="success" />;
      case 'CANCELLED':
        return <Chip size="small" label="Đã hủy" color="error" />;
      case 'IMPORTED':
        return <Chip size="small" label="Đã nhập kho" color="success" />;
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
              <TableCell>Mã</TableCell>
              <TableCell>Tên lô hàng</TableCell>
              <TableCell>
                {shipmentTypeId === SHIPMENT_TYPE_ID.INBOUND ? "Nhà cung cấp" : "Khách hàng"}
              </TableCell>
              <TableCell>Trạng thái</TableCell>
              <TableCell>Ngày giao dự kiến</TableCell>
              <TableCell>Người tạo</TableCell>
              <TableCell>Người xử lý</TableCell>
              <TableCell>Mã đơn hàng</TableCell>
            </TableRow>
          </TableHead>
          
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={10} align="center">
                  <Box py={2} display="flex" justifyContent="center" alignItems="center">
                    <CircularProgress size={24} />
                    <Typography variant="body2" sx={{ ml: 1 }}>
                      Đang tải...
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ) : shipments.length === 0 ? (
              <TableRow>
                <TableCell colSpan={10} align="center">
                  <Typography variant="body1" py={2}>
                    Không có dữ liệu
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              shipments.map((shipment, index) => (
                <TableRow key={shipment.id} hover sx= {{ cursor: "pointer" }} onClick={() => handleViewShipment(shipment.id)}>
                  <TableCell>{pagination.page * pagination.size + index + 1}</TableCell>
                  <TableCell>{shipment.id}</TableCell>
                  <TableCell>{shipment.shipmentName}</TableCell>
                  <TableCell>{shipment.partnerName}</TableCell>
                  <TableCell>{getStatusChip(shipment.statusId)}</TableCell>
                  <TableCell>
                    {shipment.expectedDeliveryDate
                      ? format(new Date(shipment.expectedDeliveryDate), 'dd/MM/yyyy')
                      : '—'}
                  </TableCell>
                  <TableCell>{shipment.createdByUserName}</TableCell>
                  <TableCell>{shipment.handledByUserName || '—'}</TableCell>
                  <TableCell>{shipment.orderId}</TableCell>
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

export default ShipmentTable;