import React from "react";
import InfoCard from "./InfoCard";
import { useOrderDetail } from "../context/OrderDetailContext";

const OrderBasicInfo = () => {
  const { orderData } = useOrderDetail();

  if (!orderData) return null;

  const basicInfoItems = [
    { label: "Mã đơn hàng", value: orderData.id },
    { label: "Số hóa đơn (TC)", value: orderData.invoiceNumber || "-" },
    { label: "Mục đích đơn hàng bán", value: orderData.orderPurpose },
    { label: "Trạng thái", value: orderData.status },
    { label: "Kênh bán hàng", value: orderData.channel },
    { label: "Kho hàng", value: orderData.facilityName },
    { label: "Khách hàng", value: orderData.customerName },
  ];

  return <InfoCard items={basicInfoItems} />;
};

export default React.memo(OrderBasicInfo);