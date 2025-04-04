import { Route, Switch, useRouteMatch } from "react-router";
import ApprovedSaleOrderDetail from "views/wms/AprrovedSaleOrderDetail/ApprovedSaleOrderDetail";
import OutBoundDetail from "views/wms/AprrovedSaleOrderDetail/components/OutBoundDetail";
import CreatePurchaseOrder from "views/wms/CreatePurchaseOrder/CreatePurchaseOrder";
import CreateSaleOrder from "views/wms/CreateSaleOrder/CreateSaleOrder";
import SaleOrderListPage from "views/wms/SaleOrderListPage/SaleOrderListPage";
import PurchaseOrderListPage from "../views/wms/PurchaseOrderListPage/PurchaseOrderListPage";
import ApprovedPurchaseOrderDetail from "../views/wms/ApprovedPurchaseOrder/ApprovedPurchaseOrderDetail";
import InBoundDetail from "views/wms/ApprovedPurchaseOrder/components/InBoundDetail";
export default function WMSRouter() {
  let { path } = useRouteMatch();
  return (
    <div>
      <Switch>
        <Route
          component={PurchaseOrderListPage}
          exact
          path={`${path}/purchase/orders`}
        ></Route>

        <Route
          component={ApprovedPurchaseOrderDetail}
          exact
          path={`${path}/purchase/orders/details/:id`}
        ></Route>

        <Route
          component={CreatePurchaseOrder}
          path={`${path}/purchase/orders/create`}
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

        <Route
          component={InBoundDetail}
            path={`${path}/purchase/orders/details/:id/inbound/:shipmentId`}
        ></Route>
      </Switch>
    </div>
  );
}
