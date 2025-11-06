import {
  Document,
  Font,
  Page,
  Text,
  View,
  StyleSheet,
} from "@react-pdf/renderer";

// Register font
Font.register({
  family: "Inter",
  fonts: [
    {
      src: "https://cdnjs.cloudflare.com/ajax/libs/ink/3.1.10/fonts/Roboto/roboto-light-webfont.ttf",
      fontWeight: "normal",
    },
    {
      src: "https://cdnjs.cloudflare.com/ajax/libs/ink/3.1.10/fonts/Roboto/roboto-regular-webfont.ttf",
      fontWeight: "bold",
    },
  ],
});

const InvoicePDF = ({ invoiceData }) => {
  const styles = StyleSheet.create({
    page: {
      fontFamily: "Inter",
      fontSize: 12,
      padding: 40,
      flexGrow: 1,
    },
    header: {
      textAlign: "center",
      fontWeight: "bold",
      fontSize: 25,
      marginBottom: 20,
    },
    subHeader: {
      textAlign: "center",
      fontSize: 14,
      marginBottom: 10,
      color: "#666",
    },
    section: {
      marginBottom: 15,
    },
    sectionTitle: {
      fontWeight: "bold",
      fontSize: 14,
      marginBottom: 8,
      textDecoration: "underline",
    },
    infoRow: {
      display: "flex",
      flexDirection: "row",
      marginBottom: 5,
    },
    infoLabel: {
      width: "30%",
      fontWeight: "bold",
    },
    infoValue: {
      width: "70%",
    },
    table: {
      width: "100%",
      marginTop: 20,
      marginBottom: 20,
    },
    tableRow: {
      display: "flex",
      flexDirection: "row",
      borderTop: "1px solid #EEE",
      paddingTop: 8,
      paddingBottom: 8,
    },
    tableHeaderRow: {
      borderTop: "2px solid #333",
      borderBottom: "2px solid #333",
      backgroundColor: "#f5f5f5",
      fontWeight: "bold",
    },
    tableCol1: { width: "8%", fontSize: 10 }, // STT
    tableCol2: { width: "35%", fontSize: 10 }, // Tên sản phẩm
    tableCol3: { width: "10%", fontSize: 10 }, // ĐVT
    tableCol4: { width: "12%", fontSize: 10, textAlign: "center" }, // Số lượng
    tableCol5: { width: "17%", fontSize: 10, textAlign: "right" }, // Đơn giá
    tableCol6: { width: "18%", fontSize: 10, textAlign: "right" }, // Thành tiền
    summarySection: {
      marginTop: 20,
      alignItems: "flex-end",
    },
    summaryRow: {
      display: "flex",
      flexDirection: "row",
      justifyContent: "space-between",
      width: "60%",
      marginBottom: 5,
    },
    summaryLabel: {
      fontWeight: "bold",
      width: "70%",
    },
    summaryValue: {
      width: "30%",
      textAlign: "right",
    },
    totalRow: {
      display: "flex",
      flexDirection: "row",
      justifyContent: "space-between",
      width: "60%",
      marginTop: 10,
      paddingTop: 10,
      borderTop: "2px solid #333",
      fontWeight: "bold",
      fontSize: 14,
    },
    signaturesSection: {
      marginTop: 40,
      display: "flex",
      flexDirection: "row",
      justifyContent: "space-between",
    },
    signatureBox: {
      width: "30%",
      textAlign: "center",
    },
    signatureTitle: {
      fontWeight: "bold",
      marginBottom: 40,
    },
    signatureLine: {
      borderTop: "1px solid #333",
      paddingTop: 5,
      fontSize: 10,
    },
    footer: {
      position: "absolute",
      bottom: 30,
      left: 40,
      right: 40,
      textAlign: "center",
      fontSize: 8,
      color: "#666",
      borderTop: "1px solid #ddd",
      paddingTop: 10,
    },
    spacing: {
      marginTop: 10,
      marginBottom: 10,
    },
  });

  // Helper functions
  const formatCurrency = (amount) => {
    if (!amount || isNaN(amount)) return '0 ₫';
    try {
      return new Intl.NumberFormat('vi-VN').format(amount) + ' ₫';
    } catch (error) {
      return amount.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') + ' ₫';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('vi-VN');
    } catch (error) {
      return 'N/A';
    }
  };

  // Check data validity
  if (!invoiceData) {
    return (
      <Document>
        <Page size="A4" style={styles.page}>
          <Text style={styles.header}>KHÔNG CÓ DỮ LIỆU</Text>
          <Text style={styles.subHeader}>Vui lòng kiểm tra lại</Text>
        </Page>
      </Document>
    );
  }

  // Determine invoice type
  const isCustomerInvoice = invoiceData.customerInfo && !invoiceData.supplierInfo;
  const partner = isCustomerInvoice ? invoiceData.customerInfo : invoiceData.supplierInfo;
  const invoiceTitle = isCustomerInvoice ? 'Hóa đơn bán hàng' : 'Hóa đơn mua hàng';

  return (
    <Document>
      <Page size="A4" style={styles.page}>
        {/* Header */}
        <Text style={styles.header}>{invoiceTitle}</Text>
        <Text style={styles.subHeader}>Mã phiếu: {invoiceData.id || 'N/A'}</Text>
        <Text style={styles.subHeader}>Ngày tạo: {formatDate(invoiceData.createdStamp)}</Text>

        {/* Partner Info */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>
            {isCustomerInvoice ? 'THÔNG TIN KHÁCH HÀNG' : 'THÔNG TIN NHÀ CUNG CẤP'}
          </Text>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Tên:</Text>
            <Text style={styles.infoValue}>{partner?.name || 'Chưa có thông tin'}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Số điện thoại:</Text>
            <Text style={styles.infoValue}>{partner?.phone || 'Chưa có thông tin'}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Email:</Text>
            <Text style={styles.infoValue}>{partner?.email || 'Chưa có thông tin'}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Địa chỉ:</Text>
            <Text style={styles.infoValue}>{partner?.fullAddress || partner?.address || 'Chưa có thông tin'}</Text>
          </View>
        </View>

        {/* Items Table */}
        <View style={styles.table}>
          {/* Table Header */}
          <View style={[styles.tableRow, styles.tableHeaderRow]}>
            <Text style={styles.tableCol1}>STT</Text>
            <Text style={styles.tableCol2}>Tên sản phẩm</Text>
            <Text style={styles.tableCol3}>ĐVT</Text>
            <Text style={styles.tableCol4}>SL</Text>
            <Text style={styles.tableCol5}>Đơn giá</Text>
            <Text style={styles.tableCol6}>Thành tiền</Text>
          </View>

          {/* Table Rows */}
          {invoiceData.items && invoiceData.items.length > 0 ? (
            invoiceData.items.map((item, index) => (
              <View style={styles.tableRow} key={item.id || index}>
                <Text style={styles.tableCol1}>{index + 1}</Text>
                <Text style={styles.tableCol2}>{item.productName || 'N/A'}</Text>
                <Text style={styles.tableCol3}>{item.unit || 'Cái'}</Text>
                <Text style={styles.tableCol4}>{item.quantity || 0}</Text>
                <Text style={styles.tableCol5}>{formatCurrency(item.price || 0)}</Text>
                <Text style={styles.tableCol6}>{formatCurrency(item.amount || 0)}</Text>
              </View>
            ))
          ) : (
            <View style={styles.tableRow}>
              <Text style={{ width: '100%', textAlign: 'center' }}>Không có sản phẩm nào</Text>
            </View>
          )}
        </View>

        {/* Summary */}
        <View style={styles.summarySection}>
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Tổng số mặt hàng:</Text>
            <Text style={styles.summaryValue}>
              {invoiceData.items ? invoiceData.items.length : 0} mặt hàng
            </Text>
          </View>
          
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Tổng số lượng:</Text>
            <Text style={styles.summaryValue}>{invoiceData.totalQuantity || 0} sản phẩm</Text>
          </View>
          
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Tạm tính:</Text>
            <Text style={styles.summaryValue}>{formatCurrency(invoiceData.subtotal || 0)}</Text>
          </View>

          {(invoiceData.totalDiscount || 0) > 0 && (
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Tổng giảm giá:</Text>
              <Text style={styles.summaryValue}>- {formatCurrency(invoiceData.totalDiscount)}</Text>
            </View>
          )}

          {(invoiceData.totalTax || 0) > 0 && (
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Tổng thuế VAT:</Text>
              <Text style={styles.summaryValue}>{formatCurrency(invoiceData.totalTax)}</Text>
            </View>
          )}

          <View style={styles.totalRow}>
            <Text style={styles.summaryLabel}>TỔNG CỘNG:</Text>
            <Text style={styles.summaryValue}>
              {formatCurrency(invoiceData.totalAmount || 0)}
            </Text>
          </View>
        </View>
        {/* Footer */}
        <View style={styles.footer}>
          <Text>
            Phiếu {isCustomerInvoice ? 'xuất' : 'nhập'} được tạo tự động bởi hệ thống WMS • 
            In lúc: {new Date().toLocaleString('vi-VN')}
          </Text>
        </View>
      </Page>
    </Document>
  );
};

export default InvoicePDF;