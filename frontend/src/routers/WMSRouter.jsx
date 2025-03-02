import { Route, Switch, useRouteMatch } from "react-router";
import CreateSaleOrder from "views/wms/CreateSaleOrder/CreateSaleOrder";
import PurchaseOrder from "views/wms/purchaseorder";
import OrderDetail from "views/wms/SaleOrderDetail/SaleOrderDetail";

export default function WMSRouter() {
  let { path } = useRouteMatch();
  return (
    <div>
      <Switch>
        <Route
          component={PurchaseOrder}
          exact
          path={`${path}/purchase/orders`}
        >
        </Route>

        
        <Route
          component={OrderDetail}
          exact
          path={`${path}/sales/orders`}
        >
        </Route>
        
      </Switch>
    </div>
  );
}
