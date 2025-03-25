import React, { memo } from "react";
import { Box, Typography, Paper, Chip, Divider, Grid } from "@mui/material";
import EventIcon from "@mui/icons-material/Event";
import PersonIcon from "@mui/icons-material/Person";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import WarehouseIcon from "@mui/icons-material/Warehouse";
import NoteIcon from "@mui/icons-material/Note";

// Component hiển thị một mục thông tin
const InfoItem = memo(({ icon, label, value, isTitle = false }) => {
  const valueDisplay = value || "-";

  return (
    <Box sx={{ mb: 1, display: "flex", alignItems: "flex-start" }}>
      {icon && <Box sx={{ mr: 1, color: "primary.main", mt: 0.3 }}>{icon}</Box>}
      <Box>
        <Typography variant="body2" color="text.secondary" component="span">
          {label}:
        </Typography>{" "}
        <Typography
          variant={isTitle ? "subtitle1" : "body2"}
          component="span"
          fontWeight={isTitle ? "bold" : "regular"}
        >
          {valueDisplay}
        </Typography>
      </Box>
    </Box>
  );
});

// Component hiển thị trạng thái
const StatusChip = memo(({ status }) => {
  let color = "default";
  let label = status || "Chưa xác định";

  switch (status?.toUpperCase()) {
    case "CREATED":
      color = "info";
      label = "Khởi tạo";
      break;
    case "SHIPPED":
      color = "success";
      label = "Đã xuất kho";
      break;
    case "CANCELLED":
      color = "error";
      label = "Đã hủy";
      break;
    case "PENDING":
      color = "warning";
      label = "Đang xử lý";
      break;
    default:
      color = "default";
  }

  return (
    <Chip
      label={label}
      color={color}
      variant="outlined"
      size="small"
      sx={{ fontWeight: "medium" }}
    />
  );
});

const OutBoundDetailInfo = ({ data }) => {
  console.log("Data ", data);
  if (!data) return null;

  return (
    <Paper elevation={1} sx={{ p: 3, mt: 2, borderRadius: 2 }}>
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Box mb={2}>
            <InfoItem
              icon={<LocalShippingIcon fontSize="small" />}
              label="Mã phiếu xuất"
              value={data.id}
              isTitle={true}
            />
            <Box display="flex" alignItems="center" sx={{ mt: 1 }}>
              <Typography variant="body2" sx={{ mr: 1 }}>
                Trạng thái:
              </Typography>
              <StatusChip status={data.statusId} />
            </Box>
          </Box>

          <Divider sx={{ my: 2 }} />

          <InfoItem  label="Tên phiếu" value={data.shipmentName} />
          <InfoItem
            icon={<PersonIcon fontSize="small" />}
            label="Khách hàng"
            value={data.customerName}
          />

          <InfoItem
            icon={<EventIcon fontSize="small" />}
            label="Ngày tạo"
            value={
              data.createdStamp &&
              new Date(data.createdStamp).toLocaleDateString("vi-VN")
            }
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <Box mb={2}>
            <InfoItem
              icon={<WarehouseIcon fontSize="small" />}
              label="Kho xuất"
              value={data.warehouseName || "Chưa xác định"}
            />
            <InfoItem label="Địa chỉ kho" value={data.warehouseAddress} />
          </Box>

          <Divider sx={{ my: 2 }} />

          <InfoItem
            icon={<EventIcon fontSize="small" />}
            label="Ngày xuất kho dự kiến"
            value={
              data.expectedDeliveryDate &&
              new Date(data.expectedDeliveryDate).toLocaleDateString("vi-VN")
            }
          />
          <InfoItem
            icon={<NoteIcon fontSize="small" />}
            label="Ghi chú"
            value={data.note}
          />
        </Grid>
      </Grid>
    </Paper>
  );
};

export default memo(OutBoundDetailInfo);
