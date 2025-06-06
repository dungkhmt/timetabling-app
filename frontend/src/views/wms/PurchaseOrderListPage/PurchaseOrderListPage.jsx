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
      const response = await getLowStockForecast(); // Sử dụng method mới
      
      if (response?.code === 200 && response?.data && response.data.length > 0) {
        const forecastData = response.data;
        
        // Format suggested items từ forecast data mới
        const suggestedItems = forecastData.map(item => {
          // Tính số lượng gợi ý dựa trên logic: nếu tồn kho hiện tại < trung bình xuất trong 7 ngày
          const recommendedQuantity = Math.max(
            item.totalPredictedQuantity || 0, // Dự báo 7 ngày
            (item.averageDailyOutbound || 0) * 7 - (item.currentStock || 0) // Bù đắp thiếu hụt
          );

          return {
            productId: item.productId,
            productName: item.productName,
            quantity: Math.max(recommendedQuantity, 0), // Đảm bảo không âm
            unit: item.unit || "Cái",
            price: item.price || 0,
            discount: item.discount || 0,
            tax: item.tax || 0,
            // Thông tin bổ sung để hiển thị trong form
            currentStock: item.currentStock || 0,
            averageDailyOutbound: item.averageDailyOutbound || 0,
            maxDailyOutbound: item.maxDailyOutbound || 0,
            forecastDate: item.forecastDate,
            reason: `Tồn kho thấp (${item.currentStock || 0}), dự báo cần ${item.totalPredictedQuantity || 0} ${item.unit} trong 7 ngày tới`
          };
        }).filter(item => item.quantity > 0); // Chỉ lấy những sản phẩm cần nhập

        if (suggestedItems.length === 0) {
          toast.info("Tất cả sản phẩm đều có đủ tồn kho theo dự báo");
          return;
        }

        // Chuẩn bị dữ liệu forecast đầy đủ để truyền sang CreatePurchaseOrder
        const formattedForecastData = forecastData.map(item => ({
          productId: item.productId,
          productName: item.productName,
          totalPredictedQuantity: item.totalPredictedQuantity || 0,
          currentStock: item.currentStock || 0,
          averageDailyOutbound: item.averageDailyOutbound || 0,
          maxDailyOutbound: item.maxDailyOutbound || 0,
          minDailyOutbound: item.minDailyOutbound || 0,
          unit: item.unit,
          price: item.price,
          discount: item.discount || 0,
          tax: item.tax || 0,
          historicalData: item.historicalData || {}, // Dữ liệu lịch sử 30 ngày
          forecastData: item.forecastData || {}, // Dữ liệu dự báo 7 ngày
          modelInfo: item.modelInfo || 'Auto ARIMA',
          confidenceLevel: item.confidenceLevel || 0.95,
          mse: item.mse,
          rmse: item.rmse,
          forecastDate: item.forecastDate
        }));

        // Navigate với dữ liệu đầy đủ
        history.push({
          pathname: "/wms/purchase/orders/create",
          state: { 
            suggestedItems, 
            forecastData: formattedForecastData,
            isFromForecast: true // Flag để biết đây là từ forecast
          }
        });

        toast.success(`Đã tạo gợi ý cho ${suggestedItems.length} sản phẩm cần nhập thêm`);
      } else {
        toast.info("Không có sản phẩm nào cần bổ sung hàng tồn kho theo dự báo");
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