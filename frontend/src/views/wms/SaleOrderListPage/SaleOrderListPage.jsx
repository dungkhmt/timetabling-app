import React from "react";
import { useWms2Data } from 'services/useWms2Data';
import OrderListBase from "../common/components/OrderListBase";
import OrderHeader from "../common/components/OrderHeader";
import OrderTabs from "../common/components/OrderTabs";
import OrderFilters from "../common/components/OrderFilters";
import OrderTable from "../common/components/OrderTable";
import {ORDER_TYPE_ID, SALE_ORDER_SCHEMA, SALE_ORDER_TABS} from "../common/constants/constants";
const SaleOrderListPage = () => {
  const { getSalesOrders, getSalesOrdersForExport } = useWms2Data();

  const initialFilters = {
    keyword: "",
    status: null,
    startCreatedAt: null,
    endCreatedAt: null,
    channelId: null
  };

  return (
    <OrderListBase
      type= {ORDER_TYPE_ID.SALES_ORDER}
      tabs={SALE_ORDER_TABS}
      filters={initialFilters}
      schema={SALE_ORDER_SCHEMA}
      getOrdersFunction={getSalesOrders}
      getOrdersForExportFunction={getSalesOrdersForExport}
      Header={OrderHeader}
      Tabs={OrderTabs}
      Filters={OrderFilters}
      Table={OrderTable}
    />
  );
};

export default SaleOrderListPage;
