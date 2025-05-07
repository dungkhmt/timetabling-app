import { request } from "api";

const API_ENDPOINTS = {
  GET_ALL: "/exam-class-group",
};

class ExamClassGroupService {
  async getAllExamClassGroups() {
    return await request("get", `${API_ENDPOINTS.GET_ALL}`);
  }
}

export const examClassGroupService = new ExamClassGroupService();
