package com.aplana.timesheet.reports;

import com.aplana.timesheet.enums.OvertimeCategory;

public class Report01 extends BaseReport {

    public static final String jrName = "report01";

    public static final String jrNameFile = "Отчет №1. Переработки, работа в выходные и праздничные дни";
	
	private OvertimeCategory category;

    private Boolean showNonBillable=true;

    @Override
    public String getJRName() {
        return jrName;
    }

    @Override
    public String getJRNameFile() {
        return jrNameFile;
    }

	public OvertimeCategory getCategory() {
		return category;
	}

	public void setCategory(OvertimeCategory category) {
		this.category = category;
	}

    public Boolean getShowNonBillable() {
        return showNonBillable;
    }

    public void setShowNonBillable(Boolean showNonBillable) {
        this.showNonBillable = showNonBillable;
    }
}
