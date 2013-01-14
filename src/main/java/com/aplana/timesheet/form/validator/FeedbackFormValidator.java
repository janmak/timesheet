package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.service.DivisionService;
import com.aplana.timesheet.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FeedbackFormValidator implements Validator {
	
	@Autowired
	private DivisionService divisionService;
	@Autowired
	private EmployeeService employeeService;
	
	private Long SUM_FILE_SIZE = 8388608L;//8 мегабайт
	
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(FeedbackForm.class);
	}
	
	public void validate(Object target, Errors errors){
		FeedbackForm fbForm = (FeedbackForm) target;
		
		Integer divisionId = fbForm.getDivisionId();
		Integer employeeId = fbForm.getEmployeeId();
		Integer feedbackType = fbForm.getFeedbackType();
		String feedbackDescription = fbForm.getFeedbackDescription();
		String name = fbForm.getName();
		String email = fbForm.getEmail();
		MultipartFile[] files = new MultipartFile[]{fbForm.getFile1Path(), fbForm.getFile2Path()};
		
		
		// Тип проблемы не выбран.
		if (feedbackType == null) {
			errors.rejectValue("feedbackType", "error.fbform.feedbackType.required", 	"Тип сообщения не выбран.");
		}
		// Неверный тип проблемы
		else{ 
			if (!isFeedbackTypeValid( feedbackType )) {
				errors.rejectValue("feedbackType", "error.fbform.feedbackType.invalid", "Неверный тип сообщения.");
			}else{ 
				if(!feedbackType.equals("Меня нет в списке")){
					// Подразделение не выбрано.
					if (divisionId == null || divisionId == 0) {
							errors.rejectValue("divisionId", "error.fbform.divisionId.required", "Подразделение не выбрано.");
					}
					// Неверное подразделение.
					else if (!isDivisionValid(divisionId)) {
						errors.rejectValue("divisionId", "error.fbform.divisionId.invalid", "Выбрано неверное подразделение.");
					}

					// Сотрудник не выбран.
					if (employeeId == null || employeeId == 0) {
						errors.rejectValue("employeeId", "error.fbform.employeeId.required", 	"Сотрудник не выбран.");
					}
					// Неверный сотрудник
					else if (!isEmployeeValid(employeeId)) {
						errors.rejectValue("employeeId", "error.fbform.employeeId.invalid", "Неверные данные сотрудника.");
					}
				}else{
					divisionId = 0;
					employeeId = 0;
					if(name.isEmpty()){
						errors.rejectValue("name", "error.fbform.name.required", "Не введено имя отправителя.");
					}
					if(email.isEmpty()){
						errors.rejectValue("email", "error.fbform.email.required", "Не введен почтовый адрес отправителя.");
					}else{
						if(!isEmailValid(email))
							errors.rejectValue("email", "error.fbform.email.invalid", "Неверный почтовый адрес отправителя.");
					}
				}
			}
		}
		// Суть проблемы не описана
		if (feedbackDescription == null || feedbackDescription.equals( "" ) ) {
			errors.rejectValue("feedbackDescription", "error.fbform.feedbackDescription.required", "Текст сообщения не заполнен.");
		}
		// Лимит на суммарный размер файлов првышен
		if(!areFilesValid(files)){
			errors.rejectValue("file1Path", "error.fbform.attachSize.invalid", "Лимит на суммарный размер файлов превышен");
		}
		
	}
	
	
	private boolean isDivisionValid(Integer division) {
        return divisionService.find( division ) != null;
    }
	private boolean isEmployeeValid(Integer employee) {
        return employeeService.find( employee ) != null;
    }
	private boolean isFeedbackTypeValid(int feedbackType) {
		return feedbackType > -1 && feedbackType < 6;
	}
	private boolean areFilesValid(MultipartFile[] files){
		long sumSize = 0;
        for ( MultipartFile file : files ) {
            if ( file != null && ! file.isEmpty() ) {
                sumSize += file.getSize();
            }
        }
        return (sumSize <= SUM_FILE_SIZE);
	}
	private boolean isEmailValid(String email){
		Pattern pattern = Pattern.compile("[a-zA-Z][a-zA-Z\\d\\u002E\\u005F]+@([a-zA-Z]+\\u002E){1,2}((net)|(com)|(org)|(ru))");
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
}
