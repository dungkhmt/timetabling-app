import React from "react";
import { useWms2Data } from 'services/useWms2Data';
import OrderListBase from "../common/components/OrderListBase";
import OrderHeader from "../common/components/OrderHeader";
import OrderTabs from "../common/components/OrderTabs";
import OrderFilters from "../common/components/OrderFilters";
import OrderTable from "../common/components/OrderTable";
import {ORDER_TYPE_ID, SALE_ORDER_SCHEMA, SALE_ORDER_TABS} from "../common/constants/constants";
import {MENU_CONSTANTS} from "../common/constants/screenId";
import {withAuthorization} from "../common/components/withAuthorization";
const SaleOrderListPageBase = () => {
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

const SaleOrderListPage = (props) => {
    const { location } = props;
    const path = location.pathname;
    const menuId = path.includes('logistics') ? MENU_CONSTANTS.LOGISTICS_SALES_LIST : MENU_CONSTANTS.SALES_LIST;
    const AuthorizedComponent = withAuthorization(SaleOrderListPageBase, menuId);
    return <AuthorizedComponent {...props} />;
}

export default SaleOrderListPage;
