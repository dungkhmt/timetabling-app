import { useQuery, useMutation} from 'react-query';
import { toast } from 'react-toastify';
import { examTimetableAssignmentService } from "repositories/examTimetableAssignmentRepository ";
import { queryClient } from 'queryClient';
import { time } from 'echarts'

export const useExamTimetableAssignmentData = (examTimetableId = null) => {
  const { data: examTimetableAssignments, isLoading, error, refetch } = useQuery(
    'examTimetableAssignments',
    () => examTimetableAssignmentService.getAllExamTimetableAssignments(examTimetableId),
    // {
    //   staleTime: 5 * 60 * 1000, // Cache for 5 minutes
    //   cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
    //   enabled: !!examTimetableId,
    // }
  );

  
  const { data: algorithms, isLoading: isLoadingAlgorithm, error: errorAlgorithm } = useQuery(
    'algorithm',
    () => examTimetableAssignmentService.getAlgorithms(),
    {
      // staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      // cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
      // enabled: !!examTimetableId,
    }
  );

  const updateAssignmentMutation = useMutation(examTimetableAssignmentService.updateExamTimetableAssignment, {
    onSuccess: () => {
      queryClient.invalidateQueries('examTimetableAssignments');
      toast.success('Cập nhật lịch thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật lịch thi');
    }
  });

  const unassignAssignmentMutation = useMutation(examTimetableAssignmentService.unassignExamTimetableAssignment, {
    onSuccess: () => {
      queryClient.invalidateQueries('examTimetableAssignments');
      // toast.success('Cập nhật lịch thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật lịch thi');
    }
  });

  const getAssignmentConflictsMutation = useMutation(examTimetableAssignmentService.checkExamTimetableAssignmentConflict, {
    onSuccess: () => {
      queryClient.invalidateQueries('examTimetables');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật lịch thi');
    }
  });

  const exportTimetableMutation = useMutation(examTimetableAssignmentService.exportTimetable, {
    onSuccess: (response) => {
      const blob = new Blob([response.data], {
        type: response.headers["content-type"],
      });
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.download = "Lịch_thi.xlsx";
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
    onError: (error) => {
      console.log(error);
      toast.error('Có lỗi xảy ra khi tải xuống danh sách lớp');
    }
  });

  const autoAssignMutation = useMutation(examTimetableAssignmentService.autoAssign, {
    onSuccess: (response) => {
      queryClient.invalidateQueries('examTimetableAssignments');
      toast.success('Tự động xếp lịch thi thành công!');
    },
    onError: (error) => {
      console.log(error);
      toast.error('Có lỗi xảy ra');
    }
  });

  const checkFullExamTimetableAssignmentConflictMutation = useMutation(
    examTimetableAssignmentService.checkFullExamTimetableAssignmentConflict,
    {
      onSuccess: (response) => {
        // queryClient.invalidateQueries('examTimetableAssignments');
        // toast.success('Cập nhật lịch thi thành công!');
      },
      onError: (error) => {
        // toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật lịch thi');
      }
    }
  );

  return {
    examTimetableAssignments: examTimetableAssignments?.data || [],
    isLoading,
    refetch,
    error,
    algorithms: algorithms?.data || [],
    isLoadingAlgorithm,
    errorAlgorithm,
    updateExamTimetableAssignments: updateAssignmentMutation.mutateAsync,
    getAssignmentConflicts: getAssignmentConflictsMutation.mutateAsync,
    isLoadingConflicts: getAssignmentConflictsMutation.isLoading,
    isLoadingUpdatingAssignment: updateAssignmentMutation.isLoading,
    exportTimetable: exportTimetableMutation.mutateAsync,
    autoAssign: autoAssignMutation.mutateAsync,
    unassignAssignments: unassignAssignmentMutation.mutateAsync,
    checkConflictForFullExamTimetableAssignment: checkFullExamTimetableAssignmentConflictMutation.mutateAsync,
  };
};
