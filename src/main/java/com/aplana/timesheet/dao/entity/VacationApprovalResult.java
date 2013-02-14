package com.aplana.timesheet.dao.entity;

import javax.persistence.*;

/**
 * User: vsergeev
 * Date: 08.02.13
 */
@Entity
@Table(name = "vacation_approval_result")
public class VacationApprovalResult {

    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = "vacation_approval_result_seq")
    @SequenceGenerator(name = "vacation_approval_result_seq", sequenceName = "vacation_approval_result_seq", allocationSize = 10)
    @Column (name = "id", columnDefinition = "INTEGER NOT NULL")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_approval_id", columnDefinition = "INTEGER NOT NULL")
    private VacationApproval vacationApproval;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_participant_id", columnDefinition = "INTEGER")
    private ProjectParticipant projectParticipant;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public VacationApproval getVacationApproval() {
        return vacationApproval;
    }

    public void setVacationApproval(VacationApproval vacationApproval) {
        this.vacationApproval = vacationApproval;
    }

    public ProjectParticipant getProjectParticipant() {
        return projectParticipant;
    }

    public void setProjectParticipant(ProjectParticipant projectParticipant) {
        this.projectParticipant = projectParticipant;
    }
}
