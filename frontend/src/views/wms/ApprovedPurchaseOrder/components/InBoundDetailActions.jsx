import React, { memo, useState } from "react";
import { Box, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import DeleteIcon from "@mui/icons-material/Delete";
import ReceiptIcon from "@mui/icons-material/Receipt";
import PrintIcon from "@mui/icons-material/Print";
import { useShipment } from "../../common/context/ShipmentContext";
import {useWms2Data} from "../../../../services/useWms2Data";

const InBoundDetailActions = ({ shipmentId, status, onActionComplete }) => {
  const {exportInBoundShipment} = useWms2Data();
  const {loading} = useShipment();
  const [confirmDialog, setConfirmDialog] = useState({ open: false, type: null });

  // Kiểm tra trạng thái để hiển thị các nút phù hợp
  const isCreated = status === "CREATED";
  const isShipped = status === "SHIPPED";
  const isCancelled = status === "CANCELLED";

  // Xử lý xác nhận hành động
  const handleConfirmAction = async () => {
    try {
      if (confirmDialog.type === "export") {
        await exportInBoundShipment(shipmentId);
      } 
      setConfirmDialog({ open: false, type: null });
      if (onActionComplete) onActionComplete();
    } catch (error) {
      console.error("Action failed:", error);
    }
  };

  // Hiển thị dialog xác nhận
  const showConfirmDialog = (type) => {
    let title, content;
    if (type === "export") {
      title = "Xác nhận nhập hàng";
      content = "Bạn có chắc chắn muốn nhập hàng cho phiếu nhập này? Hành động này không thể hoàn tác.";
    } else if (type === "cancel") {
      title = "Xác nhận hủy phiếu nhập";
      content = "Bạn có chắc chắn muốn hủy phiếu nhập này? Hành động này không thể hoàn tác.";
    }
    setConfirmDialog({ open: true, type, title, content });
  };

  return (
    <>
      <Box display="flex" gap={2} flexWrap="wrap">
        {isCreated && (
          <>
            <Button 
              variant="outlined" 
              startIcon={<EditIcon />}
              disabled={loading}
            >
              Chỉnh sửa
            </Button>
            <Button 
              variant="contained" 
              color="primary" 
              startIcon={<LocalShippingIcon />}
              disabled={loading}
              onClick={() => showConfirmDialog("export")}
            >
              Nhập hàng
            </Button>
            <Button 
              variant="contained" 
              color="error" 
              startIcon={<DeleteIcon />}
              disabled={loading}
              onClick={() => showConfirmDialog("cancel")}
            >
              Hủy bỏ
            </Button>
          </>
        )}
        
        {/* Nút in phiếu nhập có thể sử dụng bất kể trạng thái */}
        <Button 
          variant="contained" 
          color="secondary" 
          startIcon={<ReceiptIcon />}
          disabled={loading || isCancelled}
        >
          Phiếu giao
        </Button>
        
        <Button 
          variant="outlined" 
          startIcon={<PrintIcon />}
          disabled={loading || isCancelled}
        >
          In phiếu nhập
        </Button>
      </Box>

      {/* Dialog xác nhận */}
      <Dialog
        open={confirmDialog.open}
        onClose={() => setConfirmDialog({ ...confirmDialog, open: false })}
      >
        <DialogTitle>{confirmDialog.title}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {confirmDialog.content}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button 
            onClick={() => setConfirmDialog({ ...confirmDialog, open: false })} 
            disabled={loading}
          >
            Hủy
          </Button>
          <Button 
            onClick={handleConfirmAction} 
            color={confirmDialog.type === "cancel" ? "error" : "primary"}
            autoFocus 
            variant="contained"
            disabled={loading}
          >
            {loading ? "Đang xử lý..." : "Xác nhận"}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default memo(InBoundDetailActions);