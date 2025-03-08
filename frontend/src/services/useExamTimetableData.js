import { useQuery, useMutation} from 'react-query';
import { toast } from 'react-toastify';
import { examTimetableService } from "repositories/examTimetableRepository";
import { queryClient } from 'queryClient';
import { time } from 'echarts'

export const useExamTimetableData = (examPlanId = null, examTimetableId = null) => {
  const { data: examTimetables, isLoading, error } = useQuery(
    'examTimetables',
    () => examTimetableService.getAllExamTimetables(examPlanId),
    {
      // staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      // cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
      // enabled: !!examPlanId,
    }
  );

  const { data: timetable, isLoading: isLoadingDetail, error: errorDetail } = useQuery(
    ['examTimetable', examTimetableId],
    () => examTimetableService.getExamTimetableById(examTimetableId),
    {
      // staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      // cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
      // enabled: !!examTimetableId,
    }
  );

  const createMutation = useMutation(examTimetableService.createExamTimetable, {
    onSuccess: () => {
      queryClient.invalidateQueries('examTimetables');
      toast.success('Tạo lịch thi mới thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi tạo lịch thi');
    }
  });

  const updateMutation = useMutation(examTimetableService.updateExamTimetable, {
    onSuccess: () => {
      queryClient.invalidateQueries('examTimetables');
      toast.success('Cập nhật lịch thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật lịch thi');
    }
  });

  const updateAssignmentMutation = useMutation(examTimetableService.updateExamTimetableAssignment, {
    onSuccess: () => {
      queryClient.invalidateQueries('examTimetables');
      toast.success('Cập nhật lịch thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật lịch thi');
    }
  });

  const getAssignmentConflictsMutation = useMutation(examTimetableService.checkExamTimetableAssignmentConflict, {
    onSuccess: () => {
      queryClient.invalidateQueries('examTimetables');
      // toast.success('Cập nhật lịch thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật lịch thi');
    }
  });

  const deleteMutation = useMutation(examTimetableService.deleteExamTimetable, {
    onSuccess: () => {
      queryClient.invalidateQueries('examTimetables');
      toast.success('Xóa lịch thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi xóa lịch thi');
    }
  });

  return {
    examTimetables: examTimetables?.data || [],
    isLoading,
    error,
    createExamTimetable: createMutation.mutateAsync,
    updateExamTimetable: updateMutation.mutateAsync,
    deleteExamTimetable: deleteMutation.mutateAsync,
    updateExamTimetableAssignments: updateAssignmentMutation.mutateAsync,
    getAssignmentConflicts: getAssignmentConflictsMutation.mutateAsync,
    isLoadingConflicts: getAssignmentConflictsMutation.isLoading,
    isLoadingUpdatingAssignment: updateAssignmentMutation.isLoading,
    isCreating: createMutation.isLoading,
    isUpdating: updateMutation.isLoading,
    isDeleting: deleteMutation.isLoading,
    timetable: timetable?.data || {
      assignments: [],
      dates: [],
      weeks: [],
      slots: [], 
    },
    isLoadingDetail,
    errorDetail,
  };
};
