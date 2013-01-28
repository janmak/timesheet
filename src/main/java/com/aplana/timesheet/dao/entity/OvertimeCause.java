package com.aplana.timesheet.dao.entity;

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

    @OneToOne
    @JoinColumn(name = "timesheet_id", unique = true, nullable = false)
    private TimeSheet timeSheet;

    @ManyToOne
    @JoinColumn(name = "overtime_cause_id", nullable = false, unique = false)
    private DictionaryItem overtimeCause;

    @Column(name = "comment")
    private String comment;

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
}
