import React from "react";
import { Box, Card, CardHeader, CardContent, Grid, Typography, Chip } from "@mui/material";
import InventoryIcon from "@mui/icons-material/Inventory";
import CategoryIcon from "@mui/icons-material/Category";
import { useTheme } from "@mui/material/styles";

const ProductBasicInfo = ({ product }) => {
  const theme = useTheme();
  return (
    <Card elevation={2} sx={{ mb: 3, overflow: "visible" }}>
      <CardHeader
        title={
          <Box display="flex" alignItems="center">
            <InventoryIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
            <Typography variant="h6">Thông tin cơ bản</Typography>
          </Box>
        }
        sx={{
          backgroundColor: theme.palette.background.default,
          borderBottom: `1px solid ${theme.palette.divider}`,
        }}
      />
      <CardContent>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Box sx={{ p: 2, borderRadius: 1, backgroundColor: theme.palette.background.default }}>
              <Typography variant="h6" gutterBottom>
                {product.name}
              </Typography>
              <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                Mã sản phẩm: <strong>{product.id}</strong>
              </Typography>
              <Box mt={2} display="flex" alignItems="center">
                <Typography variant="body1" mr={1}>
                  Trạng thái:
                </Typography>
                {product.statusId === "ACTIVE" ? (
                  <Chip label="Mở bán" color="success" />
                ) : product.statusId === "INACTIVE" ? (
                  <Chip label="Ngừng bán" color="error" />
                ) : (
                  <Chip label={product.statusId || "N/A"} />
                )}
              </Box>
            </Box>
          </Grid>
          <Grid item xs={12} md={6}>
            <Box
              sx={{
                p: 2,
                borderRadius: 1,
                backgroundColor: theme.palette.background.default,
                height: "100%",
                display: "flex",
                flexDirection: "column",
                justifyContent: "center",
              }}
            >
              <Box display="flex" alignItems="center" mb={1}>
                <CategoryIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
                <Typography variant="body2" color="text.secondary">
                  Danh mục sản phẩm
                </Typography>
              </Box>
              <Typography variant="h6">{product.productCategoryName || "—"}</Typography>
              <Typography variant="caption" color="text.secondary" mt={1}>
                ID danh mục: {product.productCategoryId || "—"}
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default ProductBasicInfo;