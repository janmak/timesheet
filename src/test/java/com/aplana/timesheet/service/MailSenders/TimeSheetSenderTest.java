package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.CalendarDAO;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.TimeSheetDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * @author eshangareev
 * @version 1.0
 */
public class TimeSheetSenderTest extends AbstractTest {

    private String workPlaceString = "Неизвестно_";
    int COUNT_OF_DETAILS = 10;
    private String name = "Василий Иванов";
    private String reason = "Болезнь";

    @Test
    public void testTemplate() throws Exception {
        String s = getProccessedTemplate();
        Assert.assertEquals(StringUtils.countMatches(s, workPlaceString), 10);
        Assert.assertTrue(s.contains(name));
    }

    private String getProccessedTemplate() {
        VelocityEngine velocityEngine = getVelocityEngine();
        HashMap model = new HashMap();

        model.put("paramsForGenerateBody", getParamsForGenerateBody());

        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "sendmail.vm", model);
    }

    private Object getParamsForGenerateBody() {
        Table<Integer, String, String> result = HashBasedTable.create();
        for (int i = 0; i < COUNT_OF_DETAILS; i++) {
            result.put(i, TimeSheetSender.WORK_PLACE, workPlaceString + i);
            result.put(i, TimeSheetSender.ACT_TYPE, "Плевание в молоток-" + i);
            result.put(i, TimeSheetSender.PROJECT_NAME, "Прнимание горизонтального положения на печи-" + i);
            result.put(i, TimeSheetSender.CATEGORY_OF_ACTIVITY, "Тунеядство_" + i);
            result.put(i, TimeSheetSender.CQ_ID, "Не знаю, что это вообще такое-" + i);
            result.put(i, TimeSheetSender.DURATION, Integer.toString(i));
            result.put(i, TimeSheetSender.DESCRIPTION_STRINGS, "Очень долго-предолго сидел на печи.\nЗадница уже затекла, а я все лежал.\nСложное это дело, должен я вам сказать.\nОчень!");
            result.put(i, TimeSheetSender.PROBLEM_STRINGS, "Вы думаете, что в лежании на печи нет никаких проблем?\nВы сильно ошибаетесь!\nПоговорим в следующий раз!\nРаботать надо, т.е… лежать.");
            result.put(i, TimeSheetSender.PROBLEM_STRINGS, "Все так же мучаться\nНести свое тяжкое бремя");
        }

        result.put(0, TimeSheetSender.OVERTIME_CAUSE, "Много чего повлияло на этот бесславный результат\n А так конечно, ничего, захотелось.");

        result.put(0, TimeSheetSender.SENDER_NAME, name);
        return result;

    }

    private VelocityEngine getVelocityEngine() {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("resource.loader", "class");
        velocityEngine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.setProperty("input.encoding", "UTF-8");
        velocityEngine.setProperty("output.encoding", "UTF-8");
        return velocityEngine;
    }
}