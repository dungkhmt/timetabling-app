import React, { useState, useEffect } from "react";
import {
    Box,
    Button,
    Grid,
    Typography,
    Chip,
    Card,
    CardContent,
    Tabs,
    Tab,
    Divider,
} from "@mui/material";
import { useLocation, useHistory } from "react-router-dom";
import { toast } from "react-toastify";
import { useWms2Data } from "services/useWms2Data";
import { OrderFormProvider } from "../common/context/OrderFormContext";
import BasicInfoForm from "./components/BasicInfoForm";
import DeliveryInfoForm from "./components/DeliveryInfoForm";
import ImportCostForm from "./components/ImportCostForm";
import OrderSummary from "./components/OrderSummary";
import ProductSearch from "../common/components/ProductSearch";
import ProductTable from "../common/components/ProductTable";
import ProductForecastChart from "./components/ProductForecastChart";
import ProductChart from "../InventoryReport/components/ProductChart";
import InsightsIcon from "@mui/icons-material/Insights";
import ShowChartIcon from "@mui/icons-material/ShowChart";
import BarChartIcon from "@mui/icons-material/BarChart";
import DateRangeIcon from "@mui/icons-material/DateRange";
import { ORDER_TYPE_ID } from "../common/constants/constants";

// TabPanel component để hiển thị nội dung tabs
const TabPanel = ({ children, value, index, ...other }) => {
    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`forecast-tabpanel-${index}`}
            aria-labelledby={`forecast-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box sx={{ pt: 2 }}>
                    {children}
                </Box>
            )}
        </div>
    );
};

const CreatePurchaseOrder = () => {
    const { createPurchaseOrder } = useWms2Data();
    const location = useLocation();
    const history = useHistory();
    const suggestedItems = location.state?.suggestedItems || [];
    const forecastData = location.state?.forecastData || [];
    const isFromWeeklyForecast = location.state?.isFromWeeklyForecast || false;

    const [activeTab, setActiveTab] = useState(0);
    const [order, setOrder] = useState({
        id: "", 
        supplierId: "", 
        note: "", 
        orderName: "", 
        discount: 0, 
        costs: [],
        orderDate: null, 
        deliveryAfterDate: null, 
        deliveryBeforeDate: null, 
        orderItems: [], 
    });

    const [entities, setEntities] = useState({
        facilities: [],
        suppliers: [],
        products: []
    });

    useEffect(() => {
        if (suggestedItems.length > 0) {
            setOrder(prev => ({
                ...prev,
                orderItems: [...suggestedItems],
                note: isFromWeeklyForecast ? 
                    "Đơn hàng tạo từ gợi ý dự báo tồn kho thấp (theo tuần)" :
                    "Đơn hàng tạo từ gợi ý dự báo tồn kho thấp"
            }));
            toast.info(`Đã thêm ${suggestedItems.length} sản phẩm từ dự báo tồn kho thấp${isFromWeeklyForecast ? ' (theo tuần)' : ''}`);
        }
    }, [suggestedItems, isFromWeeklyForecast]);

    // Calculate subtotal of all items (price * quantity, before any discounts)
    const calculateItemsSubtotal = () => {
        return order.orderItems.reduce((total, item) => {
            return total + (item.price * item.quantity);
        }, 0);
    };

    // Calculate total of all items after applying individual item discounts but before tax
    const calculateItemsTotal = () => {
        return order.orderItems.reduce((total, item) => {
            const itemSubtotal = item.price * item.quantity;
            const itemDiscountAmount = item.discount || 0;
            const itemFinalAmount = itemSubtotal - itemDiscountAmount;
            return total + Math.max(itemFinalAmount, 0);
        }, 0);
    };

    // Calculate total import costs
    const calculateImportCostsTotal = () => {
        return (order.costs || []).reduce((total, cost) => total + (cost.value || 0), 0);
    };

    // Calculate final order total with tax and import costs
    const calculateOrderTotal = () => {
        const itemsTotal = calculateItemsTotal();
        
        // Calculate tax for each item
        const totalTax = order.orderItems.reduce((total, item) => {
            const itemSubtotal = item.price * item.quantity;
            const itemAfterDiscount = itemSubtotal - (item.discount || 0);
            return total + (itemAfterDiscount * (item.tax || 0) / 100);
        }, 0);

        const costsTotal = calculateImportCostsTotal();
        const orderDiscountAmount = order.discount || 0;
        
        const finalTotal = itemsTotal + totalTax + costsTotal - orderDiscountAmount;
        return Math.max(finalTotal, 0);
    };

    // Update item quantity
    const updateItemQuantity = (productId, change, directSet = false) => {
        const updatedItems = order.orderItems.map(item => {
            if (item.productId === productId) {
                const newQuantity = directSet 
                    ? Math.max(1, change) // Direct set quantity
                    : Math.max(1, item.quantity + change); // Increment/decrement
                return { ...item, quantity: newQuantity };
            }
            return item;
        }).filter(item => item.quantity > 0);
        
        setOrder(prev => ({ ...prev, orderItems: updatedItems }));
    };

    // Update item discount
    const updateItemDiscount = (productId, discountAmount) => {
        const updatedItems = order.orderItems.map(item => {
            if (item.productId === productId) {
                return { ...item, discount: Math.max(0, discountAmount) };
            }
            return item;
        });
        setOrder(prev => ({ ...prev, orderItems: updatedItems }));
    };

    // Update item price
    const updateItemPrice = (productId, newPrice) => {
        const updatedItems = order.orderItems.map(item => {
            if (item.productId === productId) {
                return { ...item, price: Math.max(0, newPrice) };
            }
            return item;
        });
        setOrder(prev => ({ ...prev, orderItems: updatedItems }));
    };

    // Remove item from order
    const removeItemFromOrder = (productId) => {
        const updatedItems = order.orderItems.filter(item => item.productId !== productId);
        setOrder(prev => ({ ...prev, orderItems: updatedItems }));
    };

    const handleSubmit = async () => {
        if (!order.supplierId) {
            toast.warning("Vui lòng chọn nhà cung cấp");
            return;
        }
        if (!order.deliveryAfterDate) {
            toast.warning("Vui lòng chọn ngày giao hàng");
            return;
        }
        if (order.orderItems.length === 0) {
            toast.warning("Vui lòng thêm sản phẩm vào đơn hàng");
            return;
        }

        try {
            // Prepare data for API call
            const orderData = {
                ...order,
                orderItems: order.orderItems.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity,
                    price: item.price,
                    unit: item.unit,
                    discount: item.discount,
                    tax: item.tax,
                    note: item.note
                })),
                costs: order.costs || []
            };

            await createPurchaseOrder(orderData);
        } catch (error) {
            console.error("Error creating purchase order:", error);
            toast.error("Lỗi khi tạo đơn hàng: " + (error.message || "Lỗi không xác định"));
        }
    };

    const handleTabChange = (event, newValue) => {
        setActiveTab(newValue);
    };

    // Chuẩn bị dữ liệu cho ProductChart (biểu đồ cột) - weekly data
    const chartProducts = forecastData.map(item => ({
        productName: item.productName,
        quantity: item.totalPredictedQuantity || item.quantity || 0
    }));

    const orderContextValue = {
        order,
        setOrder,
        entities,
        setEntities,
        updateItemQuantity,
        updateItemDiscount,
        updateItemPrice,
        removeItemFromOrder,
        calculateOrderTotal,
        calculateItemsTotal,
        calculateItemsSubtotal,
        calculateImportCostsTotal
    };

    return (
        <OrderFormProvider value={orderContextValue}>
            <Box p={3}>
                <Typography sx={{ fontWeight: 700 }} variant="h5" gutterBottom>
                    Tạo đơn hàng mua
                    {suggestedItems.length > 0 && (
                        <Chip 
                            icon={isFromWeeklyForecast ? <DateRangeIcon /> : <InsightsIcon />}
                            label={isFromWeeklyForecast ? "Từ dự báo tuần" : "Từ dự báo hàng tồn"} 
                            color="secondary" 
                            sx={{ ml: 2 }}
                        />
                    )}
                </Typography>

                {/* Hiển thị thông tin dự báo theo tuần với tabs */}
                {forecastData.length > 0 && (
                    <Card sx={{ mb: 3, backgroundColor: "#f5f5f5" }}>
                        <CardContent>
                            <Typography variant="subtitle1" fontWeight={600} gutterBottom>
                                <DateRangeIcon sx={{ mr: 1, verticalAlign: "middle" }} />
                                Thông tin dự báo theo tuần
                            </Typography>
                            <Typography variant="body2" color="text.secondary" gutterBottom>
                                {isFromWeeklyForecast ? 
                                    "Dự báo tiêu thụ theo tuần dựa trên dữ liệu lịch sử xuất kho và mô hình ARIMA. Dự báo cho 4 tuần tới với độ tin cậy cao hơn." :
                                    "Dự báo tiêu thụ dựa trên dữ liệu lịch sử xuất kho và mô hình ARIMA."
                                }
                            </Typography>

                            {/* Tabs để chuyển đổi giữa các view */}
                            <Box sx={{ borderBottom: 1, borderColor: 'divider', mt: 2 }}>
                                <Tabs value={activeTab} onChange={handleTabChange}>
                                    <Tab 
                                        icon={<BarChartIcon />} 
                                        label="Tổng quan" 
                                        iconPosition="start"
                                    />
                                    <Tab 
                                        icon={<ShowChartIcon />} 
                                        label="Chi tiết dự báo tuần" 
                                        iconPosition="start"
                                    />
                                </Tabs>
                            </Box>

                            {/* Tab 1: Tổng quan với ProductChart (biểu đồ cột) */}
                            <TabPanel value={activeTab} index={0}>
                                <Grid container spacing={2}>
                                    {/* Cards tóm tắt */}
                                    <Grid item xs={12}>
                                        <Grid container spacing={2} mb={2}>
                                            {forecastData.slice(0, 3).map((item) => (
                                                <Grid item xs={12} md={4} key={item.productId}>
                                                    <Card variant="outlined">
                                                        <CardContent>
                                                            <Typography variant="subtitle2" noWrap>
                                                                {item.productName}
                                                            </Typography>
                                                            <Typography variant="body2" color="primary">
                                                                Dự báo 4 tuần: {item.totalPredictedQuantity || item.quantity} {item.unit}
                                                            </Typography>
                                                            <Typography variant="body2" color="secondary">
                                                                TB/tuần: {item.averageWeeklyQuantity || 0} {item.unit}
                                                            </Typography>
                                                            <Typography variant="caption" color="textSecondary">
                                                                Tồn kho: {item.currentStock || 0} | 
                                                                Hết sau: {item.weeksUntilStockout || 0} tuần
                                                            </Typography>
                                                        </CardContent>
                                                    </Card>
                                                </Grid>
                                            ))}
                                        </Grid>
                                    </Grid>
                                    
                                    {/* Biểu đồ cột tổng quan */}
                                    <Grid item xs={12}>
                                        <ProductChart 
                                            products={chartProducts}
                                            title="Dự báo nhu cầu 4 tuần tới"
                                            color="#2196f3"
                                            emptyMessage="Không có dữ liệu dự báo theo tuần"
                                        />
                                    </Grid>
                                </Grid>
                            </TabPanel>

                            {/* Tab 2: Chi tiết với ProductForecastChart (biểu đồ đường ARIMA theo tuần) */}
                            <TabPanel value={activeTab} index={1}>
                                <Grid container spacing={3}>
                                    {forecastData.map((item) => (
                                        <Grid item xs={12} key={item.productId}>
                                            <ProductForecastChart forecastData={item} />
                                        </Grid>
                                    ))}
                                </Grid>
                            </TabPanel>
                        </CardContent>
                    </Card>
                )}

                <Divider sx={{ mb: 3 }} />

                {/* Form thông tin đơn hàng */}
                <Grid container spacing={3}>
                    <Grid item xs={12} md={6}>
                        <BasicInfoForm />
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <DeliveryInfoForm />
                    </Grid>
                </Grid>

                {/* Chi phí nhập hàng */}
                <ImportCostForm />

                {/* Tìm kiếm và bảng sản phẩm */}
                <Box mt={3}>
                    <ProductSearch orderTypeId={ORDER_TYPE_ID.PURCHASE_ORDER}/>
                    <ProductTable orderTypeId={ORDER_TYPE_ID.PURCHASE_ORDER} />
                </Box>

                {/* Order Summary */}
                <OrderSummary />

                {/* Nút lưu */}
                <Box mt={3} textAlign="right">
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleSubmit}
                        size="large"
                        disabled={order.orderItems.length === 0}
                        sx={{ px: 4, py: 1.5 }}
                    >
                        Lưu đơn hàng
                    </Button>
                </Box>
            </Box>
        </OrderFormProvider>
    );
};

export default CreatePurchaseOrder;