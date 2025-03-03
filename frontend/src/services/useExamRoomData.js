import { useQuery, useMutation} from 'react-query';
import { toast } from 'react-toastify';
import { examRoomService } from "repositories/examRoomRepository";
import { queryClient } from 'queryClient';

export const useExamRoomData = () => {
  
  const { data: examRooms, isLoading, error } = useQuery(
    'examRooms',
    examRoomService.getAllExamRooms,
    {
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
    }
  );

  const createMutation = useMutation(examRoomService.createExamRoom, {
    onSuccess: () => {
      queryClient.invalidateQueries('examRooms');
      toast.success('Tạo kế hoạc thi mới thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi tạo kế hoạch thi');
    }
  });

  const updateMutation = useMutation(examRoomService.updateExamRoom, {
    onSuccess: () => {
      queryClient.invalidateQueries('examRooms');
      toast.success('Cập nhật kế hoạch thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi cập nhật kế hoạc thi');
    }
  });

  const deleteMutation = useMutation(examRoomService.deleteExamRoom, {
    onSuccess: () => {
      queryClient.invalidateQueries('examRooms');
      toast.success('Xóa kế hoạc thi thành công!');
    },
    onError: (error) => {
      toast.error(error.response?.data || 'Có lỗi xảy ra khi xóa kế hoạc thi');
    }
  });

  return {
    examRooms: examRooms?.data || [],
    isLoading,
    error,
    createExamRoom: createMutation.mutateAsync,
    updateExamRoom: updateMutation.mutateAsync,
    deleteExamRoom: deleteMutation.mutateAsync,
    isCreating: createMutation.isLoading,
    isUpdating: updateMutation.isLoading,
    isDeleting: deleteMutation.isLoading,
  };
};
