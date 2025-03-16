import React from "react";
import { TableRow, TableCell } from "@mui/material";
import { useHistory, useLocation } from "react-router-dom";

const OutBoundListRow = ({ row, isSmall }) => {
  const navigate = useHistory();
  const location = useLocation();
  return (
    <TableRow onClick={() => navigate.push(location.pathname+ `/outbound/${row.id}`)}>
      <TableCell>{row.id}</TableCell>
      <TableCell>{row.shipmentName || "N/A"}</TableCell>
      <TableCell>{row.customerName || "N/A"}</TableCell>
      <TableCell>{row.statusId || "N/A"}</TableCell>
    </TableRow>
  );
};

export default OutBoundListRow;