import { useQuery, useMutation} from 'react-query';
import { toast } from 'react-toastify';
import { examSessionService } from "repositories/examSessionRepository";
import { queryClient } from 'queryClient';

export const useExamSessionData = () => {
  
  const { data: examSessions, isLoading, error, refetch } = useQuery(
    'examSessions',
    examSessionService.getAllExamSessions,
    {
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
    }
  );

  const createSessionMutation = useMutation(examSessionService.createExamSession, {
    onSuccess: () => {
      refetch();
      toast.success('Tạo kíp thi mới thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi tạo kíp thi');
    }
  });

  const updateSessionMutation = useMutation(examSessionService.updateExamSession, {
    onSuccess: () => {
      refetch();
      toast.success('Cập nhật kíp thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật kíp thi');
    }
  });

  const deleteSessionMutation = useMutation(examSessionService.deleteExamSession, {
    onSuccess: () => {
      refetch();

      toast.success('Xóa kíp thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi xóa kíp thi');
    }
  });

  const createCollectionMutation = useMutation(examSessionService.createExamSessionCollection, {
    onSuccess: () => {
      refetch();

      toast.success('Tạo kíp thi mới thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi tạo kíp thi');
    }
  });

  const updateCollectionMutation = useMutation(examSessionService.updateExamSessionCollection, {
    onSuccess: () => {
      refetch();

      toast.success('Cập nhật kíp thi thành công!');

    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật kíp thi');
    }
  });

  const deleteCollectionMutation = useMutation(examSessionService.deleteExamSessionCollection, {
    onSuccess: () => {
      refetch();

      toast.success('Xóa kíp thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi xóa kíp thi');
    }
  });

  return {
    sessionCollections: examSessions?.data || [],
    isLoading,
    error,
    createExamSession: createSessionMutation.mutateAsync,
    updateExamSession: updateSessionMutation.mutateAsync,
    deleteExamSession: deleteSessionMutation.mutateAsync,
    isCreating: createSessionMutation.isLoading,
    isUpdating: updateSessionMutation.isLoading,
    isDeleting: deleteSessionMutation.isLoading,
    createCollectionSession: createCollectionMutation.mutateAsync,
    updateCollectionSession: updateCollectionMutation.mutateAsync,
    deleteCollectionSession: deleteCollectionMutation.mutateAsync,
    isCreatingCollection: createCollectionMutation.isLoading,
    isUpdatingCollection: updateCollectionMutation.isLoading,
    isDeletingCollection: deleteCollectionMutation.isLoading,
  };
};
