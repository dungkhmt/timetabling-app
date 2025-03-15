import React from "react";
import {
  TableRow,
  TableCell,
  FormControl,
  Select,
  MenuItem,
  InputLabel,
  TextField,
  Typography,
} from "@mui/material";
import WarningIcon from "@mui/icons-material/Warning";

const OutboundRow = ({
  orderItem,
  inventoryItems,
  selectedInventoryItemId,
  selectedQuantity,
  onInventoryItemChange,
  onQuantityChange,
  errors,
}) => {
  const warehouseError = errors[`warehouse_${orderItem.orderItemSeqId}`];
  const quantityError = errors[`quantity_${orderItem.orderItemSeqId}`];

  // Lọc inventoryItems để chỉ hiển thị các kho chứa productId của orderItem
  const filteredInventoryItems = inventoryItems.filter(
    (inventory) => inventory.productId === orderItem.productId
  );

  const selectedInventory = filteredInventoryItems.find(
    (inv) => inv.id === selectedInventoryItemId
  );
  const availableQuantity = selectedInventory?.quantity || 0;
  const isInventoryInsufficient = availableQuantity < orderItem.quantity;

  const handleWarehouseChange = (event) => {
    onInventoryItemChange(event.target.value);
  };

  const handleQuantityChange = (event) => {
    const newQuantity = parseInt(event.target.value, 10) || 0;
    const maxQuantity = Math.min(orderItem.quantity, availableQuantity);
    const validQuantity = Math.max(0, Math.min(newQuantity, maxQuantity));
    onQuantityChange(validQuantity);
  };

  return (
    <TableRow sx={{ "&:hover": { backgroundColor: "action.hover" } }}>
      <TableCell>{orderItem.productId}</TableCell>
      <TableCell>{orderItem.productName}</TableCell>
      <TableCell align="center">{orderItem.quantity}</TableCell>
      <TableCell>
        <FormControl size="small" fullWidth sx={{ minWidth: 150 }} error={!!warehouseError}>
          <InputLabel id={`warehouse-select-${orderItem.orderItemSeqId}`}>
            Chọn kho
          </InputLabel>
          <Select
            labelId={`warehouse-select-${orderItem.orderItemSeqId}`}
            value={selectedInventoryItemId || ""}
            onChange={handleWarehouseChange}
            label="Chọn kho"
          >
            {filteredInventoryItems.length === 0 ? (
              <MenuItem disabled>Không có kho khả dụng</MenuItem>
            ) : (
              filteredInventoryItems.map((inventory) => (
                <MenuItem key={inventory.id} value={inventory.id}>
                  {inventory.facilityName} ({inventory.quantity} trong kho)
                </MenuItem>
              ))
            )}
          </Select>
          {warehouseError && (
            <Typography variant="caption" color="error">{warehouseError}</Typography>
          )}
        </FormControl>
        {isInventoryInsufficient && (
          <Typography
            variant="caption"
            color="warning.main"
            sx={{ display: "flex", alignItems: "center", mt: 1, gap: 0.5 }}
          >
            <WarningIcon fontSize="inherit" />
            Chỉ còn {availableQuantity} sản phẩm
          </Typography>
        )}
      </TableCell>
      <TableCell>
        <TextField
          type="number"
          size="small"
          value={selectedQuantity}
          onChange={handleQuantityChange}
          disabled={!selectedInventoryItemId}
          inputProps={{
            min: 0,
            max: Math.min(orderItem.quantity, availableQuantity),
            style: { textAlign: "center" },
          }}
          sx={{ width: 80 }}
          error={!!quantityError}
        />
        {quantityError && (
          <Typography variant="caption" color="error">{quantityError}</Typography>
        )}
      </TableCell>
    </TableRow>
  );
};

export default OutboundRow;