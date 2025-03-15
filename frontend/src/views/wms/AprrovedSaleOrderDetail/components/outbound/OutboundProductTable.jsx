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
import OutboundRow from "./OutboundRow";

const OutboundProductTable = ({
  orderItems,
  inventoryItems,
  products,
  onProductChange,
  errors,
}) => {
  const theme = useTheme();
  const isSmall = useMediaQuery(theme.breakpoints.down("sm"));

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
        <Typography variant="subtitle2" gutterBottom>
          Danh sách sản phẩm ({orderItems.length})
        </Typography>
      </Box>
      <TableContainer component={Paper} variant="outlined">
        <Table size={isSmall ? "small" : "medium"}>
          <TableHead sx={{ bgcolor: "action.hover" }}>
            <TableRow>
              <TableCell>Mã sản phẩm</TableCell>
              <TableCell>Tên sản phẩm</TableCell>
              <TableCell align="center">Số lượng đặt</TableCell>
              <TableCell>Kho xuất</TableCell>
              <TableCell>Số lượng xuất</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {orderItems.map((item) => {
              const product = products.find((p) => p.orderItemSeqId === item.orderItemSeqId);
              if (!product) return null; // Bỏ qua nếu không tìm thấy product
              return (
                <OutboundRow
                  key={item.orderItemSeqId}
                  orderItem={item}
                  inventoryItems={inventoryItems}
                  selectedInventoryItemId={product.inventoryItemId}
                  selectedQuantity={product.quantity}
                  onInventoryItemChange={(value) =>
                    onProductChange(item.orderItemSeqId, "inventoryItemId", value)
                  }
                  onQuantityChange={(value) =>
                    onProductChange(item.orderItemSeqId, "quantity", value)
                  }
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

export default OutboundProductTable;