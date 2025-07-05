import React from "react";
import { Box ,Card, CardHeader, CardContent, TableContainer, Paper, Table, TableBody, TableRow, TableCell, Typography, CircularProgress, Chip } from "@mui/material";
import WarehouseIcon from "@mui/icons-material/Warehouse";
import { useTheme } from "@mui/material/styles";

const InventoryTable = ({ inventory, loading }) => {
  const theme = useTheme();
  const getInventoryStatusChip = (statusId) => {
    switch (statusId) {
      case "VALID":
        return <Chip label="Còn hạn" color="success" />;
      case "EXPIRED":
        return <Chip label="Hết hạn" color="error" />;
      default:
        return <Chip label={statusId || "N/A"} />;
    }
  };
  return (
    <Card elevation={2}>
      <CardHeader
        title={
          <span style={{ display: "flex", alignItems: "center" }}>
            <WarehouseIcon style={{ marginRight: 8, color: theme.palette.primary.main }} />
            <Typography variant="h6">Tồn kho theo sản phẩm</Typography>
          </span>
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
                  <TableCell colSpan={7} sx={{ backgroundColor: theme.palette.action.hover }}>
                    <Typography fontWeight="medium">Tổng số dòng: {inventory.length}</Typography>
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell><b>Kho</b></TableCell>
                  <TableCell><b>Số lượng</b></TableCell>
                  <TableCell><b>Lô</b></TableCell>
                  <TableCell><b>Ngày sản xuất</b></TableCell>
                  <TableCell><b>Hạn dùng</b></TableCell>
                  <TableCell><b>Trạng thái</b></TableCell>
                  <TableCell><b>Ngày nhập</b></TableCell>
                </TableRow>
                {inventory.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">Không có tồn kho</TableCell>
                  </TableRow>
                ) : (
                  inventory.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>{item.facilityName}</TableCell>
                      <TableCell>{item.quantity}</TableCell>
                      <TableCell>{item.lotId || "—"}</TableCell>
                      <TableCell>{item.manufacturingDate || "—"}</TableCell>
                      <TableCell>{item.expirationDate || "—"}</TableCell>
                      <TableCell>{getInventoryStatusChip(item.statusId)}</TableCell>
                      <TableCell>{item.receivedDate || "—"}</TableCell>
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

export default InventoryTable;