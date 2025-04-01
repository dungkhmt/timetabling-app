import { Route, Switch, useRouteMatch } from "react-router";
import ApprovedSaleOrderDetail from "views/wms/AprrovedSaleOrderDetail/ApprovedSaleOrderDetail";
import OutBoundDetail from "views/wms/AprrovedSaleOrderDetail/components/OutBoundDetail";
import CreatePurchaseOrder from "views/wms/CreatePurchaseOrder/CreatePurchaseOrder";
import CreateSaleOrder from "views/wms/CreateSaleOrder/CreateSaleOrder";
import PurchaseOrder from "views/wms/purchaseorder";
import SaleOrderListPage from "views/wms/SaleOrderListPage/SaleOrderListPage";

export default function WMSRouter() {
  let { path } = useRouteMatch();
  return (
    <div>
      <Switch>
        <Route
          component={CreatePurchaseOrder}
          exact
          path={`${path}/purchase/orders`}
        ></Route>

        <Route
          component={SaleOrderListPage}
          exact
          path={`${path}/sales/orders`}
        ></Route>

        <Route
          component={ApprovedSaleOrderDetail}
          exact
          path={`${path}/sales/orders/details/:id`}
        ></Route>

        <Route
          component={CreateSaleOrder}
          path={`${path}/sales/orders/create`}
        ></Route>

        <Route
          component={OutBoundDetail}
          path={`${path}/sales/orders/details/:id/outbound/:shipmentId`}
        ></Route>
      </Switch>
    </div>
  );
}
