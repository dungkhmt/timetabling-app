import React, { useState } from "react";
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    TextField
} from "@mui/material";
import { request } from "../../../../api";
import keycloak from "../../../../config/keycloak";

export default function AddBatchDialog({ open, onClose, semester, onBatchAdded }) {
    const [batchName, setBatchName] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleSave = () => {
        if (!batchName.trim()) {
            alert("Vui lòng nhập tên batch");
            return;
        }

        setIsLoading(true);

        const payload = {
            name: batchName,  // Đổi thành "name"
            semester: semester,
            createdByUserId: keycloak.tokenParsed?.preferred_username || "unknown"  // Đổi thành "createdByUserId"
        };
        // alert(JSON.stringify(payload));

        request(
            "post",
            "/teacher-assignment-batch/create-batch", // Thay bằng endpoint thực tế để tạo batch
            (res) => {
                console.log("Tạo batch thành công:", res);
                setIsLoading(false);
                setBatchName("");
                if (onBatchAdded) {
                    onBatchAdded(res.data); // Truyền batch mới tạo về component cha nếu cần
                }
                onClose();
            },
            (error) => {
                console.error("Tạo batch thất bại:", error);
                setIsLoading(false);
                alert("Tạo batch thất bại. Vui lòng thử lại.");
            },
            payload
        );
    };

    const handleClose = () => {
        setBatchName("");
        onClose();
    };

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
            <DialogTitle>
                Thêm Batch Mới
            </DialogTitle>

            <DialogContent>
                <TextField
                    autoFocus
                    margin="dense"
                    label="Tên Batch *"
                    type="text"
                    fullWidth
                    variant="outlined"
                    value={batchName}
                    onChange={(e) => setBatchName(e.target.value)}
                    sx={{ mt: 2 }}
                    disabled={isLoading}
                />
            </DialogContent>

            <DialogActions>
                <Button onClick={handleClose} color="secondary" disabled={isLoading}>
                    Hủy
                </Button>
                <Button
                    onClick={handleSave}
                    color="primary"
                    variant="contained"
                    disabled={!batchName.trim() || isLoading}
                >
                    {isLoading ? "Đang xử lý..." : "Thêm"}
                </Button>
            </DialogActions>
        </Dialog>
    );
}