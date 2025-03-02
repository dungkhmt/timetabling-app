import React, { useState } from "react";
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  Tabs,
  Tab,
  Stack,
  Grid,
  useTheme,
  useMediaQuery,
  Card,
  CardContent,
  Divider,
} from "@mui/material";
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import CancelIcon from '@mui/icons-material/Cancel';
import EditIcon from '@mui/icons-material/Edit';
import PercentIcon from '@mui/icons-material/Percent';
import { CheckOutlined, DeleteOutlined } from "@mui/icons-material";

const OrderDetail = () => {
  const [tabValue, setTabValue] = useState(0);
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const isSmall = useMediaQuery(theme.breakpoints.down("sm"));

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  // Style for information item with even spacing
  const infoItemStyle = {
    display: 'grid',
    gridTemplateColumns: '35% 65%',
    gap: '8px',
    alignItems: 'center',
  };

  return (
    <Box p={isMobile ? 1 : 3}>
      <Stack spacing={2} direction={isMobile ? "column" : "row"} justifyContent={isMobile ? "center" : "space-between"}> 
        <Typography
          variant="h5"
          fontWeight="bold"
          sx={{ mb: 2, fontSize: isMobile ? "1.2rem" : "1.5rem" }}
        >
          Chi tiết đơn hàng bán: ORD108874
        </Typography>

        <Box mt={3}>
          <Stack
            direction={isMobile ? "column" : "row"}
            spacing={1}
            justifyContent="space-between"
            width="100%"
          >
            <Button 
              variant="outlined" 
              color="primary" 
              fullWidth={isMobile}
              startIcon={<CheckOutlined />}
            >
              Duyệt
            </Button>
            <Button 
              variant="outlined" 
              color="error" 
              fullWidth={isMobile}
              startIcon={<DeleteOutlined />}
            >
              Hủy bỏ
            </Button>
            <Button 
              variant="outlined" 
              color="info" 
              fullWidth={isMobile}
              startIcon={<EditIcon />}
            >
              Chỉnh sửa
            </Button>
            <Button 
              variant="outlined" 
              color="secondary" 
              fullWidth={isMobile}
              startIcon={<PercentIcon />}
            >
              Chiết khấu
            </Button>
          </Stack>
        </Box>
      </Stack>

      <Box sx={{ borderBottom: 1, borderColor: "divider", mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          aria-label="order details tabs"
          variant={isMobile ? "scrollable" : "standard"}
          scrollButtons={isMobile ? "auto" : false}
        >
          <Tab label="Tổng quan" />
          <Tab label="Thông tin chung" />
          <Tab label="Thanh toán" />
          <Tab label="Sản phẩm" />
        </Tabs>
      </Box>

      <Grid container spacing={2} mt={1}>
        <Grid item xs={12} md={6}>
          <Card variant="outlined" sx={{ height: "100%" }}>
            <CardContent>
              <Stack spacing={1.5}>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Mã đơn hàng:</Typography>
                  <Typography>ORD108874</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Số hóa đơn (TC):</Typography>
                  <Typography>-</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Mục đích đơn hàng bán:</Typography>
                  <Typography>Bán hàng</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Trạng thái:</Typography>
                  <Typography>Mới tạo</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Kênh bán hàng:</Typography>
                  <Typography>Kênh phân phối điểm bán (KENH_DIEMBAN)</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Kho hàng:</Typography>
                  <Typography>KHO MẠC THỊ BƯỞI (KHO01)</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Khách hàng:</Typography>
                  <Typography>76 ĐƯỜNG PHẦN LAN - 0977567968</Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card variant="outlined" sx={{ height: "100%" }}>
            <CardContent>
              <Stack spacing={1.5}>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Địa chỉ:</Typography>
                  <Typography>76 ĐƯỜNG PHẦN LAN, Hà Nội, Việt Nam</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Số Điện Thoại:</Typography>
                  <Typography>0977567968</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Độ ưu tiên:</Typography>
                  <Typography>5</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Ngày giao hàng:</Typography>
                  <Typography>11/02/2025</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Ngày tạo đơn:</Typography>
                  <Typography>11/02/2025 21:08:02</Typography>
                </Box>
                <Box sx={infoItemStyle}>
                  <Typography fontWeight="500">Ghi chú:</Typography>
                  <Typography>-</Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Box mt={3}>
        <Typography variant="h6" sx={{ mb: 2 }}>
          Đơn hàng
        </Typography>
        {isMobile ? (
          <Stack spacing={2}>
            {[
              {
                stt: "01",
                id: "SP001",
                name: "Bia Saigon Gold Ion (18lon/th)",
                unit: "Thùng",
                quantity: 7,
                price: "380.000 ₫",
                preBefore: "380.000 ₫",
                preAfter: "380.000 ₫",
                total: "2.660.000 ₫",
              },
              {
                stt: "02",
                id: "SP010",
                name: "Bia Saigon Export Ion (24lon/th)",
                unit: "Thùng",
                quantity: 14,
                price: "272.000 ₫",
                preBefore: "272.000 ₫",
                preAfter: "272.000 ₫",
                total: "3.808.000 ₫",
              },
            ].map((item, index) => (
              <Card key={index} variant="outlined">
                <CardContent>
                  <Typography variant="subtitle1" fontWeight="bold">
                    {item.stt}. {item.name}
                  </Typography>
                  <Divider sx={{ my: 1 }} />
                  <Grid container spacing={1}>
                    <Grid item xs={6}>
                      <Typography variant="body2">
                        <strong>Mã SP:</strong> {item.id}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2">
                        <strong>Đơn vị:</strong> {item.unit}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2">
                        <strong>Số lượng:</strong> {item.quantity}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2">
                        <strong>Giá bán lẻ:</strong> {item.price}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2">
                        <strong>Trước VAT:</strong> {item.preBefore}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2">
                        <strong>Sau VAT:</strong> {item.preAfter}
                      </Typography>
                    </Grid>
                    <Grid item xs={12}>
                      <Typography
                        variant="body1"
                        fontWeight="bold"
                        align="right"
                      >
                        Thành tiền: {item.total}
                      </Typography>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            ))}
          </Stack>
        ) : (
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
                <TableRow>
                  <TableCell>01</TableCell>
                  <TableCell>SP001</TableCell>
                  <TableCell>Bia Saigon Gold Ion (18lon/th)</TableCell>
                  <TableCell>Thùng</TableCell>
                  <TableCell>7</TableCell>
                  <TableCell>380.000 ₫</TableCell>
                  <TableCell>380.000 ₫</TableCell>
                  <TableCell>380.000 ₫</TableCell>
                  <TableCell>2.660.000 ₫</TableCell>
                </TableRow>
                <TableRow>
                  <TableCell>02</TableCell>
                  <TableCell>SP010</TableCell>
                  <TableCell>Bia Saigon Export Ion (24lon/th)</TableCell>
                  <TableCell>Thùng</TableCell>
                  <TableCell>14</TableCell>
                  <TableCell>272.000 ₫</TableCell>
                  <TableCell>272.000 ₫</TableCell>
                  <TableCell>272.000 ₫</TableCell>
                  <TableCell>3.808.000 ₫</TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </TableContainer>
        )}
        <Box mt={2}>
          <Typography align="right" fontWeight="bold">
            Tổng số lượng: 21
          </Typography>
          <Typography align="right" fontWeight="bold">
            Tổng đơn: 6.468.000 ₫
          </Typography>
        </Box>
      </Box>
    </Box>
  );
};

export default OrderDetail;