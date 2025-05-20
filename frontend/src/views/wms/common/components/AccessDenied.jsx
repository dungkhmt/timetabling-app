import React from "react";
import { Box, Button, Paper, Stack, Typography, useTheme } from "@mui/material";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import LockIcon from "@mui/icons-material/Lock";
import { useHistory } from "react-router-dom";
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
const AccessDenied = () => {
  const theme = useTheme();
  const history = useHistory();

  const handleGoBack = () => {
    history.goBack();
  };

  return (
    <Box
      sx={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        height: `calc(100vh - 112px)`,
        backgroundColor: theme.palette.background.default,
        padding: 3
      }}
    >
      <Paper
        elevation={3}
        sx={{
          maxWidth: 600,
          width: "100%",
          padding: 4,
          borderRadius: 2,
          borderTop: `4px solid ${theme.palette.error.main}`,
          textAlign: "center"
        }}
      >
        <Box sx={{ position: "relative", mb: 3 }}>
          <Box
            sx={{
              backgroundColor: theme.palette.error.lighter || "#ffebee",
              borderRadius: "50%",
              width: 80,
              height: 80,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "0 auto",
              position: "relative",
              zIndex: 1
            }}
          >
            <LockIcon color="error" sx={{ fontSize: 40 }} />
          </Box>
          <Box
            sx={{
              backgroundColor: theme.palette.grey[100],
              borderRadius: "50%",
              width: 100,
              height: 100,
              position: "absolute",
              top: -10,
              left: "50%",
              transform: "translateX(-50%)",
              zIndex: 0
            }}
          />
        </Box>

        <Typography variant="h4" fontWeight="bold" color="error" gutterBottom>
          Truy cập bị từ chối
        </Typography>
        
        <Typography variant="subtitle1" color="text.secondary" paragraph>
          Bạn không có quyền truy cập vào trang này. Vui lòng liên hệ với quản trị viên nếu bạn cho rằng đây là một lỗi.
        </Typography>

        <Box
          sx={{
            backgroundColor: theme.palette.grey[50],
            borderRadius: 1,
            p: 2,
            mb: 3,
            display: "flex",
            alignItems: "center",
            gap: 2
          }}
        >
          <ErrorOutlineIcon color="warning" />
          <Typography variant="body2" color="text.secondary" textAlign="left">
            Nếu bạn cần được cấp quyền truy cập, vui lòng liên hệ với quản trị viên hệ thống.
          </Typography>
        </Box>

        <Stack direction="row" spacing={2} justifyContent="center">
          <Button variant="outlined" onClick={handleGoBack} startIcon={<ArrowBackIcon />}>
            Quay lại
          </Button>
        </Stack>
      </Paper>
    </Box>
  );
};

export default AccessDenied;