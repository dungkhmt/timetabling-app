import { useState, useCallback, useEffect } from "react";
import { LoadingProvider } from "./contexts/LoadingContext";
import GeneralSemesterAutoComplete from "../common-components/GeneralSemesterAutoComplete";
import InputFileUpload from "./components/InputFileUpload";
import { Button } from "@mui/material";
import { FacebookCircularProgress } from "components/common/progressBar/CustomizedCircularProgress";
import GeneralUploadTable from "./components/GeneralUploadTable";
import { useGeneralSchedule } from "services/useGeneralScheduleData";
import { request } from "../../../api";
const GeneralUploadScreen = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [selectedIds, setSelectedIds] = useState([]);
  const [isUserEditing, setIsUserEditing] = useState(false);
  
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
      handleDeleteByIds
    }
  } = useGeneralSchedule();

  function handleComputeClusters(){
    let body = {
      semester: selectedSemester.semester
    };

    request(
      "post",
      "/general-classes/compute-class-cluster",
      (res) => {
        console.log('compute cluster returned ',res.data);
      },
      {
        onError: (e) => {
          
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

  return (
    <LoadingProvider>
      <div className="flex flex-col gap-2">
        <div className="flex flex-row gap-2 justify-between">
          <GeneralSemesterAutoComplete
            selectedSemester={selectedSemester}
            setSelectedSemester={setSelectedSemester}
          />
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
                onClick={() => handleDeleteByIds()}
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
                onClick={handleDeleteBySemester}
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
            </div>
          </div>
        </div>
        <GeneralUploadTable 
          setClasses={setClassesNoSchedule}
          classes={classesNoSchedule} 
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
