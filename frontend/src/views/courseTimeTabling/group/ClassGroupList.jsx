import { useState } from "react";
import { useGroupData } from "services/useGroupData";
import { Button, TextField } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import GroupToolbar from "./components/GroupToolbar";
import DeleteConfirmDialog from "./components/DeleteConfirmDialog";
import ManageGroupScreen from "./components/ManageGroupScreen";
import GroupDetailsDialog from "./components/GroupDetailsDialog";
import SimpleCreateGroupDialog from "./components/SimpleCreateGroupDialog";

function removeDiacritics(str) {
  return str.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
}

export default function ClassGroupList() {
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [isDialogOpen, setDialogOpen] = useState(false);
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [deleteGroupId, setDeleteGroupId] = useState(null);
  const [isManageDialogOpen, setManageDialogOpen] = useState(false);
  const [selectedDetailId, setSelectedDetailId] = useState(null);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedParentGroup, setSelectedParentGroup] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");

  const { allGroups: groups, isLoading, deleteGroupById } = useGroupData();

  if (isLoading) {
    return <div>Loading...</div>;
  }

  const filteredGroups = groups.filter((group) =>
    removeDiacritics(group.groupName.toLowerCase()).includes(removeDiacritics(searchTerm.toLowerCase()))
  );

  const columns = [
    {
      headerName: "STT",
      field: "index",
      width: 120,
      renderCell: (params) => params.row.index,
    },
    {
      headerName: "Tên nhóm",
      field: "groupName",
      width: 320,
    },
    {
      headerName: "Hành động",
      field: "actions",
      width: 200,
      renderCell: (params) => (
        <div data-action-buttons>
          <Button
            variant="outlined"
            color="primary"
            onClick={() => {
              handleEdit(params.row)
            }}
            style={{ marginRight: "8px" }}
          >
            Sửa
          </Button>
          <Button
            variant="outlined"
            color="error"
            onClick={() => handleDeleteClick(params.row.id)}
          >
            Xóa
          </Button>
        </div>
      ),
    },
  ];

  const handleEdit = (group) => {
    setSelectedGroup({
      ...group,
      id: group.groupId,
    });
    setManageDialogOpen(true);
    console.log(group)
  };

  const handleDeleteClick = (id) => {
    setDeleteGroupId(id);
    setConfirmDeleteOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (deleteGroupId) {
      try {
        await deleteGroupById(deleteGroupId);
        setConfirmDeleteOpen(false);
        setDeleteGroupId(null);
      } catch (error) {
        console.error(error);
      }
    }
  };

  const handleRowClick = (params, event) => {
    setSelectedDetailId(params.row.id);
    setSelectedParentGroup(params.row.groupName);
    setDetailDialogOpen(true);
  };

  return (
    <div style={{ height: 500, width: "100%" }}>
      <div className="flex justify-end gap-4 mb-4 items-center">
        <TextField
          label="Tìm kiếm theo tên nhóm"
          variant="outlined"
          size="small"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-[300px]"
        />
        <GroupToolbar onAdd={() => setDialogOpen(true)} />
      </div>
      <DataGrid
        loading={isLoading}
        getRowId={(row) => row.index}
        rows={filteredGroups.map((group, index) => ({
          ...group,
          groupId: group.id,
          index: index + 1,
          id: group.id,
        }))}
        disableColumnSelector
        disableDensitySelector
        columns={columns}
        pageSize={10}
        onRowDoubleClick={handleRowClick}
      />

      <DeleteConfirmDialog
        open={confirmDeleteOpen}
        onClose={() => setConfirmDeleteOpen(false)}
        onConfirm={handleConfirmDelete}
      />

      <ManageGroupScreen
        open={isManageDialogOpen}
        handleClose={() => {
          setManageDialogOpen(false);
          setSelectedGroup(null);
        }}
        initialGroup={selectedGroup} 
      />

      <GroupDetailsDialog
        open={detailDialogOpen}
        onClose={() => {
          setDetailDialogOpen(false);
          setSelectedParentGroup(null);
        }}
        groupId={selectedDetailId}
        parentGroupName={selectedParentGroup}
      />

      <SimpleCreateGroupDialog
        open={isDialogOpen}
        handleClose={() => setDialogOpen(false)}
      />
    </div>
  );
}
