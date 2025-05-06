package openerp.openerpresourceserver.examtimetabling.dtos;

import openerp.openerpresourceserver.examtimetabling.entity.ExamPlan;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Semester;

public class ExamPlanWithSemesterDTO {
  private ExamPlan examPlan;
  private Semester semester;

  public ExamPlanWithSemesterDTO(ExamPlan examPlan, Semester semester) {
      this.examPlan = examPlan;
      this.semester = semester;
  }

  public ExamPlan getExamPlan() {
      return examPlan;
  }

  public Semester getSemester() {
      return semester;
  }
}
