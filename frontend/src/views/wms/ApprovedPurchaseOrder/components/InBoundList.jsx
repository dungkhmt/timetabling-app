import React, { useState, useEffect } from "react";
import { Box, Typography, Stack, useTheme, useMediaQuery, Button } from "@mui/material";
import { useApprovedOrderDetail } from "../context/OrderDetailContext";
import InBoundTable from "./InBoundTable";
import CreateInBoundDialog from "./CreateInBoundDialog";
import { useOrderDetail } from "views/wms/common/context/OrderDetailContext";

const InBoundList = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const { getInBoundsOrderApi } = useApprovedOrderDetail();
  const {orderData} = useOrderDetail();

  const [InBoundData, setInBoundData] = useState(null);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);

  console.log("Order data:", orderData);
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await getInBoundsOrderApi(orderData.id, 0,10);
        console.log("API response:", response);
        setInBoundData(response?.data ?? null);
      } catch (error) {
        console.error("Error fetching inbound data:", error);
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
          const response = await getInBoundsOrderApi(orderData.id, 0, 10);
          setInBoundData(response?.data ?? null);
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

  if (!InBoundData) {
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
            Tạo phiếu nhập
          </Button>
        </Stack>

        <Typography variant="body1" mt={3}>Không tìm thấy thông tin đơn hàng</Typography>
        
        <CreateInBoundDialog
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
          Tạo phiếu nhập
        </Button>
      </Stack>
      
      <InBoundTable items={InBoundData} />
      
      <CreateInBoundDialog
        open={openCreateDialog}
        onClose={handleCloseDialog}
      />
    </Box>
  );
};

export default InBoundList;