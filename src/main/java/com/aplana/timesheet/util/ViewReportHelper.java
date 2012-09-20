package com.aplana.timesheet.util;

import com.aplana.timesheet.dao.entity.DayTimeSheet;
import com.aplana.timesheet.service.TimeSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class ViewReportHelper {

    @Autowired
    TimeSheetService timeSheetService;
    @Transactional
    public String getDateReportsListJson(Integer year, Integer month, Integer employeeId) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        List<DayTimeSheet> calTSList = timeSheetService.findDatesAndReportsForEmployee(year, month, employeeId);

        for (int i = 0; i < calTSList.size(); i++) {
            DayTimeSheet queryResult = calTSList.get(i);
            sb.append("\"");
            String day = new SimpleDateFormat("yyyy-MM-dd").format(queryResult.getCalDate());
            sb.append(day);
            sb.append("\":\"");
            if (queryResult.getId() != null)
                sb.append("1\"");   //если есть отчет
            else if (!queryResult.getWorkDay())
                sb.append("2\"");   //если выходной или праздничный день
            else
                sb.append("0\"");   //если нет отчета
            if (i < (calTSList.size() - 1))
                sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }
}
