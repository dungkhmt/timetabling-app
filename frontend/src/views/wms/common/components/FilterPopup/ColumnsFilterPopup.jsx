import React, { useState } from 'react';
import { 
  Box, 
  Typography, 
  Checkbox, 
  Button, 
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText
} from '@mui/material';
import { useSaleOrder } from '../../../SaleOrderListPage/context/SaleOrderContext';

// Column display names
const columnLabels = {
  id: "Mã đơn hàng",
  created_at: "Ngày tạo đơn",
  status: "Trạng thái",
  customer_name: "Tên khách hàng",
  user_created_name: "Người tạo đơn",
  total_quantity: "Tổng số lượng",
  total_price: "Tổng tiền",
  facility_name: "Kho hàng",
  delivery_date: "Ngày giao hàng",
  customer_phone: "Số điện thoại KH",
  customer_address: "Địa chỉ KH",
  customer_email: "Email KH",
  note: "Ghi chú",
  tags: "Tags",
  discount_type: "Loại chiết khấu",
  discount_value: "Giá trị chiết khấu",
  customer_id: "Mã khách hàng"
};

const ColumnsFilterPopup = ({ open, onClose }) => {
  const { columns, defaultColumns, handleColumnsChange } = useSaleOrder();
  
  // Local state for columns selection
  const [selectedColumns, setSelectedColumns] = useState({ ...columns });
  
  const handleColumnToggle = (columnKey) => {
    setSelectedColumns(prev => ({
      ...prev,
      [columnKey]: !prev[columnKey]
    }));
  };
  
  const handleResetToDefault = () => {
    setSelectedColumns({ ...defaultColumns });
  };
  
  const handleApply = () => {
    handleColumnsChange(selectedColumns);
    onClose();
  };
  
  return (
    <Dialog 
      open={open} 
      onClose={onClose}
      fullWidth
      maxWidth="xs"
    >
      <DialogTitle>
        <Typography variant="h6">Tùy chỉnh cột hiển thị</Typography>
      </DialogTitle>
      
      <DialogContent dividers>
        <List dense>
          {Object.entries(selectedColumns).map(([key, checked]) => (
            <ListItem 
              key={key} 
              button 
              onClick={() => handleColumnToggle(key)}
              dense
            >
              <ListItemIcon>
                <Checkbox
                  edge="start"
                  checked={checked}
                  tabIndex={-1}
                  disableRipple
                />
              </ListItemIcon>
              <ListItemText primary={columnLabels[key] || key} />
            </ListItem>
          ))}
        </List>
      </DialogContent>
      
      <DialogActions sx={{ justifyContent: 'space-between', px: 3, py: 2 }}>
        <Button onClick={handleResetToDefault}>
          Khôi phục mặc định
        </Button>
        <Box>
          <Button onClick={onClose} sx={{ mr: 1 }}>
            Hủy
          </Button>
          <Button 
            onClick={handleApply} 
            variant="contained" 
            color="primary"
          >
            Áp dụng
          </Button>
        </Box>
      </DialogActions>
    </Dialog>
  );
};

export default React.memo(ColumnsFilterPopup);