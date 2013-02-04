package com.aplana.timesheet.form;

import java.util.List;

/**
 * @author iziyangirov
 * @since 30/01/2013
 */
public class AssignmentLeadersForm {

    List <AssignmentLeadersTableRowForm> tableRows;

    public List<AssignmentLeadersTableRowForm> getTableRows() {
        return tableRows;
    }

    public void setTableRows(List<AssignmentLeadersTableRowForm> tableRows) {
        this.tableRows = tableRows;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("AssignmentLeadersForm");
        sb.append("{tableRows=").append(tableRows);
        sb.append('}');
        return sb.toString();
    }
}

