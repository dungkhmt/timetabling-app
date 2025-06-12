import React from "react";
import { TableRow, TableCell } from "@mui/material";
import { useHistory, useLocation } from "react-router-dom";
import { getShipmentStatus } from "views/wms/common/utils/functions";

const OutBoundListRow = ({ row, isSmall }) => {
  const navigate = useHistory();
  const location = useLocation();
  return (
    <TableRow onClick={() => navigate.push(location.pathname+ `/outbound/${row.id}`)}>
        <TableCell>{row.id}</TableCell>
             <TableCell>{row.shipmentName || "N/A"}</TableCell>
           <TableCell>{row.toCustomerName || "N/A"}</TableCell>
           <TableCell>{row.expectedDeliveryDate || "N/A"}</TableCell>
           <TableCell>{row.totalWeight || "N/A"}</TableCell>
           <TableCell>{row.totalQuantity || "N/A"}</TableCell>
           <TableCell>{getShipmentStatus(row.statusId) || "N/A"}</TableCell>
    </TableRow>
  );
};

export default OutBoundListRow;