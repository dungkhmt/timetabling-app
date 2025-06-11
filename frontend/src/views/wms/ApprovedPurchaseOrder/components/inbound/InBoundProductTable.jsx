import React from "react";
import {
  TableContainer,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Paper,
  Typography,
  Box,
  useTheme,
  useMediaQuery,
} from "@mui/material";
import InBoundRow from "./InBoundRow";

const InBoundProductTable = ({
  orderItems,
  inventoryItems,
  products,
  onProductChange,
  onDeleteProduct,
  errors,
}) => {
  const theme = useTheme();
  const isSmall = useMediaQuery(theme.breakpoints.down("lg"));

  if (!orderItems || orderItems.length === 0) {
    return (
      <Box sx={{ p: 2, textAlign: "center" }}>
        <Typography color="text.secondary">Không có sản phẩm nào</Typography>
      </Box>
    );
  }

  return (
    <>
      <Box mb={2}>
        <Typography variant="subtitle2" gutterBottom fontWeight="bold">
          Danh sách sản phẩm ({orderItems.length})
        </Typography>
      </Box>
      <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 600, overflow: 'auto' }}>
        <Table size={isSmall ? "small" : "medium"} stickyHeader>
          <TableHead>
            <TableRow sx={{ bgcolor: "action.hover" }}>
              <TableCell sx={{ fontWeight: 'bold', minWidth: 100 }}>Mã SP</TableCell>
              <TableCell sx={{ fontWeight: 'bold', minWidth: 150 }}>Tên sản phẩm</TableCell>
              <TableCell align="center" sx={{ fontWeight: 'bold', minWidth: 100 }}>SL đặt</TableCell>
              <TableCell sx={{ fontWeight: 'bold', minWidth: 180 }}>Kho nhập</TableCell>
              <TableCell sx={{ fontWeight: 'bold', minWidth: 100 }}>SL nhập</TableCell>
              <TableCell sx={{ fontWeight: 'bold', minWidth: 120 }}>Lô hàng</TableCell>
              <TableCell sx={{ fontWeight: 'bold', minWidth: 140 }}>Ngày SX</TableCell>
              <TableCell sx={{ fontWeight: 'bold', minWidth: 140 }}>Ngày HH</TableCell>
              <TableCell sx={{ fontWeight: 'bold', minWidth: 80 }}>Thao tác</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {orderItems.map((item) => {
              const product = products.find((p) => p.orderItemId === item.id);
              if (!product) return null;
              
              return (
                <InBoundRow
                  key={item.id}
                  orderItem={item}
                  inventoryItems={inventoryItems}
                  selectedFacilityId={product.facilityId}
                  selectedQuantity={product.quantity}
                  lotId={product.lotId}
                  manufacturingDate={product.manufacturingDate}
                  expirationDate={product.expirationDate}
                  onFacilityChange={(value) =>
                    onProductChange(item.id, "facilityId", value)
                  }
                  onQuantityChange={(value) =>
                    onProductChange(item.id, "quantity", value)
                  }
                  onLotIdChange={(value) =>
                    onProductChange(item.id, "lotId", value)
                  }
                  onManufacturingDateChange={(value) =>
                    onProductChange(item.id, "manufacturingDate", value)
                  }
                  onExpirationDateChange={(value) =>
                    onProductChange(item.id, "expirationDate", value)
                  }
                  onDeleteProduct={onDeleteProduct}
                  errors={errors}
                />
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
};

export default InBoundProductTable;