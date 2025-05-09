import React from "react";
import InfoCard from "./InfoCard";
import { useOrderDetail } from "../context/OrderDetailContext";
import dayjs from "dayjs";
import { ORDER_TYPE_ID } from "../constants/constants";

const OrderBasicInfo = () => {
  const { orderData } = useOrderDetail();

  if (!orderData) return null;

  // Xác định loại đơn hàng
  const orderTypeId = orderData.orderTypeId || ORDER_TYPE_ID.SALES_ORDER;
  
  // Các trường thông tin cơ bản
  const commonInfoItems = [
    { label: "Mã đơn hàng", value: orderData.id },
    { label: "Số hóa đơn", value: orderData.numberOfInvoices || "-" },
    { label: "Ngày tạo", value: dayjs(orderData.createdStamp).format("DD/MM/YYYY HH:mm") },
    { label: "Trạng thái", value: orderData?.status },
    { label: "Kho hàng", value: orderData.facilityName || "-" },
    { label: "Người tạo", value: orderData.createdByUser || "-" },
  ];
  
  // Thông tin riêng theo từng loại đơn hàng
  let specificItems = [];
  
  switch (orderTypeId) {
    case ORDER_TYPE_ID.SALES_ORDER:
      specificItems = [
        { label: "Kênh bán hàng", value: orderData.saleChannelId || "-" },
        { label: "Khách hàng", value: orderData.customerName || "-" },
      ];
      break;
      
    case ORDER_TYPE_ID.PURCHASE_ORDER:
      specificItems = [
        { label: "Nhà cung cấp", value: orderData.supplierName || "-" },
        { label: "Loại đơn", value: orderData.purchaseType || "Mua hàng" },
      ];
      break;
      
    case ORDER_TYPE_ID.TRANSFER_ORDER:
      specificItems = [
        { label: "Kho xuất", value: orderData.facilityFromName || "-" },
        { label: "Kho nhận", value: orderData.facilityToName || "-" },
      ];
      break;
      
    case ORDER_TYPE_ID.RETURN_ORDER:
      // Đơn trả hàng có thể là trả từ khách hàng hoặc trả đến nhà cung cấp
      if (orderData.customerName) {
        specificItems = [
          { label: "Khách hàng", value: orderData.customerName || "-" },
          { label: "Lý do trả", value: orderData.returnReason || "-" },
        ];
      } else {
        specificItems = [
          { label: "Nhà cung cấp", value: orderData.supplierName || "-" },
          { label: "Lý do trả", value: orderData.returnReason || "-" },
        ];
      }
      break;
      
    default:
      // Mặc định không thêm thông tin gì
      break;
  }
  
  // Kết hợp thông tin cơ bản và thông tin riêng
  const basicInfoItems = [...commonInfoItems, ...specificItems];

  return <InfoCard items={basicInfoItems} />;
};

export default React.memo(OrderBasicInfo);