package com.aplana.timesheet.form;

/**
 * Created with IntelliJ IDEA.
 * User: eyaroslavtsev
 * Date: 03.08.12
 * Time: 11:02
 * To change this template use File | Settings | File Templates.
 */
public class AdminMessageForm {
    String email;
    String description;
    String name;
    String date;
    String error;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(" name=")
                .append(name)
                .append(" email=")
                .append(email)
                .append(" date=")
                .append(date)
                .append(" description=")
                .append(description)
                .append(" error=")
                .append(error)
                .toString();
    }
}
