export const teacherClassAssignment = {
  id: "MENU_TEACHER_CLASS_ASSIGNMENT",
  icon: "AnalyticsIcon",
  text: "Phân công giảng dạy",
  child: [
    {
      id: "MENU_TEACHER_CLASS_ASSIGNMENT.BATCH",
      path: "/teacher-class-assignment/ListClassBySchool",
      isPublic: true,
      text: "Phân công",
      child: [],
    },
    {
      id: "MENU_TEACHER_CLASS_ASSIGNMENT.DASHBOARD",
      path: "/teacher-class-assignment/ListClassPlan",
      isPublic: true,
      text: "Danh sách lớp",
      child: [],
    },
    {
      id: "MENU_TEACHER_CLASS_ASSIGNMENT.SETTING",
      path: "/teacher-class-assignment/SettingBatch",
      isPublic: true,
      text: "Cài đặt phân công",
      child: [],
    },
   ],
};
