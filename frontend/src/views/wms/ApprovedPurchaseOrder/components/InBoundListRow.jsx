import React from "react";
import { TableRow, TableCell } from "@mui/material";
import { useHistory, useLocation } from "react-router-dom";

const InBoundListRow = ({ row, isSmall }) => {
  const navigate = useHistory();
  const location = useLocation();
  return (
    <TableRow onClick={() => navigate.push(location.pathname+ `/inbound/${row.id}`)}>
      <TableCell>{row.id}</TableCell>
        <TableCell>{row.shipmentName || "N/A"}</TableCell>
      <TableCell>{row.supplierName || "N/A"}</TableCell>
      <TableCell>{row.expectedDeliveryDate || "N/A"}</TableCell>
      <TableCell>{row.statusId || "N/A"}</TableCell>
    </TableRow>
  );
};

export default InBoundListRow;