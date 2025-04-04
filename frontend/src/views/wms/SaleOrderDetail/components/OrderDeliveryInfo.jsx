import React from "react";
import InfoCard from "../../common/components/InfoCard";
import { useOrderDetail } from "../../common/context/OrderDetailContext";

const OrderDeliveryInfo = () => {
  const { orderData } = useOrderDetail();

  if (!orderData) return null;

  const deliveryInfoItems = [
    { label: "Địa chỉ", value: orderData.deliveryAddress },
    { label: "Số Điện Thoại", value: orderData.deliveryPhone },
    { label: "Độ ưu tiên", value: orderData.priority },
    { label: "Ngày giao hàng dự kiến", value: orderData.deliveryAfterDate },
    { label: "Tổng giá trị đơn", value: orderData.totalAmount },
    {label: "Tổng số lượng sản phẩm bán: ", value: orderData.totalQuantity},
    { label: "Ghi chú", value: orderData.note },
  ];

  return <InfoCard items={deliveryInfoItems} />;
};

export default React.memo(OrderDeliveryInfo);