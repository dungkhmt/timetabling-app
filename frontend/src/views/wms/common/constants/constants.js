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
    label: "Mới tạo",
  },
  {
    value: "WAITING_FOR_APPROVAL",
    label: "Chờ duyệt",
  },
  {
    value: "APPROVED",
    label: "Đã duyệt",
  },
  {
    value: "REJECTED",
    label: "Từ chối",
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
}