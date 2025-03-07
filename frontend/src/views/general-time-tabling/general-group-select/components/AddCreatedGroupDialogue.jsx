import { useState } from "react";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from "@mui/material";
import GeneralGroupAutoComplete from "views/general-time-tabling/common-components/GeneralGroupAutoComplete";
import { FacebookCircularProgress } from "components/common/progressBar/CustomizedCircularProgress";
import { useGeneralSchedule } from "services/useGeneralScheduleData";

const AddCreatedGroupDialogue = ({ open, setOpen, setClasses, selectedClasses }) => {
  const [selectedGroup, setSelectedGroup] = useState(null);
  
  const { handlers, states } = useGeneralSchedule();
  const { isUpdatingClassesGroup } = states;

  const handleClose = () => {
    setOpen(false);
  };

  const handleSubmitAddToGroup = async () => {
    if (!selectedGroup) return;
    
    const ids = Array.from(
      new Set(selectedClasses.filter((id) => id !== null))
    );
    
    try {
      const response = await handlers.updateClassesGroup({
        ids,
        groupName: selectedGroup?.groupName
      });
      
      let generalClasses = [];
      response?.data?.forEach((classObj) => {
        if (classObj?.classCode !== null && classObj?.timeSlots) {
          delete classObj.timeSlots;
          generalClasses.push(classObj);
        }
      });
      
      setClasses(generalClasses);
      handleClose();
    } catch (error) {
      handleClose();
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      width={400}
      sx={{
        '& .MuiDialog-paper': {
          minHeight: '200px', 
          maxHeight: '400px'    
        }
      }}
    >
      <DialogTitle className="text-center">Thêm vào nhóm đã tạo</DialogTitle>
      <DialogContent className="w-full">
        <div className="py-0 w-full">  
          <GeneralGroupAutoComplete
            selectedGroup={selectedGroup}
            setSelectedGroup={setSelectedGroup}
            width={400}
          />
        </div>
      </DialogContent>
      <DialogActions>
        {isUpdatingClassesGroup ? <FacebookCircularProgress/> : null}
        <Button onClick={handleClose}>Hủy</Button>
        <Button 
          disabled={isUpdatingClassesGroup || selectedGroup === null} 
          onClick={handleSubmitAddToGroup} 
          type="submit"
        >
          Xác nhận
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddCreatedGroupDialogue;
