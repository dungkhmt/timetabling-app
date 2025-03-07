import { useState } from "react";
import AddNewGroupDialogue from "./components/AddNewGroupDialogue";
import AddCreatedGroupDialogue from "./components/AddCreatedGroupDialogue";
import GeneralSemesterAutoComplete from "../common-components/GeneralSemesterAutoComplete";
import { Button } from "@mui/material";
import GeneralGroupTable from "./components/GeneralGroupTable";
import { useGeneralSchedule } from "services/useGeneralScheduleData";

const GeneralGroupScreen = () => {
  const { states, setters, handlers } = useGeneralSchedule();
  const { classesNoSchedule, isClassesNoScheduleLoading, selectedSemester } = states;
  const { setSelectedSemester } = setters;
  
  const [selectedClasses, setSelectedClasses] = useState([]);
  const [openCreatedGroupDialouge, setOpenCreatedGroupDialouge] = useState(false);
  const [openNewGroupDialouge, setOpenNewGroupDialouge] = useState(false);

  const handleSelectionModelChange = (selectionModel) => {
    setSelectedClasses(selectionModel);
  };

  return (
    <div className="flex flex-col gap-2">
      <AddNewGroupDialogue
        open={openNewGroupDialouge}
        selectedClasses={selectedClasses}
        setOpen={setOpenNewGroupDialouge}
        setClasses={setters.setClassesNoSchedule}
      />
      <AddCreatedGroupDialogue
        setClasses={setters.setClassesNoSchedule}
        selectedClasses={selectedClasses}
        open={openCreatedGroupDialouge}
        setOpen={setOpenCreatedGroupDialouge}
      />
      <div className="flex flex-row gap-2 justify-between">
        <GeneralSemesterAutoComplete
          selectedSemester={selectedSemester}
          setSelectedSemester={setSelectedSemester}
        />
        <div className="flex flex-row gap-2">
          <Button
            onClick={() => {
              setOpenNewGroupDialouge(true);
            }}
            disabled={selectedClasses.length === 0}
            sx={{ width: "200px" }}
            variant="outlined"
          >
            Thêm vào nhóm mới
          </Button>

          <Button
            onClick={() => {
              setOpenCreatedGroupDialouge(true);
            }}
            disabled={selectedClasses.length === 0}
            sx={{ width: "200px" }}
            variant="outlined"
          >
            Thêm vào nhóm đã tạo
          </Button>
        </div>
      </div>
      <GeneralGroupTable
        handleSelectionModelChange={handleSelectionModelChange}
        classes={classesNoSchedule}
        dataLoading={isClassesNoScheduleLoading}
      />
    </div>
  );
};

export default GeneralGroupScreen;
