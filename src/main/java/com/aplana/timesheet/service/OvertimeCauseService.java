package com.aplana.timesheet.service;

import com.aplana.timesheet.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.OvertimeCauseDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.OvertimeCause;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.enums.OvertimeCausesEnum;
import com.aplana.timesheet.enums.TSEnum;
import com.aplana.timesheet.enums.UndertimeCausesEnum;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.util.EnumsUtils;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author eshangareev
 * @version 1.0
 */
@Service("overtimeCauseService")
public class OvertimeCauseService {

    @Autowired
    private OvertimeCauseDAO dao;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private TSPropertyProvider propertyProvider;


    @Transactional
    public void store(TimeSheet timeSheet, TimeSheetForm tsForm) {
        double totalDuration = calculateTotalDuration(tsForm);

        if (!isOvertimeCauseNeeeded(totalDuration)) return;

        OvertimeCause overtimeCause = new OvertimeCause();
        overtimeCause.setOvertimeCause( dictionaryItemService.find(tsForm.getOvertimeCause()) );
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
        UndertimeCausesEnum unfinishedDayCauses = EnumsUtils.tryFindById(overtimeCauseId, UndertimeCausesEnum.class);
        TSEnum cause = Preconditions.checkNotNull(overtimeCause == null? unfinishedDayCauses : overtimeCause);
        if (cause == OvertimeCausesEnum.OTHER || cause == UndertimeCausesEnum.OTHER) {
            return Preconditions.checkNotNull(tsForm.getOvertimeCauseComment());
        } else {
            return dictionaryItemService.find(overtimeCauseId).getValue();
        }
    }

    public boolean isOvertimeCauseNeeeded(double totalDuration) {
        return Math.abs(totalDuration - TimeSheetConstants.WORK_DAY_DURATION) > propertyProvider.getOvertimeThreshold();
    }

    public Integer getDictId(Integer overtimeCauseId) {
        DictionaryItem overtimeCause = dictionaryItemService.find(overtimeCauseId);
        return overtimeCause != null ? overtimeCause.getDictionary().getId() : null;
    }
}
