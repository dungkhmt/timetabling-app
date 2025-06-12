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
  useMediaQuery,
  Box,
  Typography,
} from "@mui/material";
import InBoundListRow from "./InBoundListRow";

const InBoundTable = ({ items = [] }) => {
  const theme = useTheme();
  const isSmall = useMediaQuery(theme.breakpoints.down("sm"));

  // Kiểm tra nếu không có dữ liệu
  if (!items || items.length === 0) {
    return (
      <Box sx={{ p: 3, textAlign: "center" }}>
        <Typography color="text.secondary">Không có đơn hàng nào</Typography>
      </Box>
    );
  }

  return (
    <TableContainer component={Paper} variant="outlined">
      <Table size={isSmall ? "small" : "medium"}>
        <TableHead sx={{ bgcolor: "action.hover" }}>
          <TableRow>
            <TableCell>Mã đơn nhập</TableCell>
            <TableCell>Tên phiếu nhập</TableCell>
            <TableCell>Nhà cung cấp</TableCell>
            <TableCell>Ngày giao dự kiến</TableCell>
            <TableCell>Tổng khối lượng</TableCell>
            <TableCell>Tổng số lượng</TableCell>
            <TableCell>Trạng thái</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {items.map((item) => (
            <InBoundListRow
              key={item.id}
              row={item}
              isSmall={isSmall}
            />
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default React.memo(InBoundTable);