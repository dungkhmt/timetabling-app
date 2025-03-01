import { useQuery, useMutation} from 'react-query';
import { toast } from 'react-toastify';
import { examSessionService } from "repositories/examSessionRepository";
import { queryClient } from 'queryClient';

export const useExamSessionData = () => {
  
  const { data: examSessions, isLoading, error } = useQuery(
    'examSessions',
    examSessionService.getAllExamSessions,
    {
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
    }
  );

  const createMutation = useMutation(examSessionService.createExamSession, {
    onSuccess: () => {
      queryClient.invalidateQueries('examSessions');
      toast.success('Tạo kế hoạc thi mới thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi tạo kế hoạch thi');
    }
  });

  const updateMutation = useMutation(examSessionService.updateExamSession, {
    onSuccess: () => {
      queryClient.invalidateQueries('examSessions');
      toast.success('Cập nhật kế hoạc thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật kế hoạc thi');
    }
  });

  const deleteMutation = useMutation(examSessionService.deleteExamSession, {
    onSuccess: () => {
      queryClient.invalidateQueries('examSessions');
      toast.success('Xóa kế hoạc thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi xóa kế hoạc thi');
    }
  });

  return {
    examSessions: examSessions?.data || [],
    isLoading,
    error,
    createExamSession: createMutation.mutateAsync,
    updateExamSession: updateMutation.mutateAsync,
    deleteExamSession: deleteMutation.mutateAsync,
    isCreating: createMutation.isLoading,
    isUpdating: updateMutation.isLoading,
    isDeleting: deleteMutation.isLoading,
  };
};
