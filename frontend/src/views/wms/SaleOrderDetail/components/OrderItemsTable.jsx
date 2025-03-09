import React from "react";
import { 
  TableContainer, 
  Table, 
  TableHead, 
  TableBody, 
  TableRow, 
  TableCell, 
  Paper,
  useTheme,
  useMediaQuery
} from "@mui/material";

const OrderItemsTable = ({ items }) => {
  console.log("Items in OrderItemsTable: ", items);
  const theme = useTheme();
  const isSmall = useMediaQuery(theme.breakpoints.down("sm"));

  return (
    <TableContainer component={Paper}>
      <Table size={isSmall ? "small" : "medium"}>
        <TableHead>
          <TableRow>
            <TableCell>STT</TableCell>
            <TableCell>Mã sản phẩm</TableCell>
            <TableCell>Tên sản phẩm</TableCell>
            <TableCell>Đơn vị</TableCell>
            <TableCell>Số lượng</TableCell>
            <TableCell>Giá bán lẻ</TableCell>
            <TableCell>Đơn giá (trước VAT)</TableCell>
            <TableCell>Đơn giá (sau VAT)</TableCell>
            <TableCell>Thành tiền</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {items.map((item) => (
            <TableRow key={item.stt}>
              <TableCell>{item.stt}</TableCell>
              <TableCell>{item.id}</TableCell>
              <TableCell>{item.name}</TableCell>
              <TableCell>{item.unit}</TableCell>
              <TableCell>{item.quantity}</TableCell>
              <TableCell>{item.price}</TableCell>
              <TableCell>{item.preBefore}</TableCell>
              <TableCell>{item.preAfter}</TableCell>
              <TableCell>{item.total}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default React.memo(OrderItemsTable);