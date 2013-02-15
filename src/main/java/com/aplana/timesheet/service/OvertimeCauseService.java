package com.aplana.timesheet.service;

import com.aplana.timesheet.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.OvertimeCauseDAO;
import com.aplana.timesheet.dao.entity.OvertimeCause;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.enums.OvertimeCausesEnum;
import com.aplana.timesheet.enums.TSEnum;
import com.aplana.timesheet.enums.UnfinishedDayCausesEnum;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.util.EnumsUtils;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author eshangareev
 * @version 1.0
 */
@Service("overtimeCauseService")
public class OvertimeCauseService {

    @Autowired
    private OvertimeCauseDAO dao;
    @Autowired
    private DictionaryItemDAO dictionaryItemDAO;
    @Autowired
    private TSPropertyProvider propertyProvider;


    public void store(TimeSheet timeSheet, TimeSheetForm tsForm) {
        double totalDuration = calculateTotalDuration(tsForm);

        if (!isOvertimeCauseNeeeded(totalDuration)) return;

        OvertimeCause overtimeCause = new OvertimeCause();
        overtimeCause.setOvertimeCause( dictionaryItemDAO.find(tsForm.getOvertimeCause()) );
        overtimeCause.setTimeSheet(timeSheet);
        overtimeCause.setComment(tsForm.getOvertimeCauseComment());

        dao.store(overtimeCause);
    }

    private double calculateTotalDuration(TimeSheetForm tsForm) {
        double totalDuration = 0D;
        for (TimeSheetTableRowForm tableRowForm : tsForm.getTimeSheetTablePart()) {
            totalDuration += Double.parseDouble(tableRowForm.getDuration());
        }

        return totalDuration;
    }

    public String getCauseName(TimeSheetForm tsForm) {
        Integer overtimeCauseId = tsForm.getOvertimeCause();
        if (!isOvertimeCauseNeeeded(tsForm.getTotalDuration()) || overtimeCauseId == null) return null;

        OvertimeCausesEnum overtimeCause = EnumsUtils.tryFindById(overtimeCauseId, OvertimeCausesEnum.class);
        UnfinishedDayCausesEnum unfinishedDayCauses = EnumsUtils.tryFindById(overtimeCauseId, UnfinishedDayCausesEnum.class);
        TSEnum cause = Preconditions.checkNotNull(overtimeCause == null? unfinishedDayCauses : overtimeCause);
        if (cause == OvertimeCausesEnum.OTHER || cause == UnfinishedDayCausesEnum.OTHER) {
            return Preconditions.checkNotNull(tsForm.getOvertimeCauseComment());
        } else {
            return dictionaryItemDAO.find(overtimeCauseId).getValue();
        }
    }

    public boolean isOvertimeCauseNeeeded(double totalDuration) {
        return Math.abs(totalDuration - TimeSheetConstants.WORK_DAY_DURATION) > propertyProvider.getOvertimeThreshold();
    }
}
