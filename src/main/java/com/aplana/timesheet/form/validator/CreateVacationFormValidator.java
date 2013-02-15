package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.form.CreateVacationForm;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.SecurityService;
import com.aplana.timesheet.service.VacationService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class CreateVacationFormValidator extends AbstractValidator {

    public static final int TYPE_WITH_REQUIRED_COMMENT = VacationTypesEnum.WITH_NEXT_WORKING.getId();
    private static final int MAX_COMMENT_LENGTH = 400;
    private static final String MAX_LENGTH_ERROR_MESSAGE =
            String.format("Длина комментария превышает допустимые %d символов", MAX_COMMENT_LENGTH);

    @Autowired
    private VacationService vacationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EmployeeService employeeService;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(CreateVacationForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        final CreateVacationForm createVacationForm = (CreateVacationForm) o;

        final String calFromDate = createVacationForm.getCalFromDate();
        final String calToDate = createVacationForm.getCalToDate();

        final boolean calFromDateIsNotEmpty = StringUtils.length(calFromDate) > 0;
        final boolean calToDateIsNotEmpty = StringUtils.length(calToDate) > 0;

        if (calFromDateIsNotEmpty && calToDateIsNotEmpty) {
            final Timestamp fromDate = DateTimeUtil.stringToTimestamp(calFromDate);
            final Timestamp toDate = DateTimeUtil.stringToTimestamp(calToDate);

            if (!employeeService.isEmployeeAdmin(securityService.getSecurityPrincipal().getEmployee().getId())) {
                final Date currentDate = new Date();

                if (!fromDate.after(currentDate)) {
                    errors.rejectValue(
                            "calFromDate",
                            "error.createVacation.fromdate.wrong",
                            "Дата начала отпуска должна быть больше текущей даты"
                    );
                }

                if (!toDate.after(currentDate)) {
                    errors.rejectValue(
                            "calToDate",
                            "error.createVacation.todate.wrong",
                            "Дата окончания отпуска должна быть больше текущей даты"
                    );
                }
            }

            final long intersectVacationsCount = vacationService.getIntersectVacationsCount(
                    createVacationForm.getEmployeeId(),
                    fromDate,
                    toDate
            );

            if (intersectVacationsCount > 0) {
                errors.reject(
                        "error.createVacation.wrongperiod",
                        "Указанный период частично или полностью приходится на период отпуска в уже существующем заявлении"
                );
            }

            if (fromDate.after(toDate)) {
                errors.rejectValue(
                        "calToDate",
                        "error.createVacation.wrongtodate",
                        "Дата окончания отпуска не может быть больше даты начала"
                );
            }
        } else {
            if (!calFromDateIsNotEmpty) {
                errors.rejectValue(
                        "calFromDate",
                        "error.createVacation.fromdate.required",
                        "Не указана дата начала отпуска"
                );
            }

            if (!calToDateIsNotEmpty) {
                errors.rejectValue(
                        "calToDate",
                        "error.createVacation.todate.required",
                        "Не указана дата окончания отпуска"
                );
            }
        }

        if (createVacationForm.getVacationType() == 0) {
            errors.reject(
                    "error.createVacation.type.required",
                    "Не указан тип отпуска"
            );
        }

        final int commentLength = StringUtils.length(createVacationForm.getComment());

        if (
                createVacationForm.getVacationType().equals(TYPE_WITH_REQUIRED_COMMENT) &&
                        commentLength == 0
        ) {
            errors.reject(
                    "error.createVacation.comment.required",
                    "Не указан комментарий"
            );
        }

        if (commentLength > MAX_COMMENT_LENGTH) {
            errors.rejectValue(
                    "comment",
                    "error.createVacation.comment.maxlength",
                    new Object[]{ MAX_COMMENT_LENGTH },
                    MAX_LENGTH_ERROR_MESSAGE
            );
        }
    }

}
