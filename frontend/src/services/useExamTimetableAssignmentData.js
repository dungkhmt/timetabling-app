import { useQuery, useMutation} from 'react-query';
import { toast } from 'react-toastify';
import { examTimetableAssignmentService } from "repositories/examTimetableAssignmentRepository ";
import { queryClient } from 'queryClient';

export const useExamTimetableAssignmentData = (examTimetableId = null) => {
  const { data: examTimetableAssignments, isLoading, error } = useQuery(
    'examTimetableAssignments',
    () => examTimetableAssignmentService.getAllExamTimetableAssignments(examTimetableId),
    {
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
      enabled: !!examTimetableId,
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

  const getAssignmentConflictsMutation = useMutation(examTimetableAssignmentService.checkExamTimetableAssignmentConflict, {
    onSuccess: () => {
      queryClient.invalidateQueries('examTimetables');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật lịch thi');
    }
  });

  return {
    examTimetableAssignments: examTimetableAssignments?.data || [],
    isLoading,
    error,
    updateExamTimetableAssignments: updateAssignmentMutation.mutateAsync,
    getAssignmentConflicts: getAssignmentConflictsMutation.mutateAsync,
    isLoadingConflicts: getAssignmentConflictsMutation.isLoading,
    isLoadingUpdatingAssignment: updateAssignmentMutation.isLoading,
  };
};
