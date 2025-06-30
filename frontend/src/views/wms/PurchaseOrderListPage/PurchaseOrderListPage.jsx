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
  const { getPurchaseOrders, getPurchaseOrdersForExport, getWeeklyLowStockForecast } = useWms2Data();
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
      const response = await getWeeklyLowStockForecast();
      
      if (response?.code === 200 && response?.data && response.data.length > 0) {
        const forecastData = response.data;
        
        // Format suggested items từ weekly forecast data
        const suggestedItems = forecastData.map(item => {
          // Tính số lượng gợi ý dựa trên logic tuần:
          // Nếu tồn kho hiện tại < dự báo xuất 4 tuần tới
          const recommendedQuantity = Math.max(
            item.totalPredictedQuantity || 0, // Dự báo 4 tuần
            (item.averageWeeklyQuantity || 0) * 4 - (item.currentStock || 0) // Bù đắp thiếu hụt
          );

          return {
            productId: item.productId,
            productName: item.productName,
            quantity: Math.max(recommendedQuantity, 0), // Đảm bảo không âm
            unit: item.unit || "Cái",
            price: item.price || 0,
            discount: 0,
            tax: item.tax || 0,
            // Thông tin bổ sung để hiển thị trong form
            currentStock: item.currentStock || 0,
            averageWeeklyQuantity: item.averageWeeklyQuantity || 0,
            maxWeeklyOutbound: item.maxWeeklyOutbound || 0,
            weeksUntilStockout: item.weeksUntilStockout || 0,
            forecastDate: item.forecastDate,
            reason: `Tồn kho thấp (${item.currentStock || 0}), dự báo cần ${item.totalPredictedQuantity || 0} ${item.unit} trong 4 tuần tới`
          };
        }).filter(item => item.quantity > 0 && (item.weeksUntilStockout || 0) <= 8); // Chỉ lấy sản phẩm cần nhập trong 8 tuần

        if (suggestedItems.length === 0) {
          toast.info("Tất cả sản phẩm đều có đủ tồn kho theo dự báo tuần");
          return;
        }

        // Chuẩn bị dữ liệu forecast đầy đủ để truyền sang CreatePurchaseOrder
        const formattedForecastData = forecastData.map(item => ({
          productId: item.productId,
          productName: item.productName,
          totalPredictedQuantity: item.totalPredictedQuantity || 0,
          averageWeeklyQuantity: item.averageWeeklyQuantity || 0,
          currentStock: item.currentStock || 0,
          maxWeeklyOutbound: item.maxWeeklyOutbound || 0,
          minWeeklyOutbound: item.minWeeklyOutbound || 0,
          weeksUntilStockout: item.weeksUntilStockout || 0,
          unit: item.unit,
          predictedValue: item.predictedValue,
          upperBoundTotal: item.upperBoundTotal || 0,
          lowerBoundTotal: item.lowerBoundTotal || 0,
          trend: item.trend || 0,
          historicalWeeklyData: item.historicalWeeklyData || {}, // Dữ liệu lịch sử theo tuần
          weeklyForecastData: item.weeklyForecastData || {}, // Dữ liệu dự báo 4 tuần
          weeklyUpperBounds : item.weeklyUpperBounds || {}, // Upper bounds theo tuần
          weeklyLowerBounds : item.weeklyLowerBounds || {}, // Lower bounds theo tuần
          modelInfo: item.modelInfo || 'ARIMA (Weekly)',
          confidenceLevel: item.confidenceLevel || 75,
          rmse: item.rmse,
          forecastDate: item.forecastDate
        }));

        // Navigate với dữ liệu đầy đủ
        history.push({
          pathname: "/wms/purchase/orders/create",
          state: { 
            suggestedItems, 
            forecastData: formattedForecastData,
            isFromWeeklyForecast: true // Flag để biết đây là từ weekly forecast
          }
        });

        toast.success(`Đã tạo gợi ý cho ${suggestedItems.length} sản phẩm cần nhập thêm (dự báo theo tuần)`);
      } else {
        toast.info("Không có sản phẩm nào cần bổ sung hàng tồn kho theo dự báo tuần");
      }
    } catch (error) {
      console.error("Error fetching weekly forecast data:", error);
      toast.error("Không thể lấy dữ liệu dự báo theo tuần: " + (error.message || "Lỗi không xác định"));
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
          {!isMobile && "Gợi ý nhập hàng (Tuần)"}
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