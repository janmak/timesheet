package com.aplana.timesheet.form;

import com.aplana.timesheet.enums.TypeOfActivity;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TimeSheetForm {
    private Integer divisionId;
    private Integer employeeId;
    private List<TimeSheetTableRowForm> timeSheetTablePart;
    private String calDate;
    private String plan;
    private double totalDuration;
    /** Начало продолжительного(й) отпуска(болезни) */
    private String beginLongDate;
    /** Конец продолжительного(й) отпуска(болезни) */
    private String endLongDate;
    /** Продолжительный отпуск */
    private boolean longVacation;
    /** Продолжительная болезнь */
    private boolean longIllness;
    /** Причина недоработок, переработок */
    private Integer overtimeCause;
    /** Комментария к причине надоработок/переработко */
    private String overtimeCauseComment;

    public String getBeginLongDate() {
        return beginLongDate;
    }

    public void setBeginLongDate(String beginLongDate) {
        this.beginLongDate = beginLongDate;
    }

    public String getEndLongDate() {
        return endLongDate;
    }

    public void setEndLongDate(String endLongDate) {
        this.endLongDate = endLongDate;
    }

    public boolean isLongVacation() {
        return longVacation;
    }

    public void setLongVacation(boolean longVacation) {
        this.longVacation = longVacation;
    }

    public boolean isLongIllness() {
        return longIllness;
    }

    public void setLongIllness(boolean longIllness) {
        this.longIllness = longIllness;
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public double getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(double totalDuration) {
        this.totalDuration = totalDuration;
    }

    private void filterTable(){
        if (timeSheetTablePart == null) {return;}
        Iterable<TimeSheetTableRowForm> tsTablePart = Iterables.filter(timeSheetTablePart,
                new Predicate<TimeSheetTableRowForm>() {
                    @Override
                    public boolean apply(@Nullable TimeSheetTableRowForm timeSheetTableRowForm) {
                        // По каким-то неведомым причинам при нажатии на кнопку веб интерфейса
                        // "Удалить выбранные строки" (если выбраны промежуточные строки) они удаляются с формы, но
                        // в объект формы вместо них попадают null`ы. Мы эти строки удаляем из объекта формы. Если
                        // удалять последние строки (с конца табличной части формы), то все работает корректно.
                        // Также, если тип активности не выбран значит вся строка пустая, валидацию ее не проводим и удаляем
                        // UPD: теперь делаем фильтрацию
                        TypeOfActivity actType =
                                TypeOfActivity.getById(timeSheetTableRowForm.getActivityTypeId());
                        return actType != null;
                    }
        });

        this.setTimeSheetTablePart(Lists.newArrayList(tsTablePart));
    }

    public List<TimeSheetTableRowForm> getTimeSheetTablePart() {
        filterTable();// удалим пустые строки
        return timeSheetTablePart;
    }

    public String getPlan() {
        return plan;
    }

    public void setTimeSheetTablePart(List<TimeSheetTableRowForm> timeSheetTablePart) {
        this.timeSheetTablePart = timeSheetTablePart;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getCalDate() {
        return calDate;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public void setCalDate(String calDate) {
        this.calDate = calDate;
    }

    public String getPlanEscaped() {
        return StringEscapeUtils.escapeHtml4(this.plan);
    }

    public Integer getOvertimeCause() {
        return overtimeCause;
    }

    public void setOvertimeCause(Integer overtimeCause) {
        this.overtimeCause = overtimeCause;
    }

    public String getOvertimeCauseComment() {
        return overtimeCauseComment;
    }

    public void setOvertimeCauseComment(String overtimeCauseComment) {
        this.overtimeCauseComment = overtimeCauseComment;
    }
}