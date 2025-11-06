import { useEffect, useState } from "react";
import { request } from "api";
import { toast } from "react-toastify";
import GeneralSemesterAutoComplete from "../common-components/GeneralSemesterAutoComplete";
import { Button } from "@mui/material";
import InputFileUpload from "../general-upload/components/InputFileUpload";
import ClassOpenPlanTable from "./components/ClassOpenPlanTable";
import { useGeneralSchedule } from "services/useGeneralScheduleData";
import GeneralGroupAutoComplete from "../common-components/GeneralGroupAutoComplete";
import AddNewClassDialog from "./components/AddNewClassDialog";
import ListBatch from "../batch/listbatch";

const GeneralPlanClassOpenScreen = () => {
  const [selectedSemester, setSelectedSemester] = useState(null);
  const [planClasses, setPlanClasses] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [isOpenDialog, setOpenDialog] = useState(false);
  const [isImportLoading, setImportLoading] = useState(false);
  const [selectedRows, setSelectedRows] = useState([]);
  const [openNewClassDialog, setOpenNewClassDialog] = useState(false);
  const { states, setters } = useGeneralSchedule();

  function getPlanClass() {
    if (!selectedSemester?.semester) return; 
    request(
      "get",
      `/plan-general-classes/?semester=${selectedSemester.semester}`,
      (res) => {
        setPlanClasses(res.data);
        //setImportLoading(false);
        toast.success("Truy vấn kế hoạch học tập thành công!");
      },
      (err) => {
        toast.error("Có lỗi khi truy vấn kế hoạch học tập");
      },
      null,
      null,
      null
    );
  }
  useEffect(() => {
    if (selectedSemester) {
      getPlanClass();
    }
  }, [selectedSemester]);

  const handleImportExcel = () => {
    if (selectedFile) {
      setImportLoading(true);
      const formData = new FormData();
      formData.append("file", selectedFile);

      const config = {
        headers: {
          "Content-Type": "multipart/form-data",
          Accept: "application/json",
        },
      };

      request(
        "post",
        `/excel/upload-plan?semester=${selectedSemester?.semester}&createclass=T&groupId=${states.selectedGroup?.id}`,

        (res) => {
          setImportLoading(false);
          toast.success("Upload file thành công!");
          console.log(res?.data);
          //setPlanClasses(res?.data); // Remove this
          if (selectedSemester) getPlanClass(); // Add this
        },
        (err) => {
          if (err.response?.status === 410) {
            toast.error(err.response.data);
          } else {
            toast.error("Có lỗi khi upload file!");
          }
          setImportLoading(false);
          console.log(err);
        },
        formData,
        config
      );
    }
  };

  function handleGenerateClasseSegmentFromClass() {
    let body = {
      semester: selectedSemester.semester,
    };
    setImportLoading(true);
    request(
      "post",
      `/plan-general-classes/generate-class-segment-from-classes`,
      (res) => {
        toast.success("Sinh lớp thành công!");
        console.log(res?.data);
        if (selectedSemester) getPlanClass();
        setImportLoading(false);
      },
      (err) => {
        if (err.response.status === 410) {
          toast.error(err.response.data);
        } else {
          toast.error("Có lỗi khi sinh lớp");
        }

        console.log(err);
      },
      body
    );
  }

  function handleGenerateClassesFromPlan() {
    let body = {
      semester: selectedSemester.semester,
    };
    setImportLoading(true);
    request(
      "post",
      `/plan-general-classes/generate-classes-from-plan`,
      (res) => {
        toast.success("Sinh lớp thành công!");
        console.log(res?.data);
        if (selectedSemester) getPlanClass();
        setImportLoading(false);
      },
      (err) => {
        if (err.response.status === 410) {
          toast.error(err.response.data);
        } else {
          toast.error("Có lỗi khi sinh lớp");
        }

        console.log(err);
      },
      body
    );
  }
  function clearPlan() {
    let body = {
      semesterId: selectedSemester.semester,
    };
    setImportLoading(true);
    request(
      "post",
      `/plan-general-classes/clear-plan`,
      (res) => {
        toast.success("Xóa kế hoạch mở lớp thành công!");
        console.log(res?.data);
        if (selectedSemester) getPlanClass();
        setImportLoading(false);
      },
      (err) => {
        if (err.response.status === 410) {
          toast.error(err.response.data);
        } else {
          toast.error("Có lỗi khi clear");
        }

        console.log(err);
      },
      body
    );
  }

  const handleDeleteSelected = () => {
    request(
      "delete",
      `/plan-general-classes/delete-by-ids?${selectedRows
        .map((id) => `planClassId=${id}`)
        .join("&")}`,
      (res) => {
        // setPlanClasses((prev) =>  // Remove this block
        //   prev.filter((row) => !selectedRows.includes(row.id))
        // );
        setSelectedRows([]);
        toast.success("Xóa lớp thành công!");
        if (selectedSemester) getPlanClass(); // Add this
      },
      (error) => {
        toast.error("Có lỗi khi xóa lớp!");
      }
    );
  };

  return (
    <div>
      <div className="flex flex-row justify-between mb-4">
        <div className="flex flex-row gap-2">
          <GeneralSemesterAutoComplete
            selectedSemester={selectedSemester}
            setSelectedSemester={setSelectedSemester}
          />
          <GeneralGroupAutoComplete
            selectedGroup={states.selectedGroup}
            setSelectedGroup={setters.setSelectedGroup}
            sx={{
              minWidth: 200,
              "& .MuiInputBase-root": { height: "40px" },
            }}
          />
        </div>
        <div className="flex flex-col justify-end gap-2">
          <div className="flex flex-row gap-2 justify-end">
            <Button
              color="primary"
              disabled={selectedSemester === null || !states.selectedGroup}
              variant="contained"
              onClick={() => setOpenNewClassDialog(true)}
              sx={{
                textTransform: "none",
              }}
            >
              Tạo mới
            </Button>
            <Button
              color="primary"
              disabled={selectedSemester === null}
              variant="contained"
              onClick={clearPlan}
              sx={{
                textTransform: "none",
              }}
            >
              Xóa
            </Button>
            <Button
              variant="contained"
              color="error"
              disabled={selectedRows.length === 0}
              onClick={handleDeleteSelected}
              sx={{
                textTransform: "none",
              }}
            >
              Xóa các lớp đã chọn ({selectedRows.length})
            </Button>
            <Button
              variant="contained"
              color="error"
              onClick={handleGenerateClassesFromPlan}
              sx={{
                textTransform: "none",
              }}
            >
              Sinh lớp
            </Button>
            <Button
              variant="contained"
              color="error"
              onClick={handleGenerateClasseSegmentFromClass}
              sx={{
                textTransform: "none",
              }}
            >
              Sinh class-segment
            </Button>
            <div id="delete-selected-container"></div>
          </div>
          <div className="flex flex-row gap-2 justify-end">
            <InputFileUpload
              disabled={states.selectedGroup == null}
              isUploading={isImportLoading}
              selectedFile={selectedFile}
              setSelectedFile={setSelectedFile}
              selectedSemester={selectedSemester}
              submitHandler={handleImportExcel}
            />
          </div>
        </div>
      </div>
      <ClassOpenPlanTable
        setOpenDialog={setOpenDialog}
        isOpenDialog={isOpenDialog}
        semester={selectedSemester?.semester}
        classes={planClasses}
        setClasses={setPlanClasses}
        selectedRows={selectedRows}
        onSelectionChange={setSelectedRows}
      />      
      <AddNewClassDialog
        open={openNewClassDialog}
        onClose={() => setOpenNewClassDialog(false)}
        semester={selectedSemester}
        selectedGroup={states.selectedGroup}
        onSuccess={(newClass) => {
          setPlanClasses([...planClasses, newClass]);
          toast.success("Tạo lớp mới thành công!");
        }}
      />

      <ListBatch semester={selectedSemester}/>

    </div>
  );
};

export default GeneralPlanClassOpenScreen;
