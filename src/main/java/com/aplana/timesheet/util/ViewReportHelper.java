package com.aplana.timesheet.util;

import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.form.entity.DayTimeSheet;
import com.aplana.timesheet.service.TimeSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;

import static argo.jdom.JsonNodeBuilders.aStringBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;

@Service
public class ViewReportHelper {

    @Autowired
    TimeSheetService timeSheetService;
    @Transactional
    public String getDateReportsListJson(Integer year, Integer month, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();
        final List<DayTimeSheet> calTSList = timeSheetService.findDatesAndReportsForEmployee(year, month, employeeId);

        for (DayTimeSheet queryResult : calTSList) {
            final String day = new SimpleDateFormat(DateTimeUtil.DATE_PATTERN).format(queryResult.getCalDate());

            Integer value = 0; //если нет отчета

            if ((queryResult.getId() != null) || (queryResult.getVacationDay()) || (queryResult.getIllnessDay()))
                value = 1;   //если есть отчет
            else if (!queryResult.getWorkDay())
                value = 2;   //если выходной или праздничный день

            builder.withField(day, aStringBuilder(value.toString()));
        }

        return JsonUtil.format(builder.build());
    }
}
