import { Route, Switch, useRouteMatch } from "react-router";
import CreateSaleOrder from "views/wms/CreateSaleOrder/CreateSaleOrder";
import PurchaseOrder from "views/wms/purchaseorder";
import SaleOrderDetail from "views/wms/SaleOrderDetail/SaleOrderDetail";
import OrderDetail from "views/wms/SaleOrderDetail/SaleOrderDetail";
import SaleOrderListPage from "views/wms/SaleOrderListPage/SaleOrderListPage";

export default function WMSRouter() {
  let { path } = useRouteMatch();
  return (
    <div>
      <Switch>
        <Route
          component={PurchaseOrder}
          exact
          path={`${path}/purchase/orders`}
        ></Route>

        <Route
          component={SaleOrderListPage}
          exact
          path={`${path}/sales/orders`}
        ></Route>

        <Route
          component={SaleOrderDetail}
          path={`${path}/sales/orders/details/:id`}
        ></Route>

        <Route
          component={CreateSaleOrder}
          path={`${path}/sales/orders/create`}
        ></Route>
      </Switch>
    </div>
  );
}
