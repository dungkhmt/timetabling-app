const localText = {
  noRowsLabel: "Không có dữ liệu",
  noResultsOverlayLabel: "Không tìm thấy kết quả.",
  errorOverlayDefaultLabel: "Đã xảy ra lỗi.",
  
  // Toolbar
  toolbarDensity: "Độ dày",
  toolbarDensityLabel: "Độ dày",
  toolbarDensityCompact: "Nhỏ",
  toolbarDensityStandard: "Tiêu chuẩn",
  toolbarDensityComfortable: "Thoải mái",
  
  // Columns selector toolbar button text
  toolbarColumns: "Cột",
  toolbarColumnsLabel: "Chọn cột",
  
  // Filters toolbar button text
  toolbarFilters: "Bộ lọc",
  toolbarFiltersLabel: "Hiển thị bộ lọc",
  toolbarFiltersTooltipHide: "Ẩn bộ lọc",
  toolbarFiltersTooltipShow: "Hiện bộ lọc",
  toolbarFiltersTooltipActive: (count) =>
    count !== 1 ? `${count} bộ lọc đang hoạt động` : `1 bộ lọc đang hoạt động`,
  
  // Export selector toolbar button text
  toolbarExport: "Xuất",
  toolbarExportLabel: "Xuất",
  toolbarExportCSV: "Tải xuống CSV",
  toolbarExportPrint: "In",
  
  // Columns panel text
  columnsPanelTextFieldLabel: "Tìm cột",
  columnsPanelTextFieldPlaceholder: "Tiêu đề cột",
  columnsPanelDragIconLabel: "Sắp xếp lại cột",
  columnsPanelShowAllButton: "Hiện tất cả",
  columnsPanelHideAllButton: "Ẩn tất cả",
  
  // Filter panel text
  filterPanelAddFilter: "Thêm bộ lọc",
  filterPanelDeleteIconLabel: "Xóa",
  filterPanelOperators: "Toán tử",
  filterPanelOperatorAnd: "Và",
  filterPanelOperatorOr: "Hoặc",
  filterPanelColumns: "Cột",
  
  // Filter operators text
  filterOperatorContains: "chứa",
  filterOperatorEquals: "bằng",
  filterOperatorStartsWith: "bắt đầu với",
  filterOperatorEndsWith: "kết thúc với",
  filterOperatorIs: "là",
  filterOperatorNot: "không phải là",
  filterOperatorAfter: "sau",
  filterOperatorOnOrAfter: "trên hoặc sau",
  filterOperatorBefore: "trước",
  filterOperatorOnOrBefore: "trên hoặc trước",
  filterOperatorIsEmpty: "rỗng",
  filterOperatorIsNotEmpty: "không rỗng",
  
  // Pagination
  paginationRowsPerPage: "Số dòng trên trang:",
  paginationOf: "của",
  
  // Footer
  footerTotalRows: "Tổng số dòng:",
  
  // Selected rows count
  selectedRowCount: (count) => `${count} hàng đã chọn`,
};

export default localText;
