import React, { useState, useEffect } from 'react';
import { Button, CircularProgress, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { Print, Cancel, Save } from '@mui/icons-material';
import { PDFViewer, pdf } from '@react-pdf/renderer';
import { saveAs } from 'file-saver';
import { toast } from 'react-toastify';
import InvoicePDF from './InvoicePDF';
import { useWms2Data } from 'services/useWms2Data';

const InvoicePrint = ({ shipmentId, disabled = false }) => {
  const { getInvoiceByShipmentId } = useWms2Data();
  const [invoiceData, setInvoiceData] = useState(null);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  const loadInvoiceData = async () => {
    if (!shipmentId) {
      toast.error('Không có mã phiếu xuất');
      return null;
    }

    setLoading(true);
    try {
      const response = await getInvoiceByShipmentId(shipmentId);
      if (response?.code === 200 && response?.data) {
        setInvoiceData(response.data);
        return response.data;
      } else {
        throw new Error(response?.message || 'Không thể tải dữ liệu hóa đơn');
      }
    } catch (error) {
      console.error('Error loading invoice data:', error);
      toast.error('Không thể tải dữ liệu hóa đơn: ' + (error.message || 'Lỗi không xác định'));
      return null;
    } finally {
      setLoading(false);
    }
  };

  const downloadPdf = async (data) => {
    try {
      console.log('Generating PDF with data:', data);
      const blob = await pdf(<InvoicePDF invoiceData={data} />).toBlob();
      saveAs(blob, `phieu-xuat-${data.id || 'unknown'}.pdf`);
      toast.success('Đã tải xuống phiếu xuất thành công');
    } catch (error) {
      console.error('Error generating PDF:', error);
      toast.error('Lỗi khi tạo file PDF');
    }
  };

  const handlePrint = async () => {
    try {
      if (!invoiceData) {
        toast.error('Không có dữ liệu để in');
        return;
      }

      const blob = await pdf(<InvoicePDF invoiceData={invoiceData} />).toBlob();
      const url = URL.createObjectURL(blob);
      const printWindow = window.open(url);

      if (printWindow) {
        printWindow.onload = () => {
          printWindow.print();
          URL.revokeObjectURL(url);
        };
        toast.success('Đã gửi phiếu xuất tới máy in');
        setPreviewOpen(false);
      } else {
        toast.error('Không thể mở cửa sổ in. Vui lòng kiểm tra popup blocker.');
      }
    } catch (error) {
      console.error('Error printing PDF:', error);
      toast.error('Lỗi khi in phiếu xuất');
    }
  };

  const handleSaveAndClose = async () => {
    if (invoiceData) {
      await downloadPdf(invoiceData);
    }
    setPreviewOpen(false);
  };

  const handleCancel = () => {
    setPreviewOpen(false);
  };

  const handleShowPreview = async () => {
    if (!invoiceData) {
      const data = await loadInvoiceData();
      if (!data) return;
    }
    setPreviewOpen(true);
  };

  return (
      <>
        {/* Main Print Button */}
        <Button
            variant="contained"
            color="primary"
            startIcon={loading ? <CircularProgress size={16} color="inherit" /> : <Print />}
            onClick={handleShowPreview}
            disabled={disabled || loading}
            sx={{ mr: 1 }}
        >
          {loading ? 'Đang tải...' : 'In phiếu xuất'}
        </Button>

        {/* Preview Dialog */}
        <Dialog
            open={previewOpen}
            onClose={handleCancel}
            maxWidth="lg"
            fullWidth
            PaperProps={{
              style: { height: '95vh' }
            }}
        >
          <DialogTitle sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            pb: 1
          }}>
            <span>Xem trước hóa đơn</span>
          </DialogTitle>

          <DialogContent style={{ padding: 0, overflow: 'hidden' }}>
            {invoiceData && (
                <PDFViewer
                    width="100%"
                    height="100%"
                    style={{ border: 'none' }}
                    showToolbar={true}
                >
                  <InvoicePDF invoiceData={invoiceData} />
                </PDFViewer>
            )}
          </DialogContent>

          <DialogActions sx={{ p: 2, gap: 1 }}>
            <Button
                onClick={handleCancel}
                variant="outlined"
                startIcon={<Cancel />}
                color="secondary"
            >
              Đóng
            </Button>

            <Button
                onClick={handleSaveAndClose}
                variant="outlined"
                startIcon={<Save />}
                color="primary"
            >
              Tải xuống
            </Button>

            <Button
                onClick={handlePrint}
                variant="contained"
                startIcon={<Print />}
                color="primary"
            >
              In ngay
            </Button>
          </DialogActions>
        </Dialog>
      </>
  );
};

export default InvoicePrint;