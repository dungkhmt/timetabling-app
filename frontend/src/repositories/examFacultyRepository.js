import { request } from "api";

const API_ENDPOINTS = {
  GET_ALL: "/exam-faculty",
};

class ExamFacultyService {
  async getAllExamFaculties() {
    return await request("get", `${API_ENDPOINTS.GET_ALL}`);
  }
}

export const examFacultyService = new ExamFacultyService();
