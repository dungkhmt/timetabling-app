import React, { useState, useEffect } from "react";
import {
  TableRow,
  TableCell,
  IconButton,
  Tooltip,
  FormControl,
  Select,
  MenuItem,
  Button,
  Stack,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Typography,
  Box,
} from "@mui/material";
import VisibilityIcon from "@mui/icons-material/Visibility";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import AddIcon from "@mui/icons-material/Add";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";

const OutboundRow = ({
  row,
  facilities,
  onSplitRow,
  updateRowQuantity,
  onExport,
  isSmall,
}) => {
  const [selectedWarehouse, setSelectedWarehouse] = useState("");
  const [warehouseInventory, setWarehouseInventory] = useState(0);
  const [isInventoryInsufficient, setIsInventoryInsufficient] = useState(false);
  const [openSplitDialog, setOpenSplitDialog] = useState(false);
  const [splitQuantity, setSplitQuantity] = useState(0);
  
  // Reset selected warehouse when row changes
  useEffect(() => {
    setSelectedWarehouse("");
    setIsInventoryInsufficient(false);
  }, [row.id, row.rowId]);

  // Xử lý chọn kho cho phiếu
  const handleWarehouseChange = (event) => {
    const warehouseId = event.target.value;
    setSelectedWarehouse(warehouseId);

    // Lấy thông tin kho từ danh sách facilities
    const warehouse = facilities.find((w) => w.id === warehouseId);
    
    // Lấy số lượng tồn kho của sản phẩm trong kho này
    // Trong thực tế, bạn nên gọi API để lấy số lượng tồn kho chính xác
    const inventoryAmount = warehouse?.inventory?.[row.productId] || 0;
    setWarehouseInventory(inventoryAmount);

    // Kiểm tra xem số lượng tồn kho có đủ không
    setIsInventoryInsufficient(inventoryAmount < row.remainingQuantity);
  };

  // Mở dialog tách dòng
  const handleOpenSplitDialog = () => {
    // Đặt giá trị mặc định cho số lượng tách là số lượng hiện có trong kho
    setSplitQuantity(Math.min(warehouseInventory, row.remainingQuantity));
    setOpenSplitDialog(true);
  };

  // Đóng dialog tách dòng
  const handleCloseSplitDialog = () => {
    setOpenSplitDialog(false);
  };

  // Xử lý tách dòng
  const handleSplitConfirm = () => {
    if (splitQuantity > 0 && splitQuantity <= row.remainingQuantity) {
      onSplitRow(row.rowId, {
        ...row,
        warehouseId: selectedWarehouse,
        quantity: splitQuantity
      });
      setOpenSplitDialog(false);
    }
  };

  // Xử lý nút xuất kho
  const handleExport = () => {
    if (!selectedWarehouse) {
      alert("Vui lòng chọn kho xuất!");
      return;
    }

    if (isInventoryInsufficient) {
      alert(
        "Số lượng trong kho không đủ! Vui lòng tách dòng hoặc chọn kho khác."
      );
      return;
    }

    // Gọi hàm xuất kho từ props
    onExport(row.rowId, {
      productId: row.productId,
      warehouseId: selectedWarehouse,
      quantity: row.remainingQuantity
    });
  };

  // Trạng thái đã được render thành màu sắc
  const getStatusChip = (status) => {
    let color = "default";

    switch (status?.toLowerCase()) {
      case "đã duyệt":
      case "completed":
      case "approved":
        color = "success";
        break;
      case "chờ duyệt":
      case "pending":
        color = "warning";
        break;
      case "đã hủy":
      case "cancelled":
        color = "error";
        break;
      case "đang xử lý":
      case "processing":
        color = "info";
        break;
      default:
        color = "default";
    }

    return (
      <Chip
        label={status || "Chờ xử lý"}
        color={color}
        size={isSmall ? "small" : "medium"}
        variant="outlined"
      />
    );
  };

  // Kiểm tra xem dòng này có thể xuất hàng không
  const canExport =
    selectedWarehouse &&
    row.remainingQuantity > 0 &&
    !isInventoryInsufficient &&
    (row.status?.toLowerCase() !== "đã hủy" &&
     row.status?.toLowerCase() !== "cancelled");

  // Kiểm tra trạng thái để cho phép thao tác hay không
  const isActionDisabled =
    row.status?.toLowerCase() === "đã hủy" ||
    row.status?.toLowerCase() === "cancelled" ||
    row.remainingQuantity === 0;

  return (
    <TableRow
      sx={row.isMainRow ? {} : { backgroundColor: "rgba(0, 0, 0, 0.04)" }}
    >
      <TableCell>
        <Tooltip title={row.isMainRow ? "Xem chi tiết" : "Phiếu con"}>
          <span>
            {row.isMainRow ? (
              <IconButton size="small" color="primary" sx={{ mr: 0.5 }}>
                <VisibilityIcon fontSize="small" />
              </IconButton>
            ) : null}
            {row.productCode || row.id} {!row.isMainRow && `(Tách)`}
          </span>
        </Tooltip>
      </TableCell>
      <TableCell>{getStatusChip(row.status)}</TableCell>
      <TableCell>{row.unit || "Chiếc"}</TableCell>
      <TableCell>
        {row.splitQuantity !== row.remainingQuantity ? (
          <span>
            {row.remainingQuantity}/{row.splitQuantity}
          </span>
        ) : (
          row.remainingQuantity
        )}
      </TableCell>
      <TableCell>{row.deliveryDate || "N/A"}</TableCell>
      <TableCell>
        <FormControl size="small" fullWidth sx={{ minWidth: 150 }}>
          <Select
            value={selectedWarehouse}
            onChange={handleWarehouseChange}
            displayEmpty
            disabled={isActionDisabled}
          >
            <MenuItem value="" disabled>
              <em>Chọn kho xuất</em>
            </MenuItem>
            {facilities.map((warehouse) => (
              <MenuItem key={warehouse.id} value={warehouse.id}>
                {warehouse.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        {isInventoryInsufficient && (
          <Typography
            variant="caption"
            color="error"
            sx={{ display: "block", mt: 1 }}
          >
            Kho chỉ còn {warehouseInventory} sản phẩm
          </Typography>
        )}
      </TableCell>
      <TableCell>
        <Stack direction="row" spacing={1}>
          {isInventoryInsufficient && row.remainingQuantity > 0 && (
            <Button
              variant="outlined"
              color="warning"
              size="small"
              startIcon={<AddIcon />}
              onClick={handleOpenSplitDialog}
            >
              Tách
            </Button>
          )}

          {canExport && (
            <Button
              variant="contained"
              color="success"
              size="small"
              startIcon={<CheckCircleIcon />}
              onClick={handleExport}
            >
              Xuất
            </Button>
          )}

          {row.isMainRow && !isActionDisabled && (
            <>
              <IconButton size="small" color="secondary">
                <EditIcon fontSize="small" />
              </IconButton>
              <IconButton size="small" color="error">
                <DeleteIcon fontSize="small" />
              </IconButton>
            </>
          )}
        </Stack>
      </TableCell>

      {/* Dialog xác nhận tách dòng */}
      <Dialog open={openSplitDialog} onClose={handleCloseSplitDialog}>
        <DialogTitle>Tách phiếu xuất kho</DialogTitle>
        <DialogContent>
          <Box my={2}>
            <Typography variant="body2">
              Kho <b>{facilities.find(f => f.id === selectedWarehouse)?.name}</b> chỉ còn <b>{warehouseInventory}</b> sản phẩm, 
              không đủ số lượng yêu cầu ({row.remainingQuantity}).
            </Typography>
            <Typography variant="body2" sx={{ mt: 1 }}>
              Bạn có thể tách phiếu để xuất một phần từ kho này.
            </Typography>
          </Box>
          <TextField
            autoFocus
            margin="dense"
            label="Số lượng xuất từ kho này"
            type="number"
            fullWidth
            variant="outlined"
            value={splitQuantity}
            onChange={(e) => {
              const value = parseInt(e.target.value);
              if (isNaN(value) || value <= 0) {
                setSplitQuantity(1);
              } else if (value > Math.min(warehouseInventory, row.remainingQuantity)) {
                setSplitQuantity(Math.min(warehouseInventory, row.remainingQuantity));
              } else {
                setSplitQuantity(value);
              }
            }}
            inputProps={{
              min: 1,
              max: Math.min(warehouseInventory, row.remainingQuantity),
            }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseSplitDialog}>Hủy</Button>
          <Button onClick={handleSplitConfirm} color="primary">
            Xác nhận
          </Button>
        </DialogActions>
      </Dialog>
    </TableRow>
  );
};

export default React.memo(OutboundRow);
