import { useQuery, useMutation} from 'react-query';
import { toast } from 'react-toastify';
import { queryClient } from 'queryClient';
import { examClassConflictService } from 'repositories/examClassConflictRepository'

export const useExamClassConflictData = (examPlanId = null) => {
  
  const { data: examClassConflicts, isLoading, error, refetch } = useQuery(
    ['examClassConflicts', examPlanId],
    () => examClassConflictService.getAllExamConflicts(examPlanId),
    {
      staleTime: 5 * 60 * 1000,
      cacheTime: 30 * 60 * 1000,
      enabled: !!examPlanId
    }
  );

  const createMutation = useMutation(examClassConflictService.createExamConflict, {
    onSuccess: () => {
      queryClient.invalidateQueries('examClassConflicts');
      toast.success('Tạo xung đột mới thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi tạo xung đột lớp!');
    }
  });

  const deleteMutation = useMutation(examClassConflictService.deleteExamConflict, {
    onSuccess: () => {
      queryClient.invalidateQueries('examClassConflicts');
      toast.success('Xóa xung đột thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi xóa xung đột');
    }
  });

  return {
    examClassConflicts: examClassConflicts?.data || {
      conflicts: [],
      examClasses: [],
    },
    isLoading,
    error,
    createConflict: createMutation.mutateAsync,
    deleteConflicts: deleteMutation.mutateAsync,
    isCreating: createMutation.isLoading,
    isDeleting: deleteMutation.isLoading,
  };
};
