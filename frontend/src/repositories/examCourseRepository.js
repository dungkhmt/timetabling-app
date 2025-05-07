import { request } from "api";

const API_ENDPOINTS = {
  GET_ALL: "/exam-course",
};

class ExamCourseService {
  async getAllExamCourses() {
    return await request("get", `${API_ENDPOINTS.GET_ALL}`);
  }
}

export const examCourseService = new ExamCourseService();
