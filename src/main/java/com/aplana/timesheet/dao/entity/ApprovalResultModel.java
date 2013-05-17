package com.aplana.timesheet.dao.entity;

/**
 * @author Aalikin
 * @since 17.05.13
 */
public class ApprovalResultModel {

    public ApprovalResultModel(){
        super();
    }

    public ApprovalResultModel(String role, String name, String result){
        this.role = role;
        this.name = name;
        this.result = result;
    }

    private String role;
    private String name;
    private String result;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
