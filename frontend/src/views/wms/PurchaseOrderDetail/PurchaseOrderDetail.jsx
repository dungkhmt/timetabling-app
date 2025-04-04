import React from "react";
import { OrderDetailProvider } from "../common/context/OrderDetailContext";
import OrderHeader from "./components/OrderHeader";
import OrderBasicInfo from "../common/components/OrderBasicInfo";
import OrderDeliveryInfo from "../common/components/OrderDeliveryInfo";
import OrderItemsList from "../common/components/OrderItemsList";
import OrderSummary from "../common/components/OrderSummary";
import OrderDetailBase from "../common/components/OrderDetailBase";
import { ORDER_TYPE_ID } from "../common/constants/constants";

const orderLabels = [
  "Tổng quan",
  "Thông tin chung",
  "Thanh toán",
  "Sản phẩm",
    "Phiếu nhập"
];

const PurchaseOrderDetailContent = () => {
  return (
    <OrderDetailBase
      tabLabels={orderLabels}
      HeaderComponent={OrderHeader}
      BasicInfoComponent={OrderBasicInfo}
      DeliveryInfoComponent={OrderDeliveryInfo}
      ItemsListComponent={OrderItemsList}
      SummaryComponent={OrderSummary}
    />
  );
};

const PurchaseOrderDetail = () => {
  return (
    <OrderDetailProvider orderType={ORDER_TYPE_ID.PURCHASE_ORDER}>
      <PurchaseOrderDetailContent />
    </OrderDetailProvider>
  );
};

export default PurchaseOrderDetail;