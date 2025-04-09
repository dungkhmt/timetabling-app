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
      // Using a function form prevents dependency on classesNoSchedule itself
      setClassesNoSchedule(() => []);
      
      // No need to call fetchClassesNoSchedule here as it is already triggered
      // when selectedSemester changes via the useEffect in useGeneralScheduleData
    }
  }, [selectedSemester?.semester]); // Only depend on the semester value itself

  // Effect for filtering by cluster
  useEffect(() => {
    console.log("Filtering by cluster:", selectedCluster?.id);
    console.log("Current semester:", selectedSemester?.semester);
    
    const filterClassesByCluster = async () => {
      // Always set loading state when filter changes
      setIsLoadingData(true);
      
      try {
        if (selectedCluster) {
          console.log("Fetching data for cluster:", selectedCluster.id);
          const clusterClasses = await getClassesByCluster(selectedCluster.id);
          console.log("Cluster classes loaded:", clusterClasses?.length || 0);
          // Always set to an empty array if result is falsy
          setFilteredClasses(clusterClasses || []);
        } else {
          // If no cluster is selected, no need to do anything with filteredClasses
          setFilteredClasses([]);
        }
      } catch (error) {
        console.error("Error fetching filtered classes:", error);
        // In case of error, explicitly set to empty array
        setFilteredClasses([]);
      } finally {
        setIsLoadingData(false);
      }
    };
    
    // Only run the filter if we have a semester selected
    if (selectedSemester?.semester) {
      filterClassesByCluster();
    } else {
      setFilteredClasses([]);
      setIsLoadingData(false);
    }
  }, [selectedCluster, getClassesByCluster, selectedSemester]);

  // Determine the appropriate loading state based on current operation
  const tableLoadingState = isClassesNoScheduleLoading || isLoadingData || isLoadingClusterClasses;

  // Create a variable to determine which data to show in the table
  const displayClasses = useMemo(() => {
    // Debugging logs
    console.log("Display classes calculation:");
    console.log("- Selected cluster:", selectedCluster?.id);
    console.log("- Filtered classes length:", filteredClasses?.length || 0);
    console.log("- Classes no schedule length:", classesNoSchedule?.length || 0);
    console.log("- Is loading:", tableLoadingState);
    
    // When loading, return empty array to prevent showing stale data
    if (tableLoadingState) {
      console.log("Returning empty array because loading");
      return [];
    }
    
    // If a cluster is selected, use filtered classes
    if (selectedCluster !== null) {
      console.log("Returning filtered classes for cluster");
      return filteredClasses || [];
    }
    
    // Otherwise, use regular classesNoSchedule
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