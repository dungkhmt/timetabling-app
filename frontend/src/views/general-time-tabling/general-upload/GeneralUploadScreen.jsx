import { useState, useCallback, useEffect } from "react";
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
  
  const { 
    states,
    states: { 
      selectedSemester,
      classesNoSchedule,
      isClassesNoScheduleLoading,
      isDeletingBySemester,
      isDeletingByIds,
      isUploading 
    },
    setters: { 
      setSelectedSemester,
      setClassesNoSchedule,
      setSelectedRows 
    },
    handlers: {
      handleDeleteBySemester,
      handleUploadFile,
      handleDeleteByIds,
      getClassesByCluster
    }
  } = useGeneralSchedule();

  function handleCreateSegment(){
    let body = {
      semester: selectedSemester.semester
    };

    request(
      "post",
      "/general-classes/create-class-segments",
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
  function handleRemoveSegment(){
    let body = {
      semester: selectedSemester.semester
    };

    request(
      "post",
      "/general-classes/remove-class-segments",
      (res) => {
        console.log('create class-segments returned ',res.data);
        // Clear selection after operation completes
        setSelectedIds([]);
        setSelectedRows([]);
      },
      {
        onError: (e) => {
          // Still clear selection even if there's an error
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
      await handleUploadFile(selectedFile);
      setSelectedFile(null);
      // Explicitly clear selection after upload
      setSelectedIds([]);
    }
  };

  const handleDeleteSelectedRows = async () => {
    const success = await handleDeleteByIds();
    if (success) {
      // Explicitly clear selection after deletion
      setSelectedIds([]);
    }
  };

  const handleDeleteSemester = async () => {
    const success = await handleDeleteBySemester();
    if (success) {
      // Explicitly clear selection after deletion
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

  useEffect(() => {
    const filterClassesByCluster = async () => {
      if (selectedCluster) {
        const clusterClasses = await getClassesByCluster(selectedCluster.id);
        setFilteredClasses(clusterClasses);
      } else {
        setFilteredClasses(classesNoSchedule);
      }
    };
    
    filterClassesByCluster();
  }, [selectedCluster, classesNoSchedule, getClassesByCluster]);

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
            <div className="flex gap-2 text-[16px]">
              <Button
                startIcon={isDeletingByIds ? <FacebookCircularProgress /> : null}
                sx={{ 
                  width: 220,
                  textTransform: 'none',
                  fontSize: '16px'
                }}
                disabled={isDeletingByIds || selectedIds.length === 0}
                onClick={handleDeleteSelectedRows} // Use our new wrapper function
                variant="contained"
                color="error"
              >
                Xóa các lớp đã chọn ({selectedIds.length})
              </Button>
              <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  width: 220,
                  textTransform: 'none',
                  fontSize: '16px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleDeleteSemester} // Use our new wrapper function
                variant="contained"
                color="error"
                
              >
                Xóa danh sách theo kỳ
              </Button>

              <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  width: 220,
                  textTransform: 'none',
                  fontSize: '16px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleComputeClusters}
                variant="contained"
                color="error"
                
              >
                Phân cụm
              </Button>
              <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  width: 220,
                  textTransform: 'none',
                  fontSize: '16px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleRemoveSegment}
                variant="contained"
                color="error"
                
              >
                Xóa ca học
              </Button>
              <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  width: 220,
                  textTransform: 'none',
                  fontSize: '16px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleCreateSegment}
                variant="contained"
                color="error"
                
              >
                Tạo ca học 
              </Button>
              <Button
                startIcon={isDeletingBySemester ? <FacebookCircularProgress /> : null}
                sx={{ 
                  width: 220,
                  textTransform: 'none',
                  fontSize: '16px'
                }}
                disabled={isDeletingBySemester || !selectedSemester}
                onClick={handleCreateSegmentForSummer}
                variant="contained"
                color="error"
                
              >
                Tạo ca học (kỳ hè)
              </Button>
            </div>
          </div>
        </div>
        <GeneralUploadTable 
          setClasses={setClassesNoSchedule}
          classes={selectedCluster ? filteredClasses : classesNoSchedule} 
          dataLoading={isClassesNoScheduleLoading}
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