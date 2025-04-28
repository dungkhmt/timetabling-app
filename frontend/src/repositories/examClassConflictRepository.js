import { request } from "api";

const API_ENDPOINTS = {
  GET_ALL: "/exam-conflict/plan/",
  CREATE: "/exam-conflict/create",
  DELETE: "/exam-conflict/delete"
};

class ExamClassConflictService {
  async getAllExamConflicts(planId) {
    return await request("get", `${API_ENDPOINTS.GET_ALL}${planId}`);
  }

  async createExamConflict(data) {
    return await request("post", API_ENDPOINTS.CREATE, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      } 
    });
  }

  async deleteExamConflict(data) {
    return await request("post", API_ENDPOINTS.DELETE, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      } 
    });
  }
}

export const examClassConflictService = new ExamClassConflictService();
