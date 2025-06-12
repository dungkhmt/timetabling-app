import React, { useState, useEffect } from "react";
import { 
  Box, 
  Typography, 
  Stack, 
  useTheme, 
  useMediaQuery, 
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  DialogContentText,
  Alert,
  CircularProgress
} from "@mui/material";
import { 
  AutoFixHigh as AutoIcon, 
  Add as AddIcon 
} from "@mui/icons-material";
import { useApprovedOrderDetail } from "../context/OrderDetailContext";
import OutBoundTable from "./OutBoundTable";
import CreateOutboundDialog from "./CreateOutboundDialog";
import { useOrderDetail } from "views/wms/common/context/OrderDetailContext";
import { useWms2Data } from "services/useWms2Data";
import { toast } from "react-toastify";

const OutBoundList = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const { getOutBoundsOrderApi } = useApprovedOrderDetail();
  const { orderData } = useOrderDetail();
  const { autoAssignShipment } = useWms2Data();

  const [outBounData, setOutBoundData] = useState(null);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [openAutoAssignDialog, setOpenAutoAssignDialog] = useState(false);
  const [autoAssignLoading, setAutoAssignLoading] = useState(false);

  console.log("Order data:", orderData);
  
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await getOutBoundsOrderApi(orderData.id, 0, 10);
        console.log("API response:", response);
        setOutBoundData(response?.data ?? null);
      } catch (error) {
        console.error("Error fetching outbound data:", error);
      }
    };
    
    fetchData();
  }, [orderData.id]);

  const refreshData = async () => {
    try {
      const response = await getOutBoundsOrderApi(orderData.id, 0, 10);
      setOutBoundData(response?.data ?? null);
    } catch (error) {
      console.error("Error refreshing data:", error);
    }
  };

  const handleCloseDialog = (shouldRefresh = false) => {
    console.log("Dialog closing with refresh:", shouldRefresh);
    setOpenCreateDialog(false);
    
    if (shouldRefresh) {
      refreshData();
    }
  };

  const handleCreateClick = () => {
    console.log("Create button clicked");
    setOpenCreateDialog(true);
  };

  const handleAutoAssignClick = () => {
    setOpenAutoAssignDialog(true);
  };

  const handleAutoAssignConfirm = async () => {
    setAutoAssignLoading(true);
    try {
      const response = await autoAssignShipment(orderData.id);
      
      if (response.code === 201) {
        toast.success("Tạo phiếu xuất tự động thành công!");
        setOpenAutoAssignDialog(false);
        // Refresh data to show new outbound
        await refreshData();
      } else {
        toast.error(response.message || "Có lỗi xảy ra khi tạo phiếu xuất");
      }
    } catch (error) {
      console.error("Error auto-assigning shipment:", error);
      toast.error("Không thể tạo phiếu xuất tự động. Vui lòng thử lại!");
    } finally {
      setAutoAssignLoading(false);
    }
  };

  const handleAutoAssignCancel = () => {
    setOpenAutoAssignDialog(false);
  };

  if (!outBounData) {
    return (
      <Box>
        <Stack 
          direction={isMobile ? "column" : "row"} 
          spacing={2} 
          justifyContent="space-between"
          alignItems="center"
          mb={2}
        >
          <Typography variant="h6">
            Danh Sách phiếu xuất
          </Typography>

          <Stack direction="row" spacing={2}>
            <Button
              variant="outlined"
              color="primary"
              startIcon={<AutoIcon />}
              onClick={handleAutoAssignClick}
            >
              Tự động tạo phiếu xuất
            </Button>
            
            <Button
              variant="contained"
              color="primary"
              startIcon={<AddIcon />}
              onClick={handleCreateClick}
            >
              Tạo phiếu xuất thủ công
            </Button>
          </Stack>
        </Stack>

        <Typography variant="body1" mt={3}>Chưa có phiếu xuất nào được tạo</Typography>
        
        <CreateOutboundDialog
          open={openCreateDialog}
          onClose={handleCloseDialog}
          orderData={orderData}
        />

        {/* Auto Assign Confirmation Dialog */}
        <Dialog
          open={openAutoAssignDialog}
          onClose={handleAutoAssignCancel}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>
            Xác nhận tạo phiếu xuất tự động
          </DialogTitle>
          <DialogContent>
            <DialogContentText sx={{ mb: 2 }}>
              Hệ thống sẽ tự động tạo phiếu xuất dựa trên các nguyên tắc tối ưu:
            </DialogContentText>
            
            <Alert severity="info" sx={{ mb: 2 }}>
              <Typography variant="body2" component="div">
                <strong>Lợi ích của việc tạo tự động:</strong>
                <ul style={{ marginTop: 8, marginBottom: 0, paddingLeft: 20 }}>
                  <li>Tối ưu hóa chi phí vận chuyển bằng cách chọn kho gần nhất</li>
                  <li>Đảm bảo nguyên tắc FEFO (First Expired, First Out)</li>
                  <li>Phân bổ hàng hóa thông minh từ nhiều kho</li>
                  <li>Tiết kiệm thời gian xử lý đơn hàng</li>
                </ul>
              </Typography>
            </Alert>

            <DialogContentText>
              Bạn có muốn tiếp tục tạo phiếu xuất tự động cho đơn hàng này không?
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button 
              onClick={handleAutoAssignCancel} 
              disabled={autoAssignLoading}
            >
              Hủy
            </Button>
            <Button 
              onClick={handleAutoAssignConfirm} 
              variant="contained"
              disabled={autoAssignLoading}
              startIcon={autoAssignLoading ? <CircularProgress size={16} /> : <AutoIcon />}
            >
              {autoAssignLoading ? "Đang xử lý..." : "Xác nhận tạo"}
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    );
  }

  return (
    <Box>
      <Stack 
        direction={isMobile ? "column" : "row"} 
        spacing={2} 
        justifyContent="space-between"
        alignItems="center"
        mb={3}
      >
        <Typography variant="h6">
          Danh Sách phiếu xuất
        </Typography>

        <Stack direction="row" spacing={2}>
          <Button
            variant="outlined"
            color="primary"
            startIcon={<AutoIcon />}
            onClick={handleAutoAssignClick}
          >
            Tự động tạo phiếu xuất
          </Button>
          
          <Button 
            variant="contained" 
            color="primary" 
            startIcon={<AddIcon />}
            onClick={handleCreateClick}
          >
            Tạo phiếu xuất thủ công
          </Button>
        </Stack>
      </Stack>
      
      <OutBoundTable items={outBounData} />
      
      <CreateOutboundDialog
        open={openCreateDialog}
        onClose={handleCloseDialog}
        orderData={orderData}
      />

      {/* Auto Assign Confirmation Dialog */}
      <Dialog
        open={openAutoAssignDialog}
        onClose={handleAutoAssignCancel}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          Xác nhận tạo phiếu xuất tự động
        </DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ mb: 2 }}>
            Hệ thống sẽ tự động tạo phiếu xuất dựa trên các nguyên tắc tối ưu:
          </DialogContentText>
          
          <Alert severity="info" sx={{ mb: 2 }}>
            <Typography variant="body2" component="div">
              <strong>Lợi ích của việc tạo tự động:</strong>
              <ul style={{ marginTop: 8, marginBottom: 0, paddingLeft: 20 }}>
                <li>Tối ưu hóa chi phí vận chuyển bằng cách chọn kho gần nhất</li>
                <li>Đảm bảo nguyên tắc FEFO (First Expired, First Out)</li>
                <li>Phân bổ hàng hóa thông minh từ nhiều kho</li>
                <li>Tiết kiệm thời gian xử lý đơn hàng</li>
              </ul>
            </Typography>
          </Alert>

          <DialogContentText>
            Bạn có muốn tiếp tục tạo phiếu xuất tự động cho đơn hàng này không?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button 
            onClick={handleAutoAssignCancel} 
            disabled={autoAssignLoading}
          >
            Hủy
          </Button>
          <Button 
            onClick={handleAutoAssignConfirm} 
            variant="contained"
            disabled={autoAssignLoading}
            startIcon={autoAssignLoading ? <CircularProgress size={16} /> : <AutoIcon />}
          >
            {autoAssignLoading ? "Đang xử lý..." : "Xác nhận tạo"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default OutBoundList;