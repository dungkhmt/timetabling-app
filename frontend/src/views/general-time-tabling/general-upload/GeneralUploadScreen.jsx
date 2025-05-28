import { useState, useCallback, useEffect, useMemo, useRef } from "react";
import { LoadingProvider } from "./contexts/LoadingContext";
import GeneralSemesterAutoComplete from "../common-components/GeneralSemesterAutoComplete";
import GeneralClusterAutoComplete from "../common-components/GeneralClusterAutoComplete";
import InputFileUpload from "./components/InputFileUpload";
import { Button } from "@mui/material";
import { FacebookCircularProgress } from "components/common/progressBar/CustomizedCircularProgress";
import GeneralUploadTable from "./components/GeneralUploadTable";
import { useGeneralSchedule } from "services/useGeneralScheduleData";
import { request } from "api";

const GeneralUploadScreen = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [selectedIds, setSelectedIds] = useState([]);
  const [isUserEditing, setIsUserEditing] = useState(false);
  const [selectedCluster, setSelectedCluster] = useState(null);
  const [filteredClasses, setFilteredClasses] = useState([]);
  const [isLoadingData, setIsLoadingData] = useState(false);
  const initialRender = useRef(true);
  
  const { 
    states,
    states: { 
      selectedSemester,
      classesNoSchedule,
      isClassesNoScheduleLoading,
      isDeletingBySemester,
      isDeletingByIds,
      isUploading,
      isLoadingClusterClasses
    },
    setters: { 
      setSelectedSemester,
      setClassesNoSchedule,
      setSelectedRows 
    },
    handlers: {
      handleDeleteBySemester,
      uploadFile, 
      handleDeleteByIds,
      getClassesByCluster
    }
  } = useGeneralSchedule();

  function handleAssignSessionForSummer(){
    let body = {
      semester: selectedSemester.semester
    };

    request(
      "post",
      "/general-classes/assign-session-to-classes-for-summer-semester",
      (res) => {
        console.log('Assign session to classes ',res.data);
        setSelectedIds([]);
        setSelectedRows([]);
      },
      {
        onError: (e) => {
          setSelectedIds([]);
          setSelectedRows([]);
        }
      },
      body
    );
  }

  
  function handleCreateSegmentForSummer(){
    let body = {
      semester: selectedSemester.semester
    };

    request(
      "post",
      "/general-classes/create-class-segments-for-summer-semester",
      (res) => {
        console.log('create class-segments returned ',res.data);
        setSelectedIds([]);
        setSelectedRows([]);
      },
      {
        onError: (e) => {
          setSelectedIds([]);
          setSelectedRows([]);
        }
      },
      body
    );
  }
  function handleComputeClusters(){
    let body = {
      semester: selectedSemester.semester
    };

    request(
      "post",
      "/general-classes/compute-class-cluster",
      (res) => {
        console.log('compute cluster returned ',res.data);
        // Clear selection after operation completes
        setSelectedIds([]);
        setSelectedRows([]);
      },
      {
        onError: (e) => {
          setSelectedIds([]);
          setSelectedRows([]);
        }
      },
      body
    );
  }
  const handleSelectionChange = (newSelection) => {
    setSelectedIds(newSelection);
    setSelectedRows(newSelection);
  };

  const handleSubmitFile = async () => {
    if (selectedFile) {
      await uploadFile(selectedSemester.semester, selectedFile); // Changed from handleUploadFile
      setSelectedFile(null);
      setSelectedIds([]);
    }
  };

  const handleDeleteSelectedRows = async () => {
    const success = await handleDeleteByIds();
    if (success) {
      setSelectedIds([]);
    }
  };

  const handleDeleteSemester = async () => {
    const success = await handleDeleteBySemester();
    if (success) {
      setSelectedIds([]);
    }
  };

  const handleRefreshData = useCallback(async () => {
    if (isUserEditing) {
      console.log("Skipping refresh while user is editing");
      return;
    }
    
    try {
      await states.refetchNoSchedule();
      console.log("Data refreshed in GeneralUploadScreen");
    } catch (error) {
      console.error("Error refreshing data:", error);
    }
  }, [states, isUserEditing]);

  useEffect(() => {
    return () => {
      setIsUserEditing(false);
    };
  }, []);

  // Clear selected rows when semester changes
  useEffect(() => {
    setSelectedIds([]);
    setSelectedRows([]);
    // Force clear the filtered classes immediately
    setFilteredClasses([]);
    // Reset cluster selection
    setSelectedCluster(null);
  }, [selectedSemester, setSelectedRows]);
  
  // Ensure classesNoSchedule gets cleared too when semester changes
  // We need to make sure this only runs once when the semester changes
  useEffect(() => {
    if (initialRender.current) {
      // Skip the first render
      initialRender.current = false;
      return;
    }
    
    if (selectedSemester) {
      setClassesNoSchedule(() => []);
      
    }
  }, [selectedSemester?.semester]); 

  useEffect(() => {
    const filterClassesByCluster = async () => {
      setIsLoadingData(true);
      try {
        if (selectedCluster) {
          console.log("Filtering classes for cluster:", selectedCluster.id, selectedCluster.name);
          // Explicitly passing null as versionId
          const clusterClasses = await getClassesByCluster(selectedCluster.id, null);
          console.log("Received cluster classes:", clusterClasses ? clusterClasses.length : 0);
          setFilteredClasses(clusterClasses || []);
        } else {
          console.log("No cluster selected, clearing filtered classes");
          setFilteredClasses([]);
        }
      } catch (error) {
        console.error("Error fetching filtered classes:", error);
        setFilteredClasses([]);
      } finally {
        setIsLoadingData(false);
      }
    };
    
    if (selectedSemester?.semester) {
      filterClassesByCluster();
    } else {
      setFilteredClasses([]);
      setIsLoadingData(false);
    }
  }, [selectedCluster, getClassesByCluster, selectedSemester]);

  const tableLoadingState = isClassesNoScheduleLoading || isLoadingData || isLoadingClusterClasses;

  const displayClasses = useMemo(() => {
    if (tableLoadingState) {
      console.log("Returning empty array because loading");
      return [];
    }
    
    if (selectedCluster !== null) {
      console.log("Returning filtered classes for cluster");
      return filteredClasses || [];
    }
    
    console.log("Returning general classes");
    return classesNoSchedule || [];
  }, [
    selectedCluster, 
    filteredClasses, 
    classesNoSchedule, 
    tableLoadingState
  ]);

  return (
    <LoadingProvider>
      <div className="flex flex-col gap-2">
        <div className="flex flex-row gap-2 justify-between">
          <div className="flex gap-2">
            <GeneralSemesterAutoComplete
              selectedSemester={selectedSemester}
              setSelectedSemester={(semester) => {
                setSelectedSemester(semester);
                setSelectedCluster(null); 
              }}
            />
            <GeneralClusterAutoComplete
              selectedCluster={selectedCluster}
              setSelectedCluster={setSelectedCluster}
              selectedSemester={selectedSemester}
              sx={{
                minWidth: 200,
                "& .MuiInputBase-root": { height: "40px" },
              }}
            />
          </div>
          <div className="flex flex-col gap-2 items-end">
            <InputFileUpload
              isUploading={isUploading}
              selectedSemester={selectedSemester}
              selectedFile={selectedFile}
              setSelectedFile={setSelectedFile}
              submitHandler={handleSubmitFile}
            />
            <div className="flex flex-wrap gap-2 text-[16px] justify-end">
              <Button
                startIcon={isDeletingByIds ? <FacebookCircularProgress /> : null}
                sx={{ 
                  minWidth: '180px',
                  textTransform: 'none',
                  fontSize: '14px'
                }}
                disabled={isDeletingByIds || selectedIds.length === 0}
                onClick={handleDeleteSelectedRows}
                variant="contained"
                color="error"
              >
                Xóa các lớp đã chọn ({selectedIds.length})
              </Button>
              <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  minWidth: '180px',
                  textTransform: 'none',
                  fontSize: '14px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleDeleteSemester}
                variant="contained"
                color="error"
              >
                Xóa danh sách theo kỳ
              </Button>

              <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  minWidth: '120px',
                  textTransform: 'none',
                  fontSize: '14px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleComputeClusters}
                variant="contained"
                color="primary"
              >
                Phân cụm
              </Button>
              {/* <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  minWidth: '120px',
                  textTransform: 'none',
                  fontSize: '14px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleRemoveSegment}
                variant="contained"
                color="secondary"
              >
                Xóa ca học
              </Button>
              <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  minWidth: '120px',
                  textTransform: 'none',
                  fontSize: '14px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleCreateSegment}
                variant="contained"
                color="info"
              >
                Tạo ca học
              </Button> */}
              <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  minWidth: '140px',
                  textTransform: 'none',
                  fontSize: '14px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleAssignSessionForSummer}
                variant="contained"
                color="success"
              >
                Thiết lập buổi học
              </Button>
              <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  minWidth: '140px',
                  textTransform: 'none',
                  fontSize: '14px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleCreateSegmentForSummer}
                variant="contained"
                color="success"
              >
                Tạo ca học (kỳ hè)
              </Button>
            </div>
          </div>
        </div>
        <GeneralUploadTable 
          setClasses={setClassesNoSchedule}
          classes={displayClasses} 
          dataLoading={tableLoadingState}
          onSelectionChange={handleSelectionChange} 
          selectedIds={selectedIds}
          onRefreshNeeded={handleRefreshData}
          setIsEditing={setIsUserEditing}
        />
      </div>
    </LoadingProvider>
  );
};

export default GeneralUploadScreen;