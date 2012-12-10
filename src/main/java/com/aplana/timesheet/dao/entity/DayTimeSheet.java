package com.aplana.timesheet.dao.entity;

import com.aplana.timesheet.dao.TimeSheetDAO;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class DayTimeSheet implements Comparable<DayTimeSheet> {

    private TimeSheetDAO timeSheetDAO;
    private Employee emp;
    private Timestamp calDate;
    private Timestamp current;
    private Boolean workDay;
    private Integer id;
    /**
     * Отпуск или отгул или ещё что (не я это придумал так было до меня)
     */
    private Integer act_type;
    private Boolean isLoadDuration = false;
    private BigDecimal duration;
    private Boolean isLoadTimeSheet = false;
    private TimeSheet timeSheet;

    public DayTimeSheet(Timestamp calendarDate, Boolean isHoliday, Integer timeSheetId, Integer act_type, BigDecimal dur, Employee emp) {
        this.setCalDate(calendarDate);
        this.setWorkDay(!isHoliday); // APLANATS-266. workday = true - выходной день, а false - рабочий!
        this.setId(timeSheetId);
        this.setAct_type(act_type);
        this.setCurrent();
        this.setDuration(dur);
        this.setEmp(emp);
    }

    public Integer getAct_type() {
        return act_type;
    }

    public void setAct_type(Integer act_type) {
        this.act_type = act_type;
    }

    public BigDecimal getDuration() {
        return this.duration;
    }

    public void setDuration(BigDecimal duration) {
        this.duration = duration;
        if (this.duration == null)
            this.duration = new BigDecimal(0);
    }

    public Timestamp getCalDate() {
        return calDate;
    }

    public void setCalDate(Timestamp calDate) {
        this.calDate = calDate;
    }

    /**
     * Сообщает нам выходной ли день или рабочий
     *
     * @return
     */
    public Boolean getWorkDay() {
        return workDay;
    }

    public void setWorkDay(Boolean workDay) {
        this.workDay = workDay;
    }

    public Integer getId() {
        return id;
    }

    /**
     *
     * @return TimeSheet или null
     */
    public TimeSheet getTimeSheet() {
        if (this.getId() != null) {
            if (!this.isLoadTimeSheet) {
                this.timeSheet = this.timeSheetDAO.find(this.getId());
                this.isLoadTimeSheet = true;
            }
            return this.timeSheet;
        }
        return null;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTimeSheetDAO(TimeSheetDAO timeSheetDAO) {
        this.timeSheetDAO = timeSheetDAO;
    }

    public Employee getEmp() {
        return emp;
    }

    public void setEmp(Employee emp) {
        this.emp = emp;
    }

    @Override
    public int compareTo(DayTimeSheet o) {
        if (getCalDate().getTime() < o.getCalDate().getTime()) {
            return -1;
        } else if (getCalDate().getTime() == o.getCalDate().getTime()) {
            return 0;
        } else {
            return 1;
        }
    }

    private Timestamp getCurrent() {
        return current;
    }

    /**
     * Получает текущее время в Timestamp
     */
    private void setCurrent() {
        this.current = new Timestamp(new Date().getTime());
    }
    
    /**
     * Сообщает что работнег ещё не приступил к исполнению обязанностей и день учитывать не стоит
     *
     * @param emp
     * @return
     */
    public Boolean getStatusNotStart() {
        if (this.getWorkDay() && this.getEmp().getStartDate().after(this.getCalDate())) {
            return true;
        }
        return false;
    }

    /**
     * Этот день ещё не настал (больше чем текущая дата)
     * @return
     */
    public Boolean getStatusNotCome() {
        if (!this.getStatusHoliday() && this.getCurrent().before(this.getCalDate())) {
            return true;
        }
        return false;
    }

    /**
     * Отработал и есть отчёт
     *
     * @return
     */
    public Boolean getStatusNormalDay() {
        if (this.getWorkDay() && this.getTimeSheet() != null) {
            return true;
        }
        return false;
    }

    /**
     * Рабочий день и у человека нет отчёта
     * @return 
     */
    public Boolean getStatusNoReport() {
        if (!this.getStatusNotCome() && this.getWorkDay() && this.getTimeSheet() == null && !this.getStatusNotStart()) {
            return true;
        }
        return false;
    }

    /**
     * Выходной день и у человека есть отчёт(скорей всего работал в выходные)
     * @return 
     */
    public Boolean getStatusWorkOnHoliday() {
        if (!this.getWorkDay() && this.getTimeSheet() != null) {
            return true;
        }
        return false;
    }

    /**
     * Выходной день и человек не работал, отдых
     * @return 
     */
    public Boolean getStatusHoliday() {
        if (!this.getWorkDay() && this.getTimeSheet() == null) {
            return true;
        }
        return false;
    }
}
