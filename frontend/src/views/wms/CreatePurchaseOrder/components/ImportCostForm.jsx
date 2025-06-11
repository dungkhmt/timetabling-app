import React, { useState } from 'react';
import {
  Box,
  Typography,
  TextField,
  Button,
  IconButton,
  Card,
  CardContent,
  Grid,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  LocalShipping
} from '@mui/icons-material';
import { useOrderForm } from 'views/wms/common/context/OrderFormContext';

const ImportCostDialog = ({ open, onClose, onSave, currentCost, title }) => {
  const [costData, setCostData] = useState({
    key: currentCost?.key || '',
    value: currentCost?.value || ''
  });

  const handleSave = () => {
    if (costData.key.trim() && costData.value.trim()) {
      onSave({
        key: costData.key.trim(),
        value: parseFloat(costData.value) || 0
      });
      setCostData({ key: '', value: '' });
      onClose();
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN').format(amount);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Tên chi phí"
              value={costData.key}
              onChange={(e) => setCostData(prev => ({ ...prev, key: e.target.value }))}
              placeholder="Ví dụ: Vận chuyển, Bảo hiểm, Thuế nhập khẩu..."
              size="small"
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Số tiền (VND)"
              type="number"
              value={costData.value}
              onChange={(e) => setCostData(prev => ({ ...prev, value: e.target.value }))}
              placeholder="Nhập số tiền..."
              size="small"
              InputProps={{
                endAdornment: costData.value ? (
                  <Typography variant="caption" color="textSecondary">
                    ≈ {formatCurrency(parseFloat(costData.value) || 0)} VND
                  </Typography>
                ) : null
              }}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Hủy</Button>
        <Button 
          onClick={handleSave} 
          variant="contained"
          disabled={!costData.key.trim() || !costData.value.trim()}
        >
          Lưu
        </Button>
      </DialogActions>
    </Dialog>
  );
};

const ImportCostForm = () => {
  const { order, setOrder } = useOrderForm();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingIndex, setEditingIndex] = useState(-1);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const addImportCost = (costData) => {
    const costs = order.costs || [];
    if (editingIndex >= 0) {
      // Update existing cost
      const updatedCosts = [...costs];
      updatedCosts[editingIndex] = costData;
      setOrder(prev => ({ ...prev, costs: updatedCosts }));
    } else {
      // Add new cost
      setOrder(prev => ({ 
        ...prev, 
        costs: [...costs, costData]
      }));
    }
    setEditingIndex(-1);
  };

  const removeImportCost = (index) => {
    const updatedCosts = order.costs.filter((_, i) => i !== index);
    setOrder(prev => ({ ...prev, costs: updatedCosts }));
  };

  const openEditDialog = (index) => {
    setEditingIndex(index);
    setDialogOpen(true);
  };

  const openAddDialog = () => {
    setEditingIndex(-1);
    setDialogOpen(true);
  };

  const getTotalImportCosts = () => {
    return (order.costs || []).reduce((total, cost) => total + (cost.value || 0), 0);
  };

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Typography variant="h6" component="h3">
            <LocalShipping sx={{ mr: 1, verticalAlign: 'middle' }} />
            Chi phí nhập hàng
          </Typography>
          <Button
            variant="outlined"
            startIcon={<AddIcon />}
            onClick={openAddDialog}
            size="small"
          >
            Thêm chi phí
          </Button>
        </Box>

        {order.costs && order.costs.length > 0 ? (
          <Box>
            <Grid container spacing={2}>
              {order.costs.map((cost, index) => (
                <Grid item xs={12} sm={6} md={4} key={index}>
                  <Card variant="outlined" sx={{ height: '100%' }}>
                    <CardContent sx={{ p: 2 }}>
                      <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={1}>
                        <Typography variant="subtitle2" sx={{ fontWeight: 'bold', flexGrow: 1 }}>
                          {cost.key}
                        </Typography>
                        <Box>
                          <IconButton size="small" onClick={() => openEditDialog(index)}>
                            <EditIcon fontSize="small" />
                          </IconButton>
                          <IconButton 
                            size="small" 
                            color="error" 
                            onClick={() => removeImportCost(index)}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Box>
                      </Box>
                      <Typography variant="body2" color="primary" fontWeight="medium">
                        {formatCurrency(cost.value || 0)}
                      </Typography>
                      <Chip 
                        label={`Chi phí ${index + 1}`} 
                        size="small" 
                        variant="outlined" 
                        sx={{ mt: 1 }}
                      />
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>

            <Box 
              sx={{ 
                mt: 2, 
                p: 2, 
                backgroundColor: 'primary.50', 
                borderRadius: 1,
                border: '1px solid',
                borderColor: 'primary.200'
              }}
            >
              <Typography variant="subtitle2" color="primary" fontWeight="bold">
                💰 Tổng chi phí nhập hàng: {formatCurrency(getTotalImportCosts())}
              </Typography>
              <Typography variant="caption" color="textSecondary">
                {order.costs.length} khoản chi phí được áp dụng
              </Typography>
            </Box>
          </Box>
        ) : (
          <Box 
            sx={{ 
              textAlign: 'center', 
              py: 3, 
              color: 'text.secondary',
              backgroundColor: 'grey.50',
              borderRadius: 1,
              border: '1px dashed',
              borderColor: 'grey.300'
            }}
          >
            <LocalShipping sx={{ fontSize: 48, mb: 1, opacity: 0.5 }} />
            <Typography variant="body2">
              Chưa có chi phí nhập hàng nào được thêm
            </Typography>
            <Typography variant="caption">
              Nhấn "Thêm chi phí" để bắt đầu
            </Typography>
          </Box>
        )}
      </CardContent>

      <ImportCostDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        onSave={addImportCost}
        currentCost={editingIndex >= 0 ? order.costs[editingIndex] : null}
        title={editingIndex >= 0 ? "Sửa chi phí nhập hàng" : "Thêm chi phí nhập hàng"}
      />
    </Card>
  );
};

export default ImportCostForm;