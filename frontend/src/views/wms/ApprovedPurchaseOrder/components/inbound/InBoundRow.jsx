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
  IconButton,
  Box,
  Tooltip
} from "@mui/material";
import { 
  Delete as DeleteIcon, 
  Warning as WarningIcon,
  Add,
  Remove
} from "@mui/icons-material";

const InBoundRow = ({
  orderItem,
  inventoryItems,
  selectedFacilityId,
  selectedQuantity,
  lotId,
  manufacturingDate,
  expirationDate,
  onFacilityChange,
  onQuantityChange,
  onLotIdChange,
  onManufacturingDateChange,
  onExpirationDateChange,
  onDeleteProduct,
  errors,
}) => {
  const warehouseError = errors[`warehouse_${orderItem.id}`];
  const quantityError = errors[`quantity_${orderItem.id}`];
  const lotError = errors[`lot_${orderItem.id}`];
  const dateError = errors[`date_${orderItem.id}`];

  // Filter inventoryItems để chỉ hiển thị các kho chứa productId của orderItem
  // Data structure đã được flatten trong CreateInBoundDialog
  const filteredInventoryItems = inventoryItems.filter(
    (inventory) => inventory.productId === orderItem.productId
  );

  const selectedInventory = filteredInventoryItems.find(
    (inv) => inv.facilityId === selectedFacilityId
  );
  const availableQuantity = selectedInventory?.quantity || 0;
  const isInventoryInsufficient = availableQuantity < orderItem.quantity;
  const maxQuantity = Math.min(orderItem.quantity, availableQuantity);

  const handleWarehouseChange = (event) => {
    onFacilityChange(event.target.value);
  };

  const handleQuantityChange = (event) => {
    const newQuantity = parseInt(event.target.value, 10) || 0;
    const validQuantity = Math.max(0, Math.min(newQuantity, maxQuantity));
    onQuantityChange(validQuantity);
  };

  const handleQuantityIncrement = () => {
    const newQuantity = Math.min((selectedQuantity || 0) + 1, maxQuantity);
    onQuantityChange(newQuantity);
  };

  const handleQuantityDecrement = () => {
    const newQuantity = Math.max((selectedQuantity || 0) - 1, 0);
    onQuantityChange(newQuantity);
  };

  const handleLotChange = (event) => {
    onLotIdChange(event.target.value);
  };

  const handleManufacturingDateChange = (event) => {
    onManufacturingDateChange(event.target.value);
  };

  const handleExpirationDateChange = (event) => {
    onExpirationDateChange(event.target.value);
  };

  // Validate expiration date is after manufacturing date
  const isDateValid = () => {
    if (!manufacturingDate || !expirationDate) return true;
    return new Date(manufacturingDate) <= new Date(expirationDate);
  };

  const today = new Date().toISOString().split('T')[0];

  return (
    <TableRow sx={{ "&:hover": { backgroundColor: "action.hover" } }}>
      {/* Mã sản phẩm */}
      <TableCell>
        <Typography variant="body2" fontWeight="medium">
          {orderItem.productId}
        </Typography>
      </TableCell>
      
      {/* Tên sản phẩm */}
      <TableCell>
        <Typography variant="body2">
          {orderItem.productName}
        </Typography>
      </TableCell>
      
      {/* Số lượng đặt */}
      <TableCell align="center">
        <Typography variant="body2" color="primary" fontWeight="medium">
          {orderItem.quantity}
        </Typography>
      </TableCell>
      
      {/* Kho nhập */}
      <TableCell>
        <FormControl size="small" fullWidth sx={{ minWidth: 150 }} error={!!warehouseError}>
          <InputLabel id={`warehouse-select-${orderItem.id}`}>
            Chọn kho
          </InputLabel>
          <Select
            labelId={`warehouse-select-${orderItem.id}`}
            value={selectedFacilityId || ""}
            onChange={handleWarehouseChange}
            label="Chọn kho"
          >
            {filteredInventoryItems.length === 0 ? (
              <MenuItem disabled>Không có kho khả dụng</MenuItem>
            ) : (
              filteredInventoryItems.map((inventory) => (
                <MenuItem key={inventory.facilityId} value={inventory.facilityId}>
                  {inventory.facilityName} ({inventory.quantity} trong kho)
                </MenuItem>
              ))
            )}
          </Select>
          {warehouseError && (
            <Typography variant="caption" color="error" sx={{ display: 'block', mt: 0.5 }}>
              {warehouseError}
            </Typography>
          )}
        </FormControl>
        
        {/* Show warning if inventory insufficient */}
        {selectedFacilityId && isInventoryInsufficient && (
          <Typography
            variant="caption"
            color="warning.main"
            sx={{ display: "flex", alignItems: "center", mt: 1, gap: 0.5 }}
          >
            <WarningIcon fontSize="inherit" />
            Chỉ còn {availableQuantity} sản phẩm
          </Typography>
        )}
        
        {/* Show available quantity info */}
        {selectedFacilityId && !isInventoryInsufficient && availableQuantity > 0 && (
          <Typography
            variant="caption"
            color="success.main"
            sx={{ display: "block", mt: 0.5 }}
          >
            Có sẵn: {availableQuantity} sản phẩm
          </Typography>
        )}
      </TableCell>
      
      {/* Số lượng nhập - Similar to ProductTable */}
      <TableCell>
        <Box>
          {/* Quantity controls */}
          <Box display="flex" alignItems="center" gap={0.5}>
            <IconButton 
              size="small" 
              onClick={handleQuantityDecrement}
              disabled={!selectedFacilityId || (selectedQuantity || 0) <= 0}
              color="primary"
            >
              <Remove fontSize="small" />
            </IconButton>
            
            <TextField
              type="number"
              size="small"
              value={selectedQuantity || ''}
              onChange={handleQuantityChange}
              disabled={!selectedFacilityId}
              inputProps={{
                min: 0,
                max: maxQuantity,
                style: { 
                  textAlign: "center",
                  width: '60px'
                },
              }}
              sx={{ 
                width: 80,
                '& .MuiOutlinedInput-root': {
                  '& input': {
                    fontSize: '0.875rem',
                    padding: '6px 8px'
                  }
                }
              }}
              error={!!quantityError}
            />
            
            <IconButton 
              size="small" 
              onClick={handleQuantityIncrement}
              disabled={!selectedFacilityId || (selectedQuantity || 0) >= maxQuantity}
              color="primary"
            >
              <Add fontSize="small" />
            </IconButton>
          </Box>
          
          {/* Quantity info and error */}
          <Box mt={0.5}>
            {selectedFacilityId && maxQuantity > 0 && (
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                Tối đa: {maxQuantity}
              </Typography>
            )}
            {selectedFacilityId && maxQuantity === 0 && (
              <Typography variant="caption" color="error" sx={{ display: 'block' }}>
                Kho không có sản phẩm này
              </Typography>
            )}
            {quantityError && (
              <Typography variant="caption" color="error" sx={{ display: 'block' }}>
                {quantityError}
              </Typography>
            )}
          </Box>
        </Box>
      </TableCell>
      
      {/* Lô hàng */}
      <TableCell>
        <TextField
          size="small"
          value={lotId || ''}
          onChange={handleLotChange}
          placeholder="Nhập lô hàng"
          sx={{ width: 120 }}
          error={!!lotError}
        />
        {lotError && (
          <Typography variant="caption" color="error" sx={{ display: 'block', mt: 0.5 }}>
            {lotError}
          </Typography>
        )}
      </TableCell>
      
      {/* Ngày sản xuất */}
      <TableCell>
        <TextField
          type="date"
          size="small"
          value={manufacturingDate || ''}
          onChange={handleManufacturingDateChange}
          inputProps={{ max: today }}
          sx={{ width: 140 }}
          error={!!dateError || !isDateValid()}
          InputLabelProps={{ shrink: true }}
        />
        {!isDateValid() && (
          <Typography variant="caption" color="error" sx={{ display: 'block', mt: 0.5 }}>
            Ngày SX phải trước ngày HH
          </Typography>
        )}
      </TableCell>
      
      {/* Ngày hết hạn */}
      <TableCell>
        <TextField
          type="date"
          size="small"
          value={expirationDate || ''}
          onChange={handleExpirationDateChange}
          inputProps={{ min: manufacturingDate || today }}
          sx={{ width: 140 }}
          error={!!dateError || !isDateValid()}
          InputLabelProps={{ shrink: true }}
        />
        {dateError && (
          <Typography variant="caption" color="error" sx={{ display: 'block', mt: 0.5 }}>
            {dateError}
          </Typography>
        )}
      </TableCell>
      
      {/* Thao tác */}
      <TableCell>
        <Tooltip title="Xóa sản phẩm">
          <IconButton 
            color="error" 
            size="small"
            onClick={() => onDeleteProduct(orderItem.id)}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </TableCell>
    </TableRow>
  );
};

export default InBoundRow;