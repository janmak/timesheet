package com.aplana.timesheet.dao.entity;

import com.aplana.timesheet.dao.Identifiable;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Set;

@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "job", "division"}))
public class Employee implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emp_seq")
    @SequenceGenerator(name = "emp_seq", sequenceName = "emp_seq", allocationSize = 10)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(name = "start_date", columnDefinition = "date not null")
    private Timestamp startDate;

	@Column
	private String ldap;

    @Column(name="end_date")
    private Timestamp endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager")
    @ForeignKey(name = "FK_EMP_MANAGER")
    private Employee manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division", nullable = false)
    @ForeignKey(name = "FK_EMP_DIVISION")
    private Division division;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job", nullable = false)
    @ForeignKey(name = "FK_EMP_JOB")
    private ProjectRole job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region")
    @ForeignKey(name = "FK_EMP_REGION")
    private Region region;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "employee_permissions",
               joinColumns = {
                       @JoinColumn(name = "employee_id", nullable = false) },
               inverseJoinColumns = {
                       @JoinColumn(name = "permission_id", nullable = false) })
    private Set<Permission> permissions;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Vacation> vacations;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Illness> illnesses;

    @Column(name = "not_to_sync", columnDefinition = "bool not null default false")
    private boolean notToSync;

    @Column(length = 50, name = "ldap_object_sid")
    private String objectSid;

    @Column(name = "job_rate", columnDefinition = "float default 1", nullable = false)
    private Float jobRate;

    public Set<Vacation> getVacations() {
        return vacations;
    }

    public void setVacations(Set<Vacation> vacations) {
        this.vacations = vacations;
    }

    public Set<Illness> getIllnesses() {
        return illnesses;
    }

    public void setIllnesses(Set<Illness> illnesses) {
        this.illnesses = illnesses;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean isNotToSync() {
        return notToSync;
    }

    public void setNotToSync(boolean notToSync) {
        this.notToSync = notToSync;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public ProjectRole getJob() {
        return job;
    }

    public void setJob(ProjectRole job) {
        this.job = job;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

	public String getLdap() {
		return ldap;
	}

	public void setLdap(String ldap) {
		this.ldap = ldap;
	}

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public String getObjectSid() {
        return objectSid;
    }

    public void setObjectSid(String objectSid) {
        this.objectSid = objectSid;
    }

    public Float getJobRate() {
        return jobRate;
    }

    public void setJobRate(Float jobRate) {
        this.jobRate = jobRate;
    }

    //проверяем уволенный ли сотрудник
    //ts==null сравниваем с текущей датой
    public boolean isDisabled(Timestamp ts) {
        if(endDate!=null) {
            if(ts==null) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1);
                ts = new Timestamp(c.getTimeInMillis());
            }
            return ts.compareTo(endDate)>0;
        }
        else return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;

        Employee employee = (Employee) o;

        if (id != null ? !id.equals(employee.id) : employee.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Employee");
        sb.append("{id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", startDate=").append(startDate);
        sb.append(", ldap='").append(ldap).append('\'');
        sb.append(", endDate=").append(endDate);
        sb.append(", manager=").append(manager);
        sb.append(", division=").append(division);
        sb.append(", job=").append(job);
        sb.append(", region=").append(region);
        sb.append(", permissions=").append(permissions);
        sb.append(", notToSync=").append(notToSync);
        sb.append(", objectSid='").append(objectSid).append('\'');
        sb.append(", jobRate=").append(jobRate);
        sb.append('}');
        return sb.toString();
    }

}