package com.aplana.timesheet.form.validator;

import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * <pre>
 *  <table border="1">
 *      <tr>
 *          <th>Автор</th>
 *          <th>Компания</th>
 *          <th>Описание изменения</th>
 *          <th>Дата изменения</th>
 *      </tr>
 *      <tr>
 *          <td>iziyangirov</td>
 *          <td>Аплана</td>
 *          <td>Тест на валидацию формы</td>
 *          <td>16.01.13</td>
 *      </tr>
 *  </table>
 * </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:WEB-INF/spring/testApplicationContext.xml"})
public class TimeSheetFormValidatorTest extends TestCase {

//    @Autowired
//    private TimeSheetFormValidator validator;
//
//    final Errors errors = mock(Errors.class);
//    final Object[] errorMessageArgs = {"в строке №" + (1)};
//
//    @Test
//    public void testDateValidation() throws Exception {
//        Integer employeeId = 55;
//        String date = String.format("%1$tY-%1$tm-%1$te", new Date());
//        validator.dateValidation(date, employeeId, errors);
//        checkNoRejectValueOccurs();
//
//        // не выбрана дата
//        date = "";
//        validator.dateValidation(date, employeeId, errors);
//        checkRejectValueOccurs(3);
//
//        // нет в БД
//        date = "2000-09-20";
//        validator.dateValidation(date, employeeId, errors);
//        checkRejectValueOccurs(3);
//
//        // уже списывал
//        date = "2012-09-20";
//        validator.dateValidation(date, employeeId, errors);
//        checkRejectValueOccurs(4);
//    }
//
//    @Test
//    public void testDivisionValidation() throws Exception {
//
//        Integer division = Integer.MAX_VALUE;
//        validator.divisionValidation(division, errors);
//        checkRejectValueOccurs(3);
//
//        division = 8;
//        validator.divisionValidation(division, errors);
//        checkNoRejectValueOccurs();
//    }
//
//    @Test
//    public void testEmployeeValidation() throws Exception {
//
//        Integer employeeId = Integer.MAX_VALUE;
//        validator.employeeValidation(employeeId, errors);
//        checkRejectValueOccurs(3);
//
//        employeeId = 55;
//        validator.employeeValidation(employeeId, errors);
//        checkNoRejectValueOccurs();
//    }
//
//    @Test
//    public void testLongVacationOrIllnessValidation() throws Exception {
//        // нормальные значения
//        boolean longVacation = true;
//        boolean longIllness = false;
//        String beginDate = "2012-12-12";
//        String endDate = "2012-12-21";
//        Integer employeeId = 55;
//        Boolean planNecessary = new Boolean(true);
//
//        validator.longVacationOrIllnessValidation(longVacation,longIllness,beginDate,endDate,employeeId,
//                planNecessary,errors);
//        checkNoRejectValueOccurs();
//
//        // Дата начала отпуска пустое
//        beginDate = "";
//        validator.longVacationOrIllnessValidation(longVacation,longIllness,beginDate,endDate,employeeId,
//                planNecessary,errors);
//        checkRejectValueOccurs(3);
//        beginDate = "2012-12-12";
//
//        // Дата конца отпуска пустое
//        endDate = "";
//        validator.longVacationOrIllnessValidation(longVacation,longIllness,beginDate,endDate,employeeId,
//                planNecessary,errors);
//        checkRejectValueOccurs(3);
//        endDate = "2012-12-21";
//
//        // Дата начала больше даты конца
//        beginDate = "2012-12-12";
//        endDate = "2012-12-10";
//        validator.longVacationOrIllnessValidation(longVacation,longIllness,beginDate,endDate,employeeId,
//                planNecessary,errors);
//        checkRejectValueOccurs(3);
//        beginDate = "2012-12-12";
//        endDate = "2012-12-21";
//
//        // Недопустимая дата начала, нет в БД
//        beginDate = "2000-12-12";
//        validator.longVacationOrIllnessValidation(longVacation,longIllness,beginDate,endDate,employeeId,
//                planNecessary,errors);
//        checkRejectValueOccurs(3);
//        beginDate = "2012-12-12";
//
//        // Недопустимая дата окончания, нет в БД
//        endDate = "2000-12-21";
//        validator.longVacationOrIllnessValidation(longVacation,longIllness,beginDate,endDate,employeeId,
//                planNecessary,errors);
//        checkRejectValueOccurs(3);
//        endDate = "2012-12-21";
//
//        // Сотрудник уже отправлял данные
//        beginDate = "2011-10-01";
//        endDate = "2011-10-10";
//        validator.longVacationOrIllnessValidation(longVacation,longIllness,beginDate,endDate,employeeId,
//                planNecessary,errors);
//        checkRejectValueOccurs(4);
//    }
//
//    @Test
//    public void testTotalDurationValidation () throws Exception {
//        Double totalDuration = 10.;
//        validator.totalDurationValidation(totalDuration, errors);
//        checkNoRejectValueOccurs();
//
//        totalDuration = 99.;
//        validator.totalDurationValidation(totalDuration, errors);
//        checkRejectValueOccurs(3);
//    }
//
//    @Test
//    @Transactional
//    public void testPlanValidation(){
//        Integer employeeId = 55;
//        ProjectRole employeeJob = validator.getEmployeeJob(employeeId);
//        boolean planNecessary = true;
//        String plan = "Тут какой-то план на следующий день";
//        validator.planValidation(planNecessary, employeeJob, plan, errors);
//        checkNoRejectValueOccurs();
//
//        planNecessary = false; // не требуется, ошибок быть не может
//        validator.planValidation(planNecessary, employeeJob, plan, errors);
//        checkNoRejectValueOccurs();
//        planNecessary = true;
//
//        // не заполнен план
//        plan = "";
//        validator.planValidation(planNecessary, employeeJob, plan, errors);
//        checkRejectValueOccurs(3);
//        plan = "               ";
//        validator.planValidation(planNecessary, employeeJob, plan, errors);
//        checkRejectValueOccurs(3);
//
//        // не менее 4х слов
//        plan = "Тут какой-то план на следующий день";
//        validator.planValidation(planNecessary, employeeJob, plan, errors);
//        checkNoRejectValueOccurs();
//        plan = "тут три слова";
//        validator.planValidation(planNecessary, employeeJob, plan, errors);
//        checkRejectValueOccurs(3);
//        plan = "Тут с переносом\nслова";
//        validator.planValidation(planNecessary, employeeJob, plan, errors);
//        checkNoRejectValueOccurs();
//        plan = "*&^*(^%^Тут три\n слова";
//        validator.planValidation(planNecessary, employeeJob, plan, errors);
//        checkRejectValueOccurs(3);
//
//    }
//
//    @Test
//    public void testActTypeValidation () throws Exception {
//        Integer actTypeId = Integer.MAX_VALUE;
//
//        validator.actTypeValidation(actTypeId, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//
//        actTypeId = 2;
//        validator.actTypeValidation(actTypeId, 0, errorMessageArgs, errors);
//        checkNoRejectValueOccurs();
//    }
//
//    @Test
//    public void testProjectValidation() throws Exception {
//        Integer actTypeId = 2;
//        Integer projectId = 10;  // Тест может устареть, как только проект ИАСК закроют (active = false)
//        validator.projectValidation(actTypeId, projectId, 0, errorMessageArgs, errors);
//        checkNoRejectValueOccurs();
//
//        // Не указано название проекта
//        actTypeId = DETAIL_TYPE_PROJECT;
//        projectId = 0;
//        validator.projectValidation(actTypeId, projectId, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//
//        // Не указано название пресейла
//        actTypeId = DETAIL_TYPE_PRESALE;
//        projectId = null;
//        validator.projectValidation(actTypeId, projectId, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//
//        // Неверный проект\пресейл
//        projectId = Integer.MAX_VALUE;
//        validator.projectValidation(actTypeId, projectId, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//    }
//
//    @Test
//    public void testProjectRoleValidation() throws Exception {
//        Integer actTypeId = 2;
//        Integer projectRoleId = 5;
//        validator.projectRoleValidation(actTypeId, projectRoleId, 0, errorMessageArgs, errors);
//        checkNoRejectValueOccurs();
//
//        // Не указана проектная роль
//        actTypeId = DETAIL_TYPE_PROJECT;
//        projectRoleId = null;
//        validator.projectRoleValidation(actTypeId, projectRoleId, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//
//        // Неверная проектная роль
//        projectRoleId = Integer.MAX_VALUE;
//        validator.projectRoleValidation(actTypeId, projectRoleId, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//    }
//
//    @Test
//    @Transactional
//    public void testActCatValidation() throws Exception {
//        Integer actCatId = 2;
//        Integer employeeId = 55;
//        ProjectRole employeeJob = validator.getEmployeeJob(employeeId);
//
//        validator.actCatValidation(actCatId, employeeJob, 0, errorMessageArgs, errors);
//        checkNoRejectValueOccurs();
//
//        // Не указана категория активности
//        actCatId = 0;
//        validator.actCatValidation(actCatId, employeeJob, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//
//        // Неверная категория активности
//        actCatId = Integer.MAX_VALUE;
//        validator.actCatValidation(actCatId, employeeJob, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//    }
//
//    @Test
//    public void testProjectTaskValidation() throws Exception {
//        Integer projectId = 10; // Тест может устареть, как только проект ИАСК закроют (active = false)
//        String cqId = "Гарантийная поддержка"; // или этот таск
//        validator.projectTaskValidation(projectId, cqId, 0, errorMessageArgs, errors);
//        checkNoRejectValueOccurs();
//
//        // проект не указан
//        projectId = null;
//        validator.projectTaskValidation(projectId, cqId, 0, errorMessageArgs, errors);
//        checkNoRejectValueOccurs();
//
//        projectId = 10;
//        cqId = null;
//        validator.projectTaskValidation(projectId, cqId, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//
//        projectId = 10;
//        cqId = "вряд ли будет задача с таким названием";
//        validator.projectTaskValidation(projectId, cqId, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//    }
//
//    @Test
//    @Transactional
//    public void testDiscriptionValidation() throws Exception {
//        String discription = "здесь какое-то описание текущей работы";
//        Integer employeeId = 55;
//        ProjectRole employeeJob = validator.getEmployeeJob(employeeId);
//        validator.discriptionValidation(discription, employeeJob, 0, errorMessageArgs, errors);
//        checkNoRejectValueOccurs();
//
//        discription = "                   ";
//        validator.discriptionValidation(discription, employeeJob, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//    }
//
//    @Test
//    public void testDurationValidation() throws Exception {
//        String durationStr = "8";
//        validator.durationValidation(durationStr, 0d, 0, errorMessageArgs, errors);
//        checkNoRejectValueOccurs();
//
//        // Необходимо указать часы
//        durationStr = "    ";
//        validator.durationValidation(durationStr, 0d, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//
//        // Формат
//        durationStr = "1.2.3";
//        validator.durationValidation(durationStr, 0d, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//        durationStr = "-8";
//        validator.durationValidation(durationStr, 0d, 0, errorMessageArgs, errors);
//        checkRejectValueOccurs(4);
//    }
//
//    // Test utils methods
//
//    private void afterChecking(){
//        reset(errors);
//    }
//
//    private void checkRejectValueOccurs(int paramCount){
//        if (paramCount == 3)
//            verify(errors, atLeastOnce()).rejectValue(anyString(), anyString(), anyString());
//        else
//            verify(errors, atLeastOnce()).rejectValue(anyString(), anyString(), (Object[]) anyObject(), anyString());
//        afterChecking();
//    }
//
//    private void checkNoRejectValueOccurs(){
//        verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
//        verify(errors, never()).rejectValue(anyString(), anyString(), (Object[]) anyObject(), anyString());
//        afterChecking();
//    }

}
