package com.aplana.timesheet.form;

import org.springframework.web.multipart.MultipartFile;

public class FeedbackForm
{
	private Integer divisionId;
	private Integer employeeId;
	private Integer feedbackType;
	private String feedbackTypeName;
	private String feedbackDescription;
	private String name;
	private String email;
	private MultipartFile file1Path;
	private MultipartFile file2Path;
	
	public Integer getDivisionId(){
		return divisionId;
	}

	public void setDivisionId(Integer divisionId){
		this.divisionId = divisionId;
	}
	
	public Integer getEmployeeId(){
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId){
		this.employeeId = employeeId;
	}
	
	public Integer getFeedbackType(){
		return feedbackType;
	}
	
	public void setFeedbackType(Integer feedbackType){
		this.feedbackType = feedbackType;
	}
	
	public String getFeedbackTypeName() {
		return feedbackTypeName;
	}

	public void setFeedbackTypeName(String feedbackTypeName) {
		this.feedbackTypeName = feedbackTypeName;
	}

	public String getFeedbackDescription(){
		return feedbackDescription;
	}
	
	public void setFeedbackDescription(String feedbackDescription){
		this.feedbackDescription = feedbackDescription;
	}

	public void setFile1Path(MultipartFile file1Path) {
		this.file1Path = file1Path;
	}

	public MultipartFile getFile1Path() {
		return file1Path;
	}

	public void setFile2Path(MultipartFile file2Path) {
		this.file2Path = file2Path;
	}

	public MultipartFile getFile2Path() {
		return file2Path;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}	
}