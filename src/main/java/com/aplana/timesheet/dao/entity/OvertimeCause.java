package com.aplana.timesheet.dao.entity;

import com.aplana.timesheet.enums.DictionaryEnum;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * @author eshangareev
 * @version 1.0
 */
@Entity
@Table(name = "overtime_cause")
public class OvertimeCause {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "overtime_cause_seq")
    @SequenceGenerator(name = "overtime_cause_seq", sequenceName = "overtime_cause_seq", allocationSize = 10)
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", unique = true, nullable = false)
    private TimeSheet timeSheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overtime_cause_id", nullable = false, unique = false)
    private DictionaryItem overtimeCause;

    @Column(name = "comment")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_compensation_id")
    @JoinColumn(name = "compensation_id")
    private DictionaryItem compensation;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TimeSheet getTimeSheet() {
        return timeSheet;
    }

    public void setTimeSheet(TimeSheet timeSheet) {
        this.timeSheet = timeSheet;
    }

    public DictionaryItem getOvertimeCause() {
        return overtimeCause;
    }

    public void setOvertimeCause(DictionaryItem overtimeCause) {
        this.overtimeCause = overtimeCause;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public DictionaryItem getCompensation() {
        return compensation;
    }

    public void setCompensation(DictionaryItem compensation) {
        this.compensation = compensation;
    }

    public boolean isOvertime() {
        return (overtimeCause.getDictionary().getId() == DictionaryEnum.OVERTIME_CAUSE.getId());
    }

    public boolean isUndertime() {
        return (overtimeCause.getDictionary().getId() == DictionaryEnum.UNDERTIME_CAUSE.getId());
    }
}
