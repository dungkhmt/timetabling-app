import React from "react";
import InfoCard from "../../common/components/InfoCard";
import { useOrderDetail } from "../context/OrderDetailContext";

const OrderDeliveryInfo = () => {
  const { orderData } = useOrderDetail();

  if (!orderData) return null;

  const deliveryInfoItems = [
    { label: "Địa chỉ", value: orderData.deliveryAddress },
    { label: "Số Điện Thoại", value: orderData.deliveryPhone },
    { label: "Độ ưu tiên", value: orderData.priority },
    { label: "Ngày giao hàng", value: orderData.deliveryDate },
    { label: "Ngày tạo đơn", value: orderData.createdDate },
    { label: "Ghi chú", value: orderData.notes },
  ];

  return <InfoCard items={deliveryInfoItems} />;
};

export default React.memo(OrderDeliveryInfo);