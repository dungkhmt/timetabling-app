import React, { useState } from "react";
import { useHistory } from "react-router-dom"; // React Router v5
import { Button, Box, CircularProgress, useMediaQuery } from "@mui/material";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import { toast } from "react-toastify";
import { useWms2Data } from 'services/useWms2Data';
import OrderListBase from "../common/components/OrderListBase";
import OrderHeader from "../common/components/OrderHeader";
import OrderTabs from "../common/components/OrderTabs";
import OrderFilters from "../common/components/OrderFilters";
import OrderTable from "../common/components/OrderTable";
import { PURCHASE_ORDER_SCHEMA, PURCHASE_ORDER_TABS, ORDER_TYPE_ID } from "../common/constants/constants";
import { MENU_CONSTANTS } from "../common/constants/screenId";
import { withAuthorization } from "../common/components/withAuthorization";

const PurchaseOrderListPageBase = () => {
  const history = useHistory(); // Use useHistory from React Router v5
  const { getPurchaseOrders, getPurchaseOrdersForExport, getLowStockForecast } = useWms2Data();
  const [loading, setLoading] = useState(false);
  const isMobile = useMediaQuery(theme => theme.breakpoints.down("sm"));

  const initialFilters = {
    keyword: "",
    status: null,
    startCreatedAt: null,
    endCreatedAt: null,
    channelId: null
  };

  const handleSuggestPurchase = async () => {
    setLoading(true);
    try {
      const response = await getLowStockForecast();
      if (response?.code === 200 && response?.data && response.data.length > 0) {
        const suggestedItems = response.data.map(item => ({
          productId: item.productId,
          productName: item.productName,
          forecastDate: item.forecastDate,
          quantity: item.quantity,
          unit: item.unit || "Thùng", // Default unit if missing
          price: item.price || 0, // Default price if missing
          discount: item.discount || 0, // Use provided discount or default to 0
        }));

        // Use history.push to navigate and send data
        history.push({
          pathname: "/wms/purchase/orders/create",
          state: { suggestedItems, forecastData: response.data }
        });
      } else {
        toast.info("Không có sản phẩm nào cần bổ sung hàng tồn kho");
      }
    } catch (error) {
      console.error("Error fetching forecast data:", error);
      toast.error("Không thể lấy dữ liệu dự báo: " + (error.message || "Lỗi không xác định"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Box mb={2} display="flex" justifyContent="flex-end">
        <Button
          variant="contained"
          color="secondary"
          startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <ShoppingCartIcon />}
          onClick={handleSuggestPurchase}
          disabled={loading}
          sx={{ mr: 2 }}
        >
          {!isMobile && "Gợi ý nhập hàng"}
        </Button>
      </Box>
      
      <OrderListBase
        type={ORDER_TYPE_ID.PURCHASE_ORDER}
        tabs={PURCHASE_ORDER_TABS}
        filters={initialFilters}
        schema={PURCHASE_ORDER_SCHEMA}
        getOrdersFunction={getPurchaseOrders}
        getOrdersForExportFunction={getPurchaseOrdersForExport}
        Header={OrderHeader}
        Tabs={OrderTabs}
        Filters={OrderFilters}
        Table={OrderTable}
      />
    </>
  );
};

const PurchaseOrderListPage = (props) => {
  const { location } = props;
  const path = location.pathname;
  const menuId = path.includes('logistics') ? MENU_CONSTANTS.LOGISTICS_PURCHASE_LIST : MENU_CONSTANTS.PURCHASE_LIST;
  const AuthorizedComponent = withAuthorization(PurchaseOrderListPageBase, menuId);
  return <AuthorizedComponent {...props} />;
}

export default PurchaseOrderListPage;