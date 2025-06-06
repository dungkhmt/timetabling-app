import * as React from "react";
import { styled } from "@mui/material/styles";
import { Button } from "@mui/material";
import { FacebookCircularProgress } from "components/common/progressBar/CustomizedCircularProgress";

const VisuallyHiddenInput = styled("input")({
  clip: "rect(0 0 0 0)",
  clipPath: "inset(50%)",
  height: 1,
  overflow: "hidden",
  position: "absolute",
  bottom: 0,
  left: 0,
  whiteSpace: "nowrap",
  width: 1,
});

export default function InputFileUpload({
  selectedFile,
  setSelectedFile,
  selectedSemester,
  submitHandler,
  isUploading,
  disabled = false,
}) {
  const inputRef = React.useRef();
  console.log(selectedSemester);
  const handleFileChange = (event) => {
    setSelectedFile(event.target.files[0]);
  };
  

  return (
    <div className="flex flex-row gap-2">
      <Button
        sx={{
          width: "120",
          textTransform: "none",
          fontSize: "16px",
        }}
        disabled={selectedSemester === null}
        component="label"
        color={selectedFile ? "success" : "primary"}
        variant="contained"
        
      >
        {selectedFile === null
          ? "Nhập Excel"
          : `${selectedFile?.name}`}
        <VisuallyHiddenInput
          ref={inputRef}
          type="file"
          onChange={handleFileChange}
        />
      </Button>
      <Button
        startIcon = {isUploading ? <FacebookCircularProgress/> : null}
        disabled={selectedFile === null || isUploading || disabled}
        variant="outlined"
        sx={{
          width: "120px",
          textTransform: "none",
          fontSize: "16px",
        }}
        onClick={submitHandler}
      >
        Submit
      </Button>
    </div>
  );
}
