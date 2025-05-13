import { Route, Switch, useRouteMatch } from "react-router";
import ApprovedSaleOrderDetail from "views/wms/AprrovedSaleOrderDetail/ApprovedSaleOrderDetail";
import OutBoundDetail from "views/wms/AprrovedSaleOrderDetail/components/OutBoundDetail";
import CreatePurchaseOrder from "views/wms/CreatePurchaseOrder/CreatePurchaseOrder";
import CreateSaleOrder from "views/wms/CreateSaleOrder/CreateSaleOrder";
import SaleOrderListPage from "views/wms/SaleOrderListPage/SaleOrderListPage";
import PurchaseOrderListPage from "../views/wms/PurchaseOrderListPage/PurchaseOrderListPage";
import ApprovedPurchaseOrderDetail from "../views/wms/ApprovedPurchaseOrder/ApprovedPurchaseOrderDetail";
import InBoundDetail from "views/wms/ApprovedPurchaseOrder/components/InBoundDetail";
import CreateDeliveryBill from "views/wms/CreateDeliveryBill/CreateDeliveryBill";
import DeliveryBillListPage from "views/wms/DeliveryBillListPage/DeliveryBillListPage";
import CreateDeliveryPlan from "views/wms/CreateDeliveryPlan/CreateDeliveryPlan";
import DeliveryPlanListPage from "views/wms/DeliveryPlanListPage/DeliveryPlanListPage";
import DeliveryPlanDetail from "views/wms/DeliveryPlanDetail/DeliveryPlanDetail";
import CreateProduct from "../views/wms/CreateProduct/CreateProduct";
import ProductListPage from "../views/wms/ProductListPage/ProductListPage";
import ProductDetail from "../views/wms/ProductDetail/ProductDetail";
import CreateCustomer from "../views/wms/CreateCustomer/CreateCustomer";
import CustomerListPage from "../views/wms/CustomerListPage/CustomerListPage";
import CustomerDetail from "../views/wms/CustomerDetail/CustomerDetail";
import CreateSupplier from "../views/wms/CreateSupplier/CreateSupplier";
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

        <Route
          component={DeliveryBillListPage}
          exact
          path={`${path}/logistics/deliverybill`}
        ></Route>

        <Route
          component={CreateDeliveryBill}
          path={`${path}/logistics/deliverybill/create`}
        ></Route>

        <Route
          component={DeliveryPlanListPage}
          exact
          path={`${path}/logistics/delivery`}
        ></Route>

        <Route
          component={CreateDeliveryPlan}
          path={`${path}/logistics/delivery/create`}
        ></Route>

        <Route
          component={DeliveryPlanDetail}
          path={`${path}/logistics/delivery/details/:id`}
        ></Route>

        <Route
          component={ProductListPage}
            exact
            path={`${path}/admin/product`}
        ></Route>

        <Route
            component={CreateProduct}
            path={`${path}/admin/product/create`}
        ></Route>

        <Route
          component={ProductDetail}
            path={`${path}/admin/product/details/:id`}
        ></Route>

        <Route
            component={CustomerListPage}
            exact
            path={`${path}/sales/customers`}
        ></Route>

        <Route
            component={CreateCustomer}
            path={`${path}/sales/customers/create`}
        ></Route>

        <Route
            component={CustomerDetail}
            path={`${path}/sales/customers/details/:id`}
        ></Route>

        <Route
          component={CreateSupplier}
          path={`${path}/purchase/suppliers`}
        ></Route>
      </Switch>
    </div>
  );
}
