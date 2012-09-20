package com.aplana.timesheet.reports;


import com.aplana.timesheet.dao.JasperReportDAO;
import com.aplana.timesheet.util.DateTimeUtil;

public abstract class BaseReport implements TSJasperReport {

    protected JasperReportDAO reportDAO;

    @Override
    public void setReportDAO(JasperReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    @Override
    public void checkParams() {
        this.beginDate = "".equals(this.beginDate) ? DateTimeUtil.MIN_DATE : this.beginDate;
        this.endDate = "".equals(this.endDate) ? DateTimeUtil.MAX_DATE : this.endDate;
    }

    protected String beginDate;

    protected String endDate;

    protected Integer regionId;

    protected String regionName;

    public String getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
}
