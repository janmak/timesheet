package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author eshangareev
 * @version 1.0
 */
public class Mail {
    private Division division;
    private String ccEmail;
    private String fromEmail;
    private String subject;
    private Iterable<String> toEmails = new ArrayList<String>();

    private Iterable<Employee> employeeList;

    private Map<Employee, List<String>> passedDays = new HashMap<Employee, List<String>>();
    private Iterable<MultipartFile> filePahts;
    private String preconstructedMessageBody;
    private String date;
    private Table<Integer, String, String> paramsForGenerateBody;

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public Division getDivision() {
        return division;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public String getCcEmail() {
        return ccEmail;
    }

    public void setCcEmail(String ccEmail) {
        this.ccEmail = ccEmail;
    }

    public Iterable<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(Iterable<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public Map<Employee, List<String>> getPassedDays() {
        return passedDays;
    }

    public Iterable<MultipartFile> getFilePahts() {
        return filePahts;
    }

    public void setFilePahts(Iterable<MultipartFile> filePahts) {
        this.filePahts = filePahts;
    }

    public String getPreconstructedMessageBody() {
        return preconstructedMessageBody;
    }

    public void setPreconstructedMessageBody(String preconstructedMessageBody) {
        this.preconstructedMessageBody = preconstructedMessageBody;
    }

    public Iterable<String> getToEmails() {
        return toEmails;
    }

    public void setToEmails(Iterable<String> toEmails) {
        this.toEmails = Sets.newHashSet(toEmails); //удаляем дупликаты
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setParamsForGenerateBody(Table<Integer, String, String> paramsForGenerateBody) {
        this.paramsForGenerateBody = paramsForGenerateBody;
    }

    public Table<Integer, String, String> getParamsForGenerateBody() {
        return paramsForGenerateBody;
    }

    public void setToEmails(List<String> emailsToAdd) {
        this.toEmails = emailsToAdd;
    }

}
