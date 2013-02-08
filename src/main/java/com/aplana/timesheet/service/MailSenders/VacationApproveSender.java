package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.ParticipantMailHierarchyEnum;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import com.aplana.timesheet.exception.service.CalendarServiceException;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * User: vsergeev
 * Date: 04.02.13
 */
public class VacationApproveSender extends MailSender<VacationApproval> {

    private static final Integer BEFORE_VACATION_DAYS_DEFAULT = 14;
    private static final String WRONG_BEFORE_VACATION_DAYS_ERROR = "В настройках указано неверное количество дней до отпуска, по которым будем формировать рассылку!";

    @Override
    protected List<Mail> getMailList(VacationApproval vacationApproval) {
        final Mail mail = new Mail();
        Vacation vacation = vacationApproval.getVacation();

        mail.setFromEmail(sendMailService.getEmployeeEmail(vacation.getEmployee().getId()));
        mail.setToEmails(Arrays.asList(vacationApproval.getManager().getEmail()));
        mail.setSubject(getSubject(vacation));
        mail.setParamsForGenerateBody(getParamsForGenerateBody(vacationApproval));

        return Arrays.asList(mail);
    }

    private Table<Integer, String, String> getParamsForGenerateBody(VacationApproval vacationApproval) {
        final Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(vacationApproval));

        return table;
    }

    private String getBody(VacationApproval vacationApproval) {
        Vacation vacation = vacationApproval.getVacation();

        String vacationTypeStr = vacation.getType().getValue();
        String employeeNameStr = vacation.getEmployee().getName();
        String regionNameStr = vacation.getEmployee().getRegion().getName();
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);
        String commentStr = StringUtils.EMPTY;
        String approveURL = String.format("http://timesheet.aplana.com/vacation_approval?uid={%s}", vacationApproval.getUid());
        if (StringUtils.isNotBlank(vacation.getComment())) {
            commentStr = String.format("Комментарий: %s. ", vacation.getComment());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Просьба принять решение по отпуску %s ", vacationTypeStr));
        stringBuilder.append(String.format("сотрудника %s ", employeeNameStr));
        stringBuilder.append(String.format("из г. %s ", regionNameStr));
        stringBuilder.append(String.format("на период с %s - %s. ", beginDateStr, endDateStr));
        stringBuilder.append(String.format("%s", commentStr));
        stringBuilder.append(String.format("Для регистрации Вашего решения нажмите на ссылку: (%s).", approveURL));

        return stringBuilder.toString();
    }

    private String getSubject(Vacation vacation) {
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);

        return  propertyProvider.getVacationMailMarker() +   // APLANATS-573
                String.format(" Запрос согласования отпуска %s %s - %s", vacation.getEmployee().getName(),
                        beginDateStr, endDateStr);
    }

    @Override
    protected void initMessageBody(Mail mail, MimeMessage message) throws MessagingException {
        try {
            message.setText(mail.getParamsForGenerateBody().get(FIRST, MAIL_BODY), "UTF-8", "plain");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    public VacationApproveSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    /**
     * получаем список электронных адресов руководителей проектов, на которых сотрудник планирует свою занятость в даты отпуска.
     * если таких проектов нет, то получаем список проектов, по которым сотрудник списывал занятость за определенное (задается в настройках либо берется дефолтное значение)
     * количество дней до подачи заявление на отпуск.
     */
    public static Iterable<String> getProjectManagerEmails(Vacation vacation, SendMailService mailService, TSPropertyProvider propProvider) throws CalendarServiceException {
        List<Project> employeeProjects = mailService.getEmployeeProjectPlanByDates(vacation.getBeginDate(), vacation.getEndDate(), vacation.getEmployee());
        if (employeeProjects.isEmpty()) {
            Integer beforeVacationDays = getBeforeVacationDays(propProvider);
            Date periodBeginDate = DateUtils.addDays(vacation.getCreationDate(), 0 - beforeVacationDays);
            employeeProjects = mailService.getEmployeeProjectsByDates(periodBeginDate, vacation.getCreationDate(), vacation.getEmployee());
        }

        return getManagerMailsInProjects(vacation.getEmployee(), employeeProjects, mailService, vacation);
    }

    /**
     * получаем список электронных адресов руководителей сотрудника на выбранных проектах, которые не ответили на письмо о согласовании.
     */
    private static Iterable<String> getManagerMailsInProjects(Employee employee, List<Project> employeeProjects, SendMailService mailService, Vacation vacation) {
        Set<String> chiefsMailAdresses = new HashSet<String>();
        for (Project project : employeeProjects) {
            Set<ProjectRolesEnum> chiefRoles = getChiefsRolesEnumByEmployeeRole(employee.getJob());
            List<Integer> chiefRolesIds = CollectionUtils.transform(chiefRoles, new Transformer() {
                @Override
                public Integer transform(Object o) {
                    return ((ProjectRolesEnum) o).getId();
                }
            });
            chiefsMailAdresses.add(project.getManager().getEmail());
            chiefsMailAdresses.addAll(mailService.getEmailAddressesOfManagersThatDoesntApproveVacation(chiefRolesIds, project, vacation));
        }

        chiefsMailAdresses.remove(employee.getEmail()); //удаляем емаил сотрудника, если он сам руководитель

        return chiefsMailAdresses;
    }

    /**
     * получаем список ролей, которые являются руководителями для роли сотрудника на проекте
     */
    private static Set<ProjectRolesEnum> getChiefsRolesEnumByEmployeeRole(ProjectRole employeeProjectRole) {
        ParticipantMailHierarchyEnum mailHierarchyEnum = ParticipantMailHierarchyEnum.tryFindEnumByRoleId(employeeProjectRole.getId());
        if (mailHierarchyEnum != null) {
            return mailHierarchyEnum.getChiefsProjectRolesEnums();
        } else {
            return ParticipantMailHierarchyEnum.getDafaultChiefsRolesEnum();
        }
    }

    /**
     * получаем количество дней, которое вычтем из даты создания заявления на отпуск и будем искать для утверждения
     * заявления на отпуск менеджеров проектов, по которым сотрудник списывал занятость в этом промежутке времени
     * @param propProvider
     */
    private static Integer getBeforeVacationDays(TSPropertyProvider propProvider) {
        try {
            return propProvider.getBeforeVacationDays();
        } catch (NullPointerException ex){
            return BEFORE_VACATION_DAYS_DEFAULT;
        } catch (NumberFormatException ex) {
            logger.error(WRONG_BEFORE_VACATION_DAYS_ERROR);
            throw ex;
        }
    }

}
