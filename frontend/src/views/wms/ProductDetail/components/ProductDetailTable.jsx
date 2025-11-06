import React from "react";
import { Card, CardHeader, CardContent, TableContainer, Paper, Table, TableBody, TableRow, TableCell, Typography } from "@mui/material";
import InfoIcon from "@mui/icons-material/Info";
import { useTheme } from "@mui/material/styles";
import {formatCurrency} from "../../common/utils/functions";

const ProductDetailTable = ({ product }) => {
  const theme = useTheme();
  return (
    <Card elevation={2}>
      <CardHeader
        title={
          <span style={{ display: "flex", alignItems: "center" }}>
            <InfoIcon style={{ marginRight: 8, color: theme.palette.primary.main }} />
            <Typography variant="h6">Thông tin chi tiết</Typography>
          </span>
        }
        sx={{
          backgroundColor: theme.palette.background.default,
          borderBottom: `1px solid ${theme.palette.divider}`,
        }}
      />
      <CardContent>
        <TableContainer component={Paper} elevation={0}>
          <Table>
            <TableBody sx={{ "& tr:nth-of-type(even)": { backgroundColor: theme.palette.action.hover } }}>
              <TableRow>
                <TableCell width="30%" sx={{ borderLeft: `3px solid ${theme.palette.primary.light}`, backgroundColor: theme.palette.background.default }}>
                  <Typography fontWeight="medium">Đơn vị tính</Typography>
                </TableCell>
                <TableCell>{product.unit || "—"}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ borderLeft: `3px solid ${theme.palette.primary.light}`, backgroundColor: theme.palette.background.default }}>
                  <Typography fontWeight="medium">Khối lượng</Typography>
                </TableCell>
                <TableCell>{product.weight ? `${product.weight} kg` : "—"}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ borderLeft: `3px solid ${theme.palette.primary.light}`, backgroundColor: theme.palette.background.default }}>
                  <Typography fontWeight="medium">Chiều cao</Typography>
                </TableCell>
                <TableCell>{product.height ? `${product.height} cm` : "—"}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ borderLeft: `3px solid ${theme.palette.primary.light}`, backgroundColor: theme.palette.background.default }}>
                  <Typography fontWeight="medium">Giá nhập</Typography>
                </TableCell>
                <TableCell>
                  <Typography fontWeight="medium" color="text.primary">
                    {formatCurrency(product.costPrice)}
                  </Typography>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ borderLeft: `3px solid ${theme.palette.primary.light}`, backgroundColor: theme.palette.background.default }}>
                  <Typography fontWeight="medium">Giá bán lẻ</Typography>
                </TableCell>
                <TableCell>
                  <Typography fontWeight="medium" color="success.main">
                    {formatCurrency(product.retailPrice)}
                  </Typography>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ borderLeft: `3px solid ${theme.palette.primary.light}`, backgroundColor: theme.palette.background.default }}>
                  <Typography fontWeight="medium">Giá bán buôn</Typography>
                </TableCell>
                <TableCell>
                  <Typography fontWeight="medium" color="info.main">
                    {formatCurrency(product.wholeSalePrice)}
                  </Typography>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ borderLeft: `3px solid ${theme.palette.primary.light}`, backgroundColor: theme.palette.background.default }}>
                  <Typography fontWeight="medium">Thuế VAT</Typography>
                </TableCell>
                <TableCell>
                  <Typography fontWeight="medium" color="info.main">
                    {`${product.vatRate}%`}
                  </Typography>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>
    </Card>
  );
};

export default ProductDetailTable;