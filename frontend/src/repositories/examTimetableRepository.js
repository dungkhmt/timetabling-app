import { request } from "api";

const API_ENDPOINTS = {
  GET_ALL: "/exam-timetable/plan",
  CREATE: "/exam-timetable/create",
  UPDATE: "/exam-timetable/update",
  DELETE: "/exam-timetable/delete",
  GET_BY_ID: "/exam-timetable/detail",
  GET_STATISTIC_BY_ID: "/exam-timetable/statistic"
};

class ExamTimetableService {
  async getAllExamTimetables(planId) {
    return await request("get", `${API_ENDPOINTS.GET_ALL}/${planId}`);
  }

  async createExamTimetable(data) {
    return await request("post", API_ENDPOINTS.CREATE, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      } 
    });
  }

  async updateExamTimetable(data) {
    return await request("post", API_ENDPOINTS.UPDATE, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      }
    });
  }

  async deleteExamTimetable(id) {
    return await request("post", `${API_ENDPOINTS.DELETE}/${id}`);
  }

  async getExamTimetableById(id) {
    return await request("get", `${API_ENDPOINTS.GET_BY_ID}/${id}`);
  }

  async getExamTimetablesStatisticById(id) {
    return await request("get", `${API_ENDPOINTS.GET_STATISTIC_BY_ID}/${id}`);
  }

  async updateExamTimetableAssignment(data) {
    return await request("post", "/exam-timetable/assignment/update-batch", null, null, data, {
      headers: {
        "Content-Type": "application/json"
      }
    });
  }

  async checkExamTimetableAssignmentConflict(data) {
    return await request("post", "/exam-timetable/assignment/check-conflict", null, null, data, {
      headers: {
        "Content-Type": "application/json"
      }
    });
  }
}

export const examTimetableService = new ExamTimetableService();
