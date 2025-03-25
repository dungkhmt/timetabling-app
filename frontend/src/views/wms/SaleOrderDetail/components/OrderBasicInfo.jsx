import React from "react";
import InfoCard from "../../common/components/InfoCard";
import { useOrderDetail } from "../context/OrderDetailContext";
import dayjs from "dayjs";

const OrderBasicInfo = () => {
  const { orderData } = useOrderDetail();
  console.log("orderData", orderData);

  if (!orderData) return null;

  const basicInfoItems = [
    { label: "Mã đơn hàng", value: orderData.id },
    { label: "Số hóa đơn (TC)", value: orderData.numberOfInvoices || "-" },
    { label: "Ngày tạo", value: dayjs(orderData.createdStamp).format("DD/MM/YYYY HH:mm") },
    { label: "Trạng thái", value: orderData?.status },
    { label: "Kênh bán hàng", value: orderData.saleChannelId },
    { label: "Kho hàng", value: orderData.facilityName || "-" },
    { label: "Khách hàng", value: orderData.customerName || "-" },
  ];

  return <InfoCard items={basicInfoItems} />;
};

export default React.memo(OrderBasicInfo);