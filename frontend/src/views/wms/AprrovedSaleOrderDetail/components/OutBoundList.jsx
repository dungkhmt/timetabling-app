import React, { useState, useEffect } from "react";
import { Box, Typography, Stack, useTheme, useMediaQuery, Button } from "@mui/material";
import { useApprovedOrderDetail } from "../context/OrderDetailContext";
import OutBoundTable from "./OutBoundTable";
import CreateOutboundDialog from "./CreateOutboundDialog";
import { useOrderDetail } from "views/wms/SaleOrderDetail/context/OrderDetailContext";

const OutBoundList = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const { getOutBoundsOrderApi } = useApprovedOrderDetail();
  const {orderData} = useOrderDetail();

  const [outBounData, setOutBoundData] = useState(null);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);

  console.log("Order data:", orderData);
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await getOutBoundsOrderApi(orderData.id, 0,10);
        console.log("API response:", response);
        setOutBoundData(response?.data ?? null);
      } catch (error) {
        console.error("Error fetching outbound data:", error);
      }
    };
    
    fetchData();
  }, [orderData.id]);

  const handleCloseDialog = (shouldRefresh = false) => {
    console.log("Dialog closing with refresh:", shouldRefresh);
    setOpenCreateDialog(false);
    
    if (shouldRefresh) {
      const refreshData = async () => {
        try {
          const response = await getOutBoundsOrderApi(orderData.id, 0, 10);
          setOutBoundData(response?.data ?? null);
        } catch (error) {
          console.error("Error refreshing data:", error);
        }
      };
      refreshData();
    }
  };

  const handleCreateClick = () => {
    console.log("Create button clicked");
    setOpenCreateDialog(true);
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
            Danh Sách phiếu
          </Typography>

          <Button
            variant="contained"
            color="primary"
            onClick={handleCreateClick}
          >
            Tạo phiếu xuất
          </Button>
        </Stack>

        <Typography variant="body1" mt={3}>Không tìm thấy thông tin đơn hàng</Typography>
        
        <CreateOutboundDialog
          open={openCreateDialog}
          onClose={handleCloseDialog}
          orderData={orderData}
        />
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
          Danh Sách phiếu
        </Typography>

        <Button 
          variant="contained" 
          color="primary" 
          onClick={handleCreateClick}
        >
          Tạo phiếu xuất
        </Button>
      </Stack>
      
      <OutBoundTable items={outBounData} />
      
      <CreateOutboundDialog
        open={openCreateDialog}
        onClose={handleCloseDialog}
      />
    </Box>
  );
};

export default OutBoundList;