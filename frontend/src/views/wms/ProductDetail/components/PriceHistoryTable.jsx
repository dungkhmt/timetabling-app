import React from "react";
import { Card, CardHeader, CardContent, TableContainer, Paper, Table, TableBody, TableRow, TableCell, Typography, CircularProgress, Chip, Box, Button } from "@mui/material";
import MonetizationOnIcon from "@mui/icons-material/MonetizationOn";
import { formatCurrency } from "../../common/utils/functions";
import { useTheme } from "@mui/material/styles";

const PriceHistoryTable = ({ priceHistory, loading }) => {
  const theme = useTheme();
  return (
    <Card elevation={2}>
      <CardHeader
        title={
          <Box display="flex" alignItems="center">
            <MonetizationOnIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
            <Typography variant="h6">Lịch sử điều chỉnh giá bán</Typography>
          </Box>
        }
        sx={{
          backgroundColor: theme.palette.background.default,
          borderBottom: `1px solid ${theme.palette.divider}`,
        }}
      />
      <CardContent>
        {loading ? (
          <Box display="flex" alignItems="center" justifyContent="center" minHeight={120}>
            <CircularProgress size={32} />
          </Box>
        ) : (
          <TableContainer component={Paper} elevation={0}>
            <Table>
              <TableBody>
                <TableRow>
                  <TableCell><b>Giá bán</b></TableCell>
                  <TableCell><b>Mô tả</b></TableCell>
                  <TableCell><b>Trạng thái</b></TableCell>
                  <TableCell><b>Ngày bắt đầu</b></TableCell>
                  <TableCell><b>Ngày kết thúc</b></TableCell>
                </TableRow>
                {(!priceHistory || priceHistory.length === 0) ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center">Không có lịch sử giá bán</TableCell>
                  </TableRow>
                ) : (
                  priceHistory.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>{formatCurrency(item.price)}</TableCell>
                      <TableCell>{item.description || "—"}</TableCell>
                      <TableCell>
                        <Chip
                          label={item.statusId === "ACTIVE" ? "Đang áp dụng" : "Không áp dụng"}
                          color={item.statusId === "ACTIVE" ? "success" : "default"}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{item.startDate ? new Date(item.startDate).toLocaleString() : "—"}</TableCell>
                      <TableCell>{item.endDate ? new Date(item.endDate).toLocaleString() : "—"}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </CardContent>
    </Card>
  );
};

export default PriceHistoryTable;