import { request } from "api";

const API_ENDPOINTS = {
  GET_ALL: "/exam-semester",
};

class ExamSemesterService {
  async getAllExamSemesters() {
    return await request("get", `${API_ENDPOINTS.GET_ALL}`);
  }
}

export const examSemesterService = new ExamSemesterService();
