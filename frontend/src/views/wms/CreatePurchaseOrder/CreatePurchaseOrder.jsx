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
import ProductSearch from "../common/components/ProductSearch";
import ProductTable from "../common/components/ProductTable";
import ProductForecastChart from "./components/ProductForecastChart";
import ProductChart from "../InventoryReport/components/ProductChart"; // Import từ InventoryReport
import InsightsIcon from "@mui/icons-material/Insights";
import ShowChartIcon from "@mui/icons-material/ShowChart";
import BarChartIcon from "@mui/icons-material/BarChart";

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

    const [activeTab, setActiveTab] = useState(0);
    const [order, setOrder] = useState({
        supplierId: "",
        facilityId: "",
        deliveryCost: "",
        note: "",
        orderName: "",
        tax: "",
        amount: "",
        numberOfInvoices: 0,
        deliveryAfterDate: "",
        deliveryBeforeDate: "",
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
                note: "Đơn hàng tạo từ gợi ý dự báo tồn kho thấp"
            }));
            toast.info(`Đã thêm ${suggestedItems.length} sản phẩm từ dự báo tồn kho thấp`);
        }
    }, [suggestedItems]);

    const handleSubmit = async () => {
        if (!order.facilityId) {
            toast.warning("Vui lòng chọn kho hàng");
            return;
        }
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
            await createPurchaseOrder(order);
        } catch (error) {
            console.error("Error creating purchase order:", error);
            toast.error("Lỗi khi tạo đơn hàng: " + (error.message || "Lỗi không xác định"));
        }
    };

    const handleTabChange = (event, newValue) => {
        setActiveTab(newValue);
    };

    // Chuẩn bị dữ liệu cho ProductChart (biểu đồ cột)
    const chartProducts = forecastData.map(item => ({
        productName: item.productName,
        quantity: item.totalPredictedQuantity || item.quantity || 0
    }));

    return (
        <OrderFormProvider value={{ order, setOrder, entities, setEntities }}>
            <Box p={3}>
                <Typography sx={{ fontWeight: 700 }} variant="h5" gutterBottom>
                    Tạo đơn hàng mua
                    {suggestedItems.length > 0 && (
                        <Chip 
                            icon={<InsightsIcon />}
                            label="Từ dự báo hàng tồn" 
                            color="secondary" 
                            sx={{ ml: 2 }}
                        />
                    )}
                </Typography>

                {/* Hiển thị thông tin dự báo với tabs */}
                {forecastData.length > 0 && (
                    <Card sx={{ mb: 3, backgroundColor: "#f5f5f5" }}>
                        <CardContent>
                            <Typography variant="subtitle1" fontWeight={600} gutterBottom>
                                <InsightsIcon sx={{ mr: 1, verticalAlign: "middle" }} />
                                Thông tin dự báo
                            </Typography>
                            <Typography variant="body2" color="text.secondary" gutterBottom>
                                Dự báo tiêu thụ dựa trên dữ liệu lịch sử xuất kho và mô hình ARIMA.
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
                                        label="Chi tiết dự báo" 
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
                                                                Dự báo: {item.totalPredictedQuantity || item.quantity} {item.unit}
                                                            </Typography>
                                                            <Typography variant="body2">
                                                                Giá: {item.price?.toLocaleString()} VND
                                                            </Typography>
                                                            <Typography variant="caption" color="textSecondary">
                                                                Tồn kho: {item.currentStock || 0}
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
                                            title="Dự báo nhu cầu 7 ngày tới"
                                            color="#2196f3"
                                            emptyMessage="Không có dữ liệu dự báo"
                                        />
                                    </Grid>
                                </Grid>
                            </TabPanel>

                            {/* Tab 2: Chi tiết với ProductForecastChart (biểu đồ đường ARIMA) */}
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

                {/* Tìm kiếm và bảng sản phẩm */}
                <Box mt={3}>
                    <ProductSearch />
                    <ProductTable />
                </Box>

                {/* Nút lưu */}
                <Box mt={3} textAlign="right">
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleSubmit}
                        size="large"
                    >
                        Lưu đơn hàng
                    </Button>
                </Box>
            </Box>
        </OrderFormProvider>
    );
};

export default CreatePurchaseOrder;