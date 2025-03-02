import React from "react";
import {
  Box,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Checkbox,
  FormControlLabel,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Stack,
  Grid,
  Typography,
  TextareaAutosize,
} from "@mui/material";
import RequireField from "../common-components/RequireField";

const CreateSaleOrder = () => {
  return (
    <Box p={3}>
      <Typography sx={{fontWeight: 700}} variant="h5" gutterBottom>
        Tạo đơn hàng bán
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Box sx={{ mb: 3 }}>
            <Grid container spacing={2}>
              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Kênh bán hàng: <RequireField />
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <FormControl fullWidth size="small">
                  <Select defaultValue="">
                    <MenuItem value="">Kênh phân phối điểm bán</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Mã kho hàng:
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <FormControl fullWidth size="small">
                  <Select defaultValue="">
                    <MenuItem value="">KHO MAC THỊ BƯỞI</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Khách hàng: <RequireField />
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <TextField fullWidth placeholder="Tìm kiếm" size="small" />
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Giao sau ngày: <RequireField />
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <TextField
                  fullWidth
                  type="date"
                  size="small"
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Số hóa đơn (TC):
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <TextField fullWidth size="small" />
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Loại chiết khấu:
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <FormControl fullWidth size="small">
                  <Select defaultValue="">
                    <MenuItem value="">Chọn loại</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Giá trị chiết khấu:
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <TextField fullWidth size="small" />
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Bảng giá:
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <FormControl fullWidth size="small">
                  <Select defaultValue="">
                    <MenuItem value="">Chọn bảng giá</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

     
            </Grid>
          </Box>
        </Grid>

        <Grid item xs={12} md={6}>
          <Box sx={{ mb: 3 }}>
            <Grid container spacing={2}>
              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Địa chỉ nhận:{" "}
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <TextField fullWidth size="small" />
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Số điện thoại nhận:{" "}
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <TextField fullWidth size="small" />
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Phương thức vận chuyển:
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <FormControl fullWidth size="small">
                  <Select defaultValue="">
                    <MenuItem value="">Standard</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Đơn vị vận chuyển
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <FormControl fullWidth size="small">
                  <Select defaultValue="">
                    <MenuItem value="">VNPOST</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Mục đích đơn hàng:
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <FormControl fullWidth size="small">
                  <Select defaultValue="">
                    <MenuItem value="">Bán hàng</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>
                  Ghi chú:
                </Typography>
              </Grid>
              <Grid item xs={8}>
                <TextareaAutosize
                  minRows={3} 
                  maxRows={6} 
                  style={{ 
                    width: "100%", 
                    padding: "8.5px 14px",
                    border: "1px solid rgba(0, 0, 0, 0.23)",
                    borderRadius: "4px",
                    fontFamily: "inherit",
                    fontSize: "1rem",
                    resize: "vertical"
                  }}
                  placeholder="Nhập ghi chú..." />
              </Grid>

              <Grid item xs={4}>
                <Typography variant="body1" sx={{ pt: 1 }}>Xuất hóa đơn VAT</Typography>
              </Grid>
              <Grid item xs={8}>
                <FormControlLabel control={<Checkbox />}/>
              </Grid>
            </Grid>
          </Box>
        </Grid>
      </Grid>

      <Box mt={3}>
        <TextField fullWidth label="Tìm sản phẩm" size="small" />
        <TableContainer component={Paper} sx={{ mt: 2 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>STT</TableCell>
                <TableCell>Mã sản phẩm</TableCell>
                <TableCell>Tên sản phẩm</TableCell>
                <TableCell>Đơn vị</TableCell>
                <TableCell>Giá sàn</TableCell>
                <TableCell>Giá bán lẻ</TableCell>
                <TableCell>Giá bao gồm thuế</TableCell>
                <TableCell>Số lượng</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                <TableCell colSpan={8} align="center">
                  Chưa có dữ liệu
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>
      </Box>

      <Box mt={3} textAlign="right">
        <Button variant="contained" color="primary">
          Lưu
        </Button>
      </Box>
    </Box>
  );
};

export default CreateSaleOrder;
