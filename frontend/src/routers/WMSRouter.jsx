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
import SupplierListPage from "../views/wms/SupplierListPage/SupplierListPage";
import SupplierDetail from "views/wms/SupplierDetail/SupplierDetail";
import CreateFacility from "views/wms/CreateFacility/CreateFacility";
import FacilityListPage from "views/wms/FacilityListPage/FacilityListPage";
import FacilityDetail from "views/wms/FacilityDetail/FacilityDetail";
import OrderDashBoard from "../views/wms/OrderDashBoard/OrderDashBoard";
import ShipmentListPage from "../views/wms/ShipmentListPage/ShipmentListPage";
import {ORDER_TYPE_ID, SHIPMENT_TYPE_ID} from "../views/wms/common/constants/constants";
import PurchaseOrderDetail from "../views/wms/PurchaseOrderDetail/PurchaseOrderDetail";
import SaleOrderDetail from "../views/wms/SaleOrderDetail/SaleOrderDetail";
import DeliveryRouteListPage from "../views/wms/DeliveryRouteListPage/DeliveryRouteListPage";
import VehicleListPage from "../views/wms/VehicleListPage/VehicleListPage";
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
          component={PurchaseOrderDetail}
          exact
          path={`${path}/purchase/orders/details/:id`}
        ></Route>

        <Route
            component={ApprovedPurchaseOrderDetail}
            exact
            path={`${path}/purchase/orders/details/reviewed/:id`}
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
          component={SaleOrderDetail}
          exact
          path={`${path}/sales/orders/details/:id`}
        ></Route>

        <Route
            component={ApprovedSaleOrderDetail}
            exact
            path={`${path}/sales/orders/details/reviewed/:id`}
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
          component={SupplierListPage}
            exact
          path={`${path}/purchase/suppliers`}
        ></Route>

        <Route
            component={CreateSupplier}
                path={`${path}/purchase/suppliers/create`}
        ></Route>

        <Route
            component={SupplierDetail}
            path={`${path}/purchase/suppliers/details/:id`}
        ></Route>

        <Route
        component={FacilityListPage }
        exact
          path={`${path}/admin/facility`}
        ></Route>

        <Route
          component={CreateFacility}
          path={`${path}/admin/facility/create`}
        ></Route>

        <Route
          component={FacilityDetail}
          path={`${path}/admin/facility/details/:id`}
        ></Route>

        <Route
            path={`${path}/purchase/dashboard`}
            render={(props) => <OrderDashBoard {...props} orderTypeId={ORDER_TYPE_ID.PURCHASE_ORDER} />}
        />

        <Route
            path={`${path}/sales/dashboard`}
            render={(props) => <OrderDashBoard {...props} orderTypeId={ORDER_TYPE_ID.SALES_ORDER} />}
        />

        <Route
        path={`${path}/logistics/saleshipment`}
        render={(props) => <ShipmentListPage {...props} shipmentTypeId={SHIPMENT_TYPE_ID.OUTBOUND} />}
        />

        <Route
        path={`${path}/logistics/purchaseshipment`}
        render={(props) => <ShipmentListPage {...props} shipmentTypeId={SHIPMENT_TYPE_ID.INBOUND} />}
        />

        <Route
        path={`${path}/logistics/shipper`}
        component={ShipmentListPage}
        ></Route>

        <Route
          component={PurchaseOrderListPage}
          exact
          path={`${path}/logistics/purchaseorders`}
        ></Route>

        <Route
          component={SaleOrderListPage}
          exact
          path={`${path}/logistics/salesorders`}
        ></Route>

        <Route
          component={DeliveryRouteListPage}
            exact
            path={`${path}/logistics/deliveryroute`}
        ></Route>

        <Route
          component={VehicleListPage}
            exact
            path={`${path}/logistics/vehicle`}
        ></Route>
        
      </Switch>
    </div>
  );
}
