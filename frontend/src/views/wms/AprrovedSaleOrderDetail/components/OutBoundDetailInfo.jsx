import React, { memo } from "react";
import { Box, Typography, Paper, Chip, Divider, Grid } from "@mui/material";
import EventIcon from "@mui/icons-material/Event";
import PersonIcon from "@mui/icons-material/Person";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import WarehouseIcon from "@mui/icons-material/Warehouse";
import NoteIcon from "@mui/icons-material/Note";
import ScaleIcon from "@mui/icons-material/Scale";
import NumbersIcon from "@mui/icons-material/Numbers";
import DescriptionIcon from "@mui/icons-material/Description";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import PhoneIcon from "@mui/icons-material/Phone";

const InfoItem = memo(({ icon, label, value, isTitle = false }) => {
  const valueDisplay = value || "-";

  return (
    <Box sx={{ mb: 1.5, display: "flex", alignItems: "center" }}>
      {icon && <Box sx={{ mr: 1.2, color: "primary.main" }}>{icon}</Box>}
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

const StatusChip = memo(({ status }) => {
  let color = "default";
  let label = status || "Chưa xác định";

  switch (status?.toUpperCase()) {
    case "CREATED":
      color = "info";
      label = "Khởi tạo";
      break;
    case "EXPORTED":
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

          <InfoItem
            icon={<DescriptionIcon fontSize="small" />}
            label="Tên phiếu"
            value={data.shipmentName}
          />
          <InfoItem
            icon={<PersonIcon fontSize="small" />}
            label="Khách hàng"
            value={data.toCustomerName || "Chưa xác định"}
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
              icon={<ScaleIcon fontSize="small" />}
              label="Tổng khối lượng"
              value={data.totalWeight || "Chưa xác định"}
            />
            <InfoItem
              icon={<NumbersIcon fontSize="small" />}
              label="Tổng số lượng"
              value={data.totalQuantity || "Chưa xác định"}
            />
          </Box>

          <Divider sx={{ my: 2 }} />

          <InfoItem
            icon={<WarehouseIcon fontSize="small" />}
            label="Ngày xuất kho dự kiến"
            value={
              data.expectedDeliveryDate &&
              new Date(data.expectedDeliveryDate).toLocaleDateString("vi-VN")
            }
          />
          <InfoItem
            icon={<LocationOnIcon fontSize="small" />}
            label="Địa chỉ nhận hàng"
            value={data.deliveryFullAddress || "Chưa xác định"}
          />
          <InfoItem
            icon={<PhoneIcon fontSize="small" />}
            label="Số điện thoại nhận hàng"
            value={data.deliveryPhone || "Chưa xác định"}
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
