import { useState, useCallback, useEffect } from "react";
import { toast } from "react-toastify";
import { generalScheduleRepository } from "repositories/generalScheduleRepository";

export const useGeneralSchedule = () => {
  // States
  const [selectedSemester, setSelectedSemester] = useState(null);
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [selectedRows, setSelectedRows] = useState([]);
  const [isOpenClassroomDialog, setOpenClassroomDialog] = useState(false);
  const [isOpenTimeslotDialog, setOpenTimeslotDialog] = useState(false);
  const [classroomTimeLimit, setClassroomTimeLimit] = useState(5);
  const [timeSlotTimeLimit, setTimeSlotTimeLimit] = useState(5);
  const [loading, setLoading] = useState(false);
  const [isOpenSelectedDialog, setOpenSelectedDialog] = useState(false);
  const [selectedTimeLimit, setSelectedTimeLimit] = useState(5);

  // Algorithm states
  const [algorithms, setAlgorithms] = useState([]);
  const [selectedAlgorithm, setSelectedAlgorithm] = useState("");
  const [isAlgorithmsLoading, setIsAlgorithmsLoading] = useState(false);

  const [maxDaySchedule, setMaxDaySchedule] = useState(6);

  // Data states
  const [classes, setClasses] = useState([]);
  const [classesNoSchedule, setClassesNoSchedule] = useState([]);

  // Loading states
  const [isClassesLoading, setIsClassesLoading] = useState(false);
  const [isClassesNoScheduleLoading, setIsClassesNoScheduleLoading] =
    useState(false);
  const [isResetLoading, setIsResetLoading] = useState(false);
  const [isAutoScheduleLoading, setIsAutoScheduleLoading] = useState(false);
  const [isSavingTimeSlot, setIsSavingTimeSlot] = useState(false);
  const [isAddingTimeSlot, setIsAddingTimeSlot] = useState(false);
  const [isRemovingTimeSlot, setIsRemovingTimeSlot] = useState(false);
  const [isExportExcelLoading, setIsExportExcelLoading] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [isDeletingBySemester, setIsDeletingBySemester] = useState(false);
  const [isUpdatingClassesGroup, setIsUpdatingClassesGroup] = useState(false);
  const [isDeletingByIds, setIsDeletingByIds] = useState(false);

  // Fetch classes when semester or group changes
  useEffect(() => {
    if (selectedSemester?.semester) {
      fetchClasses();
      fetchClassesNoSchedule();
    }
  }, [selectedSemester, selectedGroup]);

  // Fetch classes
  const fetchClasses = useCallback(async () => {
    if (!selectedSemester?.semester) return;

    setIsClassesLoading(true);
    setLoading(true);

    try {
      const data = await generalScheduleRepository.getClasses(
        selectedSemester?.semester,
        selectedGroup?.groupName
      );

      // Process data like the select function in previous implementation
      if (!Array.isArray(data)) {
        setClasses([]);
        return;
      }

      let generalClasses = [];
      data.forEach((classObj) => {
        const usedDuration =
          classObj.timeSlots?.reduce((total, slot) => {
            return total + (slot.duration || 0);
          }, 0) || 0;

        const remainingDuration = classObj.duration - usedDuration;

        // Handle child classes (time slots)
        if (classObj.timeSlots && classObj.timeSlots.length > 0) {
          // Only add valid child classes (with duration not null)
          classObj.timeSlots.forEach((timeSlot, index) => {
            if (timeSlot.duration !== null) {
              const cloneObj = JSON.parse(
                JSON.stringify({
                  ...classObj,
                  ...timeSlot,
                  classCode: classObj.classCode,
                  roomReservationId: timeSlot.id,
                  id: classObj.id + `-${index + 1}`,
                  crew: classObj.crew,
                  duration: timeSlot.duration,
                  isChild: true,
                  parentId: classObj.id,
                })
              );
              delete cloneObj.timeSlots;
              generalClasses.push(cloneObj);
            }
          });
        }

        if (remainingDuration > 0 || !classObj.timeSlots?.length) {
          generalClasses.push({
            ...classObj,
            generalClassId: String(
              classObj.generalClassId || classObj.id || ""
            ),
            duration: remainingDuration,
            isParent: true,
          });
        }
      });

      generalClasses.sort((a, b) => {
        const parentIdA = a.parentId || a.id;
        const parentIdB = b.parentId || b.id;

        if (parentIdA !== parentIdB) {
          return parentIdA - parentIdB;
        }

        if (a.isParent && !b.isParent) return -1;
        if (!a.isParent && b.isParent) return 1;

        return 0;
      });

      setClasses(generalClasses);
    } catch (error) {
      console.error("Error fetching classes:", error);
      toast.error("Không thể tải danh sách lớp!");
    } finally {
      setIsClassesLoading(false);
      setLoading(false);
    }
  }, [selectedSemester, selectedGroup]);

  // Fetch classes no schedule
  const fetchClassesNoSchedule = useCallback(async () => {
    if (!selectedSemester?.semester) return;

    setIsClassesNoScheduleLoading(true);

    try {
      const data = await generalScheduleRepository.getClassesNoSchedule(
        selectedSemester?.semester,
        selectedGroup?.groupName
      );
      setClassesNoSchedule(data || []);
    } catch (error) {
      console.error("Error fetching classes no schedule:", error);
    } finally {
      setIsClassesNoScheduleLoading(false);
    }
  }, [selectedSemester, selectedGroup]);

  // Fetch algorithms on mount
  useEffect(() => {
    fetchAlgorithms();
  }, []);

  const fetchAlgorithms = useCallback(async () => {
    setIsAlgorithmsLoading(true);
    try {
      const data = await generalScheduleRepository.getListAlgorithms();
      setAlgorithms(data || []);
      if (data && data.length > 0) {
        setSelectedAlgorithm(data[0]);
      }
    } catch (error) {
      console.error("Error fetching algorithms:", error);
      toast.error("Không thể tải danh sách thuật toán!");
    } finally {
      setIsAlgorithmsLoading(false);
    }
  }, []);

  // Reset schedule
  const handleResetTimeTabling = useCallback(async () => {
    if (!selectedSemester?.semester) {
      toast.error("Vui lòng chọn học kỳ!");
      return;
    }

    setIsResetLoading(true);
    setLoading(true);

    try {
      await generalScheduleRepository.resetSchedule(
        selectedSemester.semester,
        selectedRows
      );
      await fetchClasses();
      setSelectedRows([]);
      toast.success("Reset thời khóa biểu thành công!");
    } catch (error) {
      await fetchClasses();
      toast.error(error.response?.data || "Có lỗi khi reset thời khóa biểu!");
    } finally {
      setIsResetLoading(false);
      setLoading(false);
    }
  }, [selectedSemester, selectedRows, fetchClasses]);

  // Auto schedule time with algorithm
  const handleAutoScheduleTimeSlotTimeTabling = useCallback(async () => {
    if (!selectedSemester?.semester) {
      toast.error("Vui lòng chọn học kỳ!");
      return;
    }

    setIsAutoScheduleLoading(true);
    setLoading(true);

    try {
      await generalScheduleRepository.autoScheduleTime(
        selectedSemester.semester,
        selectedGroup?.groupName,
        timeSlotTimeLimit,
        selectedAlgorithm
      );
      await fetchClasses();
      setSelectedRows([]);
      setOpenTimeslotDialog(false);
      toast.success("Tự động xếp thời khóa biểu thành công!");
    } catch (error) {
      if (error.response?.status === 410 || error.response?.status === 420) {
        toast.error(error.response.data);
      } else {
        await fetchClasses();
        console.log("Auto schedule error:", error);
        toast.error("Có lỗi khi tự động thời khóa biểu!");
      }
      setOpenTimeslotDialog(false);
    } finally {
      setIsAutoScheduleLoading(false);
      setLoading(false);
      setSelectedRows([]);
    }
  }, [
    selectedSemester,
    selectedGroup,
    timeSlotTimeLimit,
    fetchClasses,
    selectedAlgorithm,
  ]);

  const handleAutoScheduleClassroomTimeTabling = useCallback(async () => {
    if (!selectedSemester?.semester) {
      toast.error("Vui lòng chọn học kỳ!");
      return;
    }

    setIsAutoScheduleLoading(true);
    setLoading(true);

    try {
      await generalScheduleRepository.autoScheduleRoom(
        selectedSemester.semester,
        selectedGroup?.groupName,
        classroomTimeLimit,
        selectedAlgorithm
      );
      await fetchClasses();
      setSelectedRows([]);
      setOpenClassroomDialog(false);
      toast.success("Tự động xếp phòng thành công!");
    } catch (error) {
      await fetchClasses();
      const message =
        error.response?.status === 410
          ? error.response.data
          : "Có lỗi khi tự động xếp phòng!";
      toast.error(message);
    } finally {
      setIsAutoScheduleLoading(false);
      setLoading(false);
      setSelectedRows([]);
    }
  }, [
    selectedSemester,
    selectedGroup,
    classroomTimeLimit,
    fetchClasses,
    selectedAlgorithm,
  ]);

  const handleSaveTimeSlot = useCallback(
    async (semester, data) => {
      if (!semester) {
        toast.error("Vui lòng chọn học kỳ!");
        return false;
      }

      setIsSavingTimeSlot(true);
      setLoading(true);

      try {
        await generalScheduleRepository.updateTimeSlot(semester, data);
        await fetchClasses();
        toast.success("Lưu TKB thành công!");
        return true;
      } catch (error) {
        if (error?.response?.status === 410) {
          await fetchClasses();
          toast.warn(error.response?.data || "Dữ liệu đã thay đổi");
        } else if (error?.response?.status === 420) {
          toast.error(error.response?.data || "Lỗi xác thực dữ liệu");
        } else {
          console.error("Save time slot error:", error);
          toast.error(
            error?.response?.data || error?.message || "Có lỗi khi lưu TKB!"
          );
        }
        return false;
      } finally {
        setIsSavingTimeSlot(false);
        setLoading(false);
      }
    },
    [fetchClasses]
  );

  const handleAddTimeSlot = useCallback(
    async (params) => {
      setIsAddingTimeSlot(true);
      setLoading(true);

      try {
        await generalScheduleRepository.addTimeSlot(params);
        await fetchClasses();
        toast.success("Thêm ca học thành công!");
        return true;
      } catch (error) {
        if (error.response?.status === 410) {
          toast.error(error.response.data);
        } else {
          toast.error("Thêm ca học thất bại!");
        }
        return false;
      } finally {
        setIsAddingTimeSlot(false);
        setLoading(false);
      }
    },
    [fetchClasses]
  );

  const handleRemoveTimeSlot = useCallback(
    async (params) => {
      setIsRemovingTimeSlot(true);
      setLoading(true);

      try {
        await generalScheduleRepository.removeTimeSlot(params);
        await fetchClasses();
        toast.success("Xóa ca học thành công!");
        return true;
      } catch (error) {
        if (error.response?.status === 410) {
          toast.error(error.response.data);
        } else {
          toast.error("Xóa ca học thất bại!");
        }
        return false;
      } finally {
        setIsRemovingTimeSlot(false);
        setLoading(false);
      }
    },
    [fetchClasses]
  );

  // Export Excel
  const handleExportTimeTabling = useCallback(async (semester) => {
    if (!semester) {
      toast.error("Vui lòng chọn học kỳ!");
      return;
    }

    setIsExportExcelLoading(true);

    try {
      const response = await generalScheduleRepository.exportExcel(semester);

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `timetable_${semester}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      toast.success("Tải xuống thành công!");
    } catch (error) {
      toast.error(error.response?.data || "Có lỗi khi tải xuống file!");
    } finally {
      setIsExportExcelLoading(false);
    }
  }, []);

  // Delete classes
  const handleDeleteClasses = useCallback(async () => {
    if (!selectedSemester?.semester) {
      toast.error("Vui lòng chọn học kỳ!");
      return;
    }

    setIsDeleting(true);
    setLoading(true);

    try {
      await generalScheduleRepository.deleteClasses(selectedSemester.semester);
      await fetchClasses();
      await fetchClassesNoSchedule();
      toast.success("Xóa danh sách thành công!");
    } catch (error) {
      toast.error(error.response?.data || "Có lỗi khi xóa danh sách!");
    } finally {
      setIsDeleting(false);
      setLoading(false);
    }
  }, [selectedSemester, fetchClasses, fetchClassesNoSchedule]);

  // Upload file
  const uploadFile = useCallback(
    async (semester, file) => {
      if (!semester) {
        toast.error("Vui lòng chọn học kỳ!");
        return;
      }

      setIsUploading(true);
      setLoading(true);

      try {
        const response = await generalScheduleRepository.uploadFile(
          semester,
          file
        );
        await fetchClasses();
        await fetchClassesNoSchedule();
        toast.success("Upload file thành công!");
        return response;
      } catch (error) {
        toast.error(error.response?.data || "Có lỗi khi upload file!");
        return null;
      } finally {
        setIsUploading(false);
        setLoading(false);
      }
    },
    [fetchClasses, fetchClassesNoSchedule]
  );

  // Delete by semester
  const deleteBySemester = useCallback(
    async (semester) => {
      if (!semester) {
        toast.error("Vui lòng chọn học kỳ!");
        return;
      }

      setIsDeletingBySemester(true);

      try {
        await generalScheduleRepository.deleteBySemester(semester);
        await fetchClassesNoSchedule();
        toast.success("Xóa danh sách lớp thành công!");
      } catch (error) {
        toast.error(error.response?.data || "Xóa danh sách lớp thất bại!");
      } finally {
        setIsDeletingBySemester(false);
      }
    },
    [fetchClassesNoSchedule]
  );

  // Auto schedule selected with algorithm
  const handleAutoScheduleSelected = useCallback(async () => {
    if (!selectedSemester?.semester || selectedRows.length === 0) {
      toast.error("Vui lòng chọn học kỳ và lớp!");
      return;
    }

    setLoading(true);

    // Clean up classIds by removing the -[number] suffix if it exists
    const cleanClassIds = selectedRows.map((id) => id.split("-")[0]);

    try {

      await generalScheduleRepository.autoScheduleSelected(
        cleanClassIds,
        selectedTimeLimit,
        selectedSemester.semester,
        selectedAlgorithm,
        maxDaySchedule
      );
      await fetchClasses();
      setOpenSelectedDialog(false);
      toast.success("Tự động xếp lịch các lớp đã chọn thành công!");
    } catch (error) {
      await fetchClasses();

      const message =
        error.response?.status === 410
          ? error.response.data
          : "Có lỗi khi tự động xếp lịch các lớp đã chọn!";
      toast.error(message);
    } finally {
      setLoading(false);
      setSelectedRows([]);
    }
  }, [
    selectedRows,
    selectedTimeLimit,
    selectedSemester,
    fetchClasses,
    selectedAlgorithm,
    maxDaySchedule,
  ]);

  // Update classes group
  const updateClassesGroup = useCallback(
    async (params) => {
      setIsUpdatingClassesGroup(true);
      setLoading(true);

      try {
        await generalScheduleRepository.updateClassesGroup(params);
        await fetchClasses();
        await fetchClassesNoSchedule();
        toast.success("Thêm nhóm thành công!");
        return true;
      } catch (error) {
        toast.error(
          error.response?.data ||
            "Thêm nhóm lỗi, nhóm đã có hoặc mã lớp không tồn tại!"
        );
        return false;
      } finally {
        setIsUpdatingClassesGroup(false);
        setLoading(false);
      }
    },
    [selectedSemester, fetchClasses, fetchClassesNoSchedule]
  );

  // Get class groups
  const getClassGroups = useCallback(async (classId) => {
    if (!classId) {
      toast.error("Không có ID lớp học!");
      return [];
    }

    try {
      const data = await generalScheduleRepository.getClassGroups(classId);
      return data;
    } catch (error) {
      toast.error("Không thể tải danh sách nhóm lớp học!");
      console.error("Failed to fetch class groups", error);
      return [];
    }
  }, []);

  // Update class group
  const updateClassGroup = useCallback(
    async (classId, groupId) => {
      try {
        const result = await generalScheduleRepository.updateClassGroup(
          classId,
          groupId
        );
        await fetchClasses();
        await fetchClassesNoSchedule();
        return result;
      } catch (error) {
        toast.error("Không thể cập nhật nhóm lớp!");
        console.error("Failed to update class group", error);
        return null;
      }
    },
    [fetchClasses]
  );

  // Delete class group
  const deleteClassGroup = useCallback(
    async (classId, groupId) => {
      try {
        const result = await generalScheduleRepository.deleteClassGroup(
          classId,
          groupId
        );
        await fetchClasses();
        return result;
      } catch (error) {
        toast.error("Không thể xóa nhóm lớp!");
        console.error("Failed to delete class group", error);
        return null;
      }
    },
    [fetchClasses]
  );

  // Delete by IDs
  const handleDeleteByIds = useCallback(async () => {
    if (selectedRows.length === 0) {
      toast.error("Vui lòng chọn lớp cần xóa!");
      return;
    }

    setIsDeletingByIds(true);
    setLoading(true);

    const cleanIds = selectedRows
      .map((id) => {
        if (typeof id === "number") {
          return id;
        }
        const strId = String(id);
        return parseInt(strId.includes("-") ? strId.split("-")[0] : strId);
      })
      .filter((id) => !isNaN(id));

    if (cleanIds.length === 0) {
      toast.error("Không tìm thấy ID hợp lệ!");
      setIsDeletingByIds(false);
      setLoading(false);
      return;
    }

    try {
      await generalScheduleRepository.deleteByIds(cleanIds);
      await fetchClasses();
      await fetchClassesNoSchedule();
      setSelectedRows([]);
      toast.success("Xóa các lớp đã chọn thành công!");
    } catch (error) {
      toast.error(error.response?.data || "Có lỗi khi xóa các lớp đã chọn!");
    } finally {
      setIsDeletingByIds(false);
      setLoading(false);
    }
  }, [selectedRows, fetchClasses, fetchClassesNoSchedule]);

  // Get subclasses by parent class ID
  const getSubClasses = useCallback(async (parentClassId) => {
    if (!parentClassId) {
      toast.error("Không có ID lớp cha!");
      return [];
    }

    try {
      const data = await generalScheduleRepository.getSubClasses(parentClassId);
      return data;
    } catch (error) {
      toast.error("Không thể tải danh sách lớp con!");
      console.error("Failed to fetch subclasses", error);
      return [];
    }
  }, []);

  return {
    states: {
      selectedSemester,
      selectedGroup,
      selectedRows,
      isOpenClassroomDialog,
      isOpenTimeslotDialog,
      classroomTimeLimit,
      timeSlotTimeLimit,
      classes,
      isLoading: isClassesLoading,
      isResetLoading,
      refetchSchedule: fetchClasses,
      refetchNoSchedule: fetchClassesNoSchedule,
      isAutoSaveLoading: isAutoScheduleLoading,
      loading: loading || isClassesLoading,
      isSavingTimeSlot,
      isAddingTimeSlot,
      isRemovingTimeSlot,
      isExportExcelLoading,
      isDeleting,
      isUploading,
      classesNoSchedule,
      isClassesNoScheduleLoading,
      isDeletingBySemester,
      isOpenSelectedDialog,
      selectedTimeLimit,
      isUpdatingClassesGroup,
      isDeletingByIds,
      algorithms,
      selectedAlgorithm,
      maxDaySchedule,
      isAlgorithmsLoading,
    },
    setters: {
      setSelectedSemester,
      setSelectedGroup,
      setSelectedRows,
      setOpenClassroomDialog,
      setOpenTimeslotDialog,
      setClassroomTimeLimit,
      setTimeSlotTimeLimit,
      setClassesNoSchedule: (newClasses) => {
        setClassesNoSchedule(newClasses);
        // Force refresh
        fetchClassesNoSchedule();
      },
      setOpenSelectedDialog,
      setSelectedTimeLimit,
      setSelectedAlgorithm,
      setMaxDaySchedule,
    },
    handlers: {
      handleResetTimeTabling,
      handleAutoScheduleTimeSlotTimeTabling,
      handleAutoScheduleClassroomTimeTabling,
      handleRefreshClasses: fetchClasses,
      handleSaveTimeSlot,
      handleAddTimeSlot,
      handleRemoveTimeSlot,
      handleExportTimeTabling,
      handleDeleteClasses,
      handleAutoScheduleSelected,
      getClassGroups,
      updateClassGroup,
      deleteClassGroup,
      updateClassesGroup,
      getSubClasses,
      handleDeleteByIds,
      uploadFile,
      deleteBySemester,
      fetchAlgorithms,
    },
  };
};
