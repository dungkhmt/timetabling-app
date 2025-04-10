import React, { useState, useEffect } from "react";
import {
    Box,
    Button,
    Grid,
    Typography,
    Chip,
    Card,
    CardContent,
} from "@mui/material";
import { useLocation, useHistory } from "react-router-dom"; // React Router v5
import { toast } from "react-toastify";
import { useWms2Data } from "services/useWms2Data";
import { OrderFormProvider } from "../common/context/OrderFormContext";
import BasicInfoForm from "./components/BasicInfoForm";
import DeliveryInfoForm from "./components/DeliveryInfoForm";
import ProductSearch from "../common/components/ProductSearch";
import ProductTable from "../common/components/ProductTable";
import InsightsIcon from "@mui/icons-material/Insights";

const CreatePurchaseOrder = () => {
    const { createPurchaseOrder } = useWms2Data();
    const location = useLocation();
    const history = useHistory(); // Use useHistory from React Router v5
    const suggestedItems = location.state?.suggestedItems || [];
    const forecastData = location.state?.forecastData || [];

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

                {forecastData.length > 0 && (
                    <Card sx={{ mb: 3, backgroundColor: "#f5f5f5" }}>
                        <CardContent>
                            <Typography variant="subtitle1" fontWeight={600} gutterBottom>
                                <InsightsIcon sx={{ mr: 1, verticalAlign: "middle" }} />
                                Thông tin dự báo
                            </Typography>
                            <Typography variant="body2" color="text.secondary" gutterBottom>
                                Dự báo tiêu thụ ngày mai dựa trên dữ liệu lịch sử xuất kho.
                            </Typography>
                            <Grid container spacing={2} mt={1}>
                                {forecastData.slice(0, 3).map((item) => (
                                    <Grid item xs={12} md={4} key={item.productId}>
                                        <Card variant="outlined">
                                            <CardContent>
                                                <Typography variant="subtitle2" noWrap>
                                                    {item.productName}
                                                </Typography>
                                                <Typography variant="body2" color="primary">
                                                    Dự báo: {item.quantity} {item.unit}
                                                </Typography>
                                                <Typography variant="body2">
                                                    Giá: {item.price} VND
                                                </Typography>
                                            </CardContent>
                                        </Card>
                                    </Grid>
                                ))}
                            </Grid>
                        </CardContent>
                    </Card>
                )}

                <Grid container spacing={3}>
                    <Grid item xs={12} md={6}>
                        <BasicInfoForm />
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <DeliveryInfoForm />
                    </Grid>
                </Grid>

                <Box mt={3}>
                    <ProductSearch />
                    <ProductTable />
                </Box>

                <Box mt={3} textAlign="right">
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleSubmit}
                    >
                        Lưu
                    </Button>
                </Box>
            </Box>
        </OrderFormProvider>
    );
};

export default CreatePurchaseOrder;