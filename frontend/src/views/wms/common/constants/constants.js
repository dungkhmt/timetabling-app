import dayjs from "dayjs";


export const ORDER_STATUSES = {
  DRAFT: {
    display: "Nháp",
    allowApprove: true,
    allowCancel: true,
    allowEdit: true,
    allowDiscount: true,
  },
  CREATED: {
    display: "Mới tạo",
    allowApprove: true,
    allowCancel: true,
    allowEdit: true,
    allowDiscount: true,
  },
  WAITING_FOR_APPROVAL: {
    display: "Chờ duyệt",
    allowApprove: true,
    allowCancel: true,
    allowEdit: true,
    allowDiscount: true,
  },
  APPROVED: {
    display: "Đã duyệt",
    allowApprove: false,
    allowCancel: true,
    allowEdit: false,
    allowDiscount: true,
  },
  DELIVERED: {
    display: "Đã giao hàng",
    allowApprove: false,
    allowCancel: false,
    allowEdit: false,
    allowDiscount: false,
  },
  COMPLETED: {
    display: "Hoàn thành",
    allowApprove: false,
    allowCancel: false,
    allowEdit: false,
    allowDiscount: false,
  },
  CANCELED: {
    display: "Đã hủy",
    allowApprove: false,
    allowCancel: false,
    allowEdit: false,
    allowDiscount: false,
  },
  REJECTED: {
    display: "Từ chối",
    allowApprove: false,
    allowCancel: false,
    allowEdit: false,
    allowDiscount: false,
  },
};

export const SALE_CHANNELS = {
  FACEBOOK: "Facebook",
  SHOPEE: "Shopee",
  TIKTOK: "Tiktok",
  WEBSITE: "Website",
  OTHER: "Khác",
  POS: "POS",
};

export const SALE_ORDER_STATUSES = {
  DRAFT : "Nháp",
  CREATED: "Đã tạo",
  WAITING_FOR_APPROVAL: "Chờ duyệt",
  APPROVED : "Đã duyệt",
  DELIVERED: "Đã giao hàng",
  CANCELLED : 'Đã hủy',
  REJECTED : 'Từ chối',
  COMPLETED : 'Hoàn thành',
  SHIPPING : 'Đang giao hàng',
}

export const SALE_ORDER_TABS = [
  {
    value: "ALL",
    label: "Tất cả",
  },
  {
    value: "CREATED",
    label: "Chờ duyệt",
  },
  {
    value: "APPROVED",
    label: "Đã duyệt",
  },
  {
    value: "CANCELLED",
    label: "Đã hủy",
  },
]

// Định nghĩa schema cho hàng tiêu đề và dữ liệu
export const SALE_ORDER_SCHEMA = [
  {
    column: 'Mã đơn hàng',
    type: String,
    value: order => order.id
  },
  {
    column: 'Tên đơn hàng',
    type: String,
    value: order => order.orderName || ''
  },
  {
    column: 'Khách hàng',
    type: String,
    value: order => order.customerName
  },
  {
    column: 'Trạng thái',
    type: String,
    value: order => order.status
  },
  {
    column: 'Ngày tạo',
    type: String,
    value: order => dayjs(order.createdStamp).format('DD/MM/YYYY HH:mm')
  },
  {
    column: 'Địa chỉ giao hàng',
    type: String,
    value: order => order.deliveryAddress || ''
  },
  {
    column: 'SĐT giao hàng',
    type: String,
    value: order => order.deliveryPhone || ''
  },
  {
    column: 'Ngày giao dự kiến',
    type: String,
    value: order => order.deliveryAfterDate ? dayjs(order.deliveryAfterDate).format('DD/MM/YYYY') : ''
  },
  {
    column: 'Kênh bán hàng',
    type: String,
    value: order => order.saleChannelId || ''
  },
  {
    column: 'Tổng số lượng',
    type: Number,
    value: order => order.totalQuantity
  },
  {
    column: 'Tổng tiền',
    type: Number,
    format: '0,0.00₫',
    value: order => order.totalAmount
  }
];

export const entityTypes = {
    FACILITIES: "facilities",
    CUSTOMERS: "customers",
    PRODUCTS: "products",
    SUPPLIERS: "suppliers",
};

export const PURCHASE_ORDER_STATUSES = {
  DRAFT : "Nháp",
  CREATED: "Đã tạo",
  WAITING_FOR_APPROVAL: "Chờ duyệt",
  APPROVED : "Đã duyệt",
  DELIVERED: "Đã giao hàng",
  CANCELLED : 'Đã hủy',
  REJECTED : 'Từ chối',
  COMPLETED : 'Hoàn thành',
  SHIPPING : 'Đang giao hàng',
};

export const PURCHASE_ORDER_TABS = [
  {
    value: "ALL",
    label: "Tất cả",
  },
  {
    value: "CREATED",
    label: "Chờ duyệt",
  },
  {
    value: "APPROVED",
    label: "Đã duyệt",
  },
  {
    value: "CANCELLED",
    label: "Từ chối",
  },
];

// Schema cho xuất excel đơn hàng mua
export const PURCHASE_ORDER_SCHEMA = [
  {
    column: 'Mã đơn hàng',
    type: String,
    value: order => order.id
  },
  {
    column: 'Ngày tạo',
    type: String,
    value: order => new Date(order.createdStamp).toLocaleDateString("vi-VN")
  },
  {
    column: 'Nhà cung cấp',
    type: String,
    value: order => order.supplierName || ''
  },
  {
    column: 'Số lượng',
    type: Number,
    value: order => order.totalQuantity || 0
  },
  {
    column: 'Thành tiền',
    type: String,
    value: order => new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(order.totalAmount)
  },
  {
    column: 'Trạng thái',
    type: String,
    value: order => PURCHASE_ORDER_STATUSES[order.status] || 'Không xác định'
  }
];

export const ORDER_TYPE_ID = {
  SALES_ORDER: "SALES_ORDER",
  PURCHASE_ORDER: "PURCHASE_ORDER",
}

export const SHIPMENT_TYPE_ID = {
  INBOUND: "INBOUND",
    OUTBOUND: "OUTBOUND",
}

export const SHIPMENT_STATUSES = [
  { id: "CREATED", name: "Đã tạo" },
  { id: "PENDING", name: "Chờ xử lý" },
  { id: "EXPORTED", name: "Đã xuất kho" },
  { id: "SHIPPED", name: "Đang vận chuyển" },
  { id: "PARTIALLY_DELIVERED", name: "Giao một phần" },
  { id: "DELIVERED", name: "Đã giao hàng" },
  { id: "CANCELLED", name: "Đã hủy" },
  { id: "IMPORTED", name: "Đã nhập kho" },
];

export const SHIPPER_STATUSES = [
  { id: "DRIVING", name: "Đang lái xe" },
  { id: "ASSIGNED", name: "Đã phân công" },
  { id: "IN_TRIP", name: "Đang trong chuyến" },
  { id: "ACTIVE", name: "Hoạt động" },
  { id: "INACTIVE", name: "Không hoạt động" }
];

export const GREEDY = "GREEDY";
export const CWS = "CWS";