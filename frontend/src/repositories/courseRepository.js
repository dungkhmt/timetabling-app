import { request } from "api";

export const courseService = {
  getAllCourses: () => 
    request("get", "/course-v2/get-all"),

  createCourse: (courseData) =>
    request("post", "/course-v2/create", null, null, courseData),

  updateCourse: (courseData) =>
    request("post", "/course-v2/update", null, null, courseData),

  deleteCourse: (id) =>
    request("delete", `/course-v2/delete?id=${id}`)
};
