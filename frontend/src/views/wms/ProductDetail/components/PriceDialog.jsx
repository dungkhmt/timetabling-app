import React, { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  CircularProgress,
  Box,
  Typography,
  InputAdornment,
  Stack,
} from "@mui/material";
import MonetizationOnIcon from "@mui/icons-material/MonetizationOn";
import CalendarMonthIcon from "@mui/icons-material/CalendarMonth";
import DescriptionIcon from "@mui/icons-material/Description";
import { toast } from "react-toastify";

const PriceDialog = ({ open, onClose, onSubmit, loading }) => {
  const [price, setPrice] = useState("");
  const [description, setDescription] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  const handleSubmit = () => {
    if (!price || !startDate) {
      toast.error("Vui lòng nhập đầy đủ giá và ngày bắt đầu");
      return;
    }
    onSubmit({
      price,
      description,
      startDate,
      endDate: endDate || null,
    });
  };

  useEffect(() => {
    if (!open) {
      setPrice("");
      setDescription("");
      setStartDate("");
      setEndDate("");
    }
  }, [open]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <MonetizationOnIcon color="success" />
          <Typography variant="h6" component="span">
            Thiết lập giá bán mới
          </Typography>
        </Box>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
          Nhập thông tin giá bán áp dụng cho sản phẩm
        </Typography>
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2} mt={1}>
          <TextField
            label="Giá bán"
            type="number"
            fullWidth
            value={price}
            onChange={e => setPrice(e.target.value)}
            InputProps={{
              startAdornment: <InputAdornment position="start">₫</InputAdornment>,
            }}
            autoFocus
            required
          />
          <TextField
            label="Mô tả"
            fullWidth
            value={description}
            onChange={e => setDescription(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <DescriptionIcon color="action" />
                </InputAdornment>
              ),
            }}
            multiline
            minRows={2}
          />
          <TextField
            label="Ngày bắt đầu"
            type="datetime-local"
            fullWidth
            value={startDate}
            onChange={e => setStartDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <CalendarMonthIcon color="primary" />
                </InputAdornment>
              ),
            }}
            required
          />
          <TextField
            label="Ngày kết thúc"
            type="datetime-local"
            fullWidth
            value={endDate}
            onChange={e => setEndDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <CalendarMonthIcon color="action" />
                </InputAdornment>
              ),
            }}
          />
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} variant="outlined" color="inherit">
          Hủy
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          color="success"
          disabled={loading}
          startIcon={<MonetizationOnIcon />}
        >
          {loading ? <CircularProgress size={20} color="inherit" /> : "Lưu"}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default PriceDialog;