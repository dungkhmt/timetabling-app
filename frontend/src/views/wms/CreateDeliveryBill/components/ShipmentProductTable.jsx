import React from "react";
import {
  Box,
  Typography,
  Card,
  CardContent,
  TableContainer,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Checkbox,
  TextField,
  Paper,
  Alert
} from "@mui/material";
import { useDeliveryBillForm } from "../../common/context/DeliveryBillFormContext";

const ShipmentProductTable = () => {
  const { deliveryBill, setDeliveryBill, entities } = useDeliveryBillForm();
  const products = deliveryBill.products || [];

  // Handle product selection
  const handleProductSelection = (productId, checked) => {
    setDeliveryBill(prev => ({
      ...prev,
      products: prev.products.map(product => 
        product.productId === productId 
          ? { ...product, selected: checked }
          : product
      )
    }));
  };

  // Handle quantity change
  const handleQuantityChange = (productId, value) => {
    const quantity = parseInt(value, 10);
    
    setDeliveryBill(prev => ({
      ...prev,
      products: prev.products.map(product => 
        product.productId === productId 
          ? { 
              ...product, 
              quantity: isNaN(quantity) ? 0 : Math.min(quantity, product.maxQuantity) 
            }
          : product
      )
    }));
  };

  // Select/Deselect all products
  const handleSelectAllProducts = (checked) => {
    setDeliveryBill(prev => ({
      ...prev,
      products: prev.products.map(product => ({ ...product, selected: checked }))
    }));
  };

  // Check if all products are selected
  const allSelected = products.length > 0 && products.every(p => p.selected);
  // Check if some products are selected
  const someSelected = products.some(p => p.selected) && !allSelected;

  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Sản phẩm
        </Typography>

        {!entities.selectedShipment && (
          <Alert severity="info">
            Vui lòng chọn lô hàng trước
          </Alert>
        )}

        {entities.selectedShipment && products.length === 0 && (
          <Alert severity="info">
            Lô hàng này không có sản phẩm nào
          </Alert>
        )}

        {products.length > 0 && (
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell padding="checkbox">
                    <Checkbox
                      checked={allSelected}
                      onChange={(e) => handleSelectAllProducts(e.target.checked)}
                      indeterminate={someSelected}
                    />
                  </TableCell>
                  <TableCell>Sản phẩm</TableCell>
                  <TableCell align="right">Số lượng</TableCell>
                  <TableCell align="right">Có sẵn</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {products.map((product) => (
                  <TableRow key={product.productId}>
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={product.selected}
                        onChange={(e) => handleProductSelection(product.productId, e.target.checked)}
                      />
                    </TableCell>
                    <TableCell>{product.productName}</TableCell>
                    <TableCell align="right">
                      <TextField
                        type="number"
                        size="small"
                        InputProps={{ 
                          inputProps: { min: 1, max: product.maxQuantity },
                          sx: { width: '80px' }
                        }}
                        disabled={!product.selected}
                        value={product.quantity}
                        onChange={(e) => handleQuantityChange(product.productId, e.target.value)}
                      />
                    </TableCell>
                    <TableCell align="right">{product.maxQuantity}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </CardContent>
    </Card>
  );
};

export default ShipmentProductTable;