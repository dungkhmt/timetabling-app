import { request } from 'api';

export const examClassGroupService = {
  getAllExamClassGroups: async () => {
    return await request('get', '/exam-class-group');
  },

  deleteExamClassGroups: async (examClassGroupIds) => {
    return await request('post', '/exam-class-group/delete', null, null, examClassGroupIds, {
      headers: {
        'Content-Type': 'application/json',
      }
    });
  },

  createExamClassGroup: async (examClassGroupNames) => {
    return await request('post', '/exam-class-group/bulk-create', null, null, examClassGroupNames, {
      headers: {
        'Content-Type': 'application/json',
      }
    });
  },

  updateExamClassGroup: async (examClassGroup) => {
    return await request('post', `/exam-class-group/update/${examClassGroup.id}`, null, null, examClassGroup, {
      headers: {
        'Content-Type': 'application/json',
      }
    });
  },
};
