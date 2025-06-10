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
import EditIcon from "@mui/icons-material/Edit";
import {formatCurrency} from "../../common/utils/functions";

const ProductTable = ({
  products,
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
              <TableCell>Mã sản phẩm</TableCell>
              <TableCell>Tên sản phẩm</TableCell>
              <TableCell>Đơn vị</TableCell>
              <TableCell>Giá nhập</TableCell>
              <TableCell>Giá bán lẻ</TableCell>
              <TableCell>Giá bán buôn</TableCell>
              <TableCell>Danh mục</TableCell>
              <TableCell>Trạng thái</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={10} align="center">
                  <Box display="flex" justifyContent="center" alignItems="center" py={2}>
                    <CircularProgress size={24} />
                    <Typography variant="body2" sx={{ ml: 1 }}>
                      Đang tải...
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ) : products.length === 0 ? (
              <TableRow>
                <TableCell colSpan={10} align="center">
                  <Typography variant="body1" py={2}>Không có dữ liệu</Typography>
                </TableCell>
              </TableRow>
            ) : (
              products.map((product, index) => (
                <TableRow hover key={product.id} onClick={() => onViewDetail(product.id)} sx={{ cursor: 'pointer' }}>
                  <TableCell>
                    {pagination.page * pagination.size + index + 1}
                  </TableCell>
                  <TableCell>{product.id}</TableCell>
                  <TableCell>{product.name}</TableCell>
                  <TableCell>{product.unit || "—"}</TableCell>
                  <TableCell>{formatCurrency(product.costPrice)}</TableCell>
                  <TableCell>{formatCurrency(product.retailPrice)}</TableCell>
                  <TableCell>{formatCurrency(product.wholeSalePrice)}</TableCell>
                  <TableCell>
                    {product.category ? product.category.name : "—"}
                  </TableCell>
                  <TableCell>{getStatusChip(product.statusId)}</TableCell>
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

export default ProductTable;