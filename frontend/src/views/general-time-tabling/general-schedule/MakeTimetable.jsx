import React, {useEffect, useState} from "react";
import TimeTableNew from "./components/TimeTableNew";
import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import {request} from "api";
export default function MakeTimetable(){
    const {versionId} = useParams();
    const [classes, setClasses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [selectedSemester, setSelectedSemester] = useState(null);
    const [selectedGroup, setSelectedGroup] = useState(null);
    const [selectedRows, setSelectedRows] = useState([]);
    const [selectedVersion, setSelectedVersion] = useState(null);
    const [numberSlotsToDisplay, setNumberSlotsToDisplay] = useState(6);
    function getClasses(){
        setLoading(true);
        request(
                            "get",
                            "/general-classes/get-class-segments-of-version?versionId=" + versionId,
                            (res)=>{
                                console.log(res);
                                setClasses(res.data || []);
                                setLoading(false);
                            },
                            (error)=>{
                                console.error(error);
                                setError(error);
                            },
                        );

    }
    function onSaveSuccess(){

    }
    useEffect(() => {
        getClasses();
    },[]);

    return(
        <>
            Version {versionId}
            <TimeTableNew 
                selectedSemester={selectedSemester}
                classes={classes}
                getClasses = {getClasses}
                versionId={versionId}
                selectedGroup={selectedGroup}
                onSaveSuccess={onSaveSuccess}
                loading={loading}
                selectedRows={selectedRows}
                onSelectedRowsChange={setSelectedRows}
                selectedVersion={selectedVersion}
                numberSlotsToDisplay={numberSlotsToDisplay}
            />
        </>
    );
}