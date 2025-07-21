import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import { toast } from "react-toastify";
import React, {useEffect, useState} from "react";
import {
    Button, Dialog, DialogContent, DialogTitle, Paper, TextField, Autocomplete, CircularProgress,
    FormControl, MenuItem, InputLabel, Select, DialogActions, Box

} from "@mui/material";
import GeneralSemesterAutoComplete from "../common-components/GeneralSemesterAutoComplete";
import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import { request } from "api";

export default function RoomsOfBatch({batchId}){
    const [rooms, setRooms] = useState([]);

    return (
        <>
            Rooms of batch {batchId}
            <Button>
                ADD
            </Button>
            
        </>
    );
};