import { useMutation, useQuery, } from 'react-query';
import { toast } from 'react-toastify'
import { examClassGroupService } from "repositories/examClassGroupRepository";

export const useExamClassGroupData = () => {
  
  const { data: examClassGroups, isLoading, error, refetch } = useQuery(
    'examClassGroups',
    examClassGroupService.getAllExamClassGroups,
    {
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
    }
  );

  const updateExamClassGroup = useMutation(examClassGroupService.updateExamClassGroup, {
    onSuccess: () => {
      refetch();
      toast.success('Cập nhật nhóm thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật nhóm');
    }
  });

  const createExamClassGroup = useMutation(examClassGroupService.createExamClassGroup, {
    onSuccess: () => {
      refetch();
      toast.success('Tạo nhóm mới thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi tạo nhóm');
    }
  });

  const deleteExamClassGroup = useMutation(examClassGroupService.deleteExamClassGroups, {
    onSuccess: () => {
      refetch();
      toast.success('Xóa nhóm thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi xóa nhóm');
    }
  });

  return {
    examClassGroups: examClassGroups?.data || [],
    isLoading,
    error,
    deleteExamClassGroups: deleteExamClassGroup.mutateAsync,
    updateExamClassGroup: updateExamClassGroup.mutateAsync,
    createExamClassGroups: createExamClassGroup.mutateAsync,
  };
};
