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
import OutBoundListRow from "./OutBoundListRow";

const OutBoundTable = ({ items = [] }) => {
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
            <TableCell>Mã đơn xuất</TableCell>
            <TableCell>Tên đơn hàng</TableCell>
            <TableCell>Khách hàng</TableCell>
            <TableCell>Địa chỉ giao</TableCell>
            <TableCell>Số điện thoại</TableCell>
            <TableCell>Ngày giao</TableCell>
            <TableCell>Trạng thái</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {items.map((item) => (
            <OutBoundListRow
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

export default React.memo(OutBoundTable);