package com.aplana.timesheet.service;

import com.aplana.timesheet.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.OvertimeCauseDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.OvertimeCause;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.enums.*;
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
        //if (!isOvertimeCauseNeeeded(tsForm, calculateTotalDuration(tsForm))) return; Непонятно зачем тут то проверять? Уже прислали - значит надо записать

        if (tsForm.getOvertimeCause() == null) return;

        OvertimeCause overtimeCause = new OvertimeCause();
        overtimeCause.setOvertimeCause( dictionaryItemService.find(tsForm.getOvertimeCause()) );
        overtimeCause.setTimeSheet(timeSheet);
        overtimeCause.setComment(tsForm.getOvertimeCauseComment());
        overtimeCause.setCompensation(dictionaryItemService.find(tsForm.getTypeOfCompensation()));

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
        final Integer overtimeCauseId = tsForm.getOvertimeCause();
        if (overtimeCauseId == null) return null;

        final OvertimeCausesEnum overtimeCause = EnumsUtils.tryFindById(overtimeCauseId, OvertimeCausesEnum.class);
        final UndertimeCausesEnum unfinishedDayCauses = EnumsUtils.tryFindById(overtimeCauseId, UndertimeCausesEnum.class);
        final WorkOnHolidayCausesEnum workOnHolidayCausesEnum =
                EnumsUtils.tryFindById(overtimeCauseId, WorkOnHolidayCausesEnum.class);

        final TSEnum cause = Preconditions.checkNotNull(
                overtimeCause == null
                        ? (workOnHolidayCausesEnum == null
                            ? unfinishedDayCauses
                            : workOnHolidayCausesEnum
                        )
                        : overtimeCause
        );
        if (cause == OvertimeCausesEnum.OTHER || cause == UndertimeCausesEnum.OTHER ||
            cause == WorkOnHolidayCausesEnum.OTHER
        ) {
            return Preconditions.checkNotNull(tsForm.getOvertimeCauseComment());
        } else {
            return dictionaryItemService.find(overtimeCauseId).getValue();
        }
    }

    public boolean isOvertimeCauseNeeeded(TimeSheetForm tsForm, double totalDuration) {
        for (TimeSheetTableRowForm rowForm : tsForm.getTimeSheetTablePart()) {
            if (
                    TypesOfActivityEnum.isNotCheckableForOvertime(
                        EnumsUtils.tryFindById(
                                rowForm.getActivityTypeId(),
                                TypesOfActivityEnum.class
                        )
                    )
            ) {
                return false;
            }
        }

        return Math.abs(totalDuration - TimeSheetConstants.WORK_DAY_DURATION) > propertyProvider.getOvertimeThreshold();
    }

    public Integer getDictId(Integer overtimeCauseId) {
        DictionaryItem overtimeCause = dictionaryItemService.find(overtimeCauseId);
        return overtimeCause != null ? overtimeCause.getDictionary().getId() : null;
    }
}
