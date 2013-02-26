package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.AvailableActivityCategoryDAO;
import com.aplana.timesheet.dao.entity.AvailableActivityCategory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:WEB-INF/spring/testApplicationContext.xml"})
public class TimeSheetServiceTest {

    @Autowired
    TimeSheetService timeSheetService;

    @Autowired
    AvailableActivityCategoryDAO availableActivityCategoryDAO;

    public String getListOfActDescriptoinForTest(){
        List<AvailableActivityCategory> availableActivityCategories = availableActivityCategoryDAO.getAllAvailableActivityCategories();
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (AvailableActivityCategory activityCategory : availableActivityCategories){
            result.append("{");
            result.append("\"actCat\":" + activityCategory.getActCat().getId() + ",");
            result.append("\"actType\":" + activityCategory.getActType().getId() + ",");
            result.append("\"projectRole\":" + activityCategory.getProjectRole().getId() + ",");
            result.append("\"description\":\"");
            if (activityCategory.getDescription() != null){
                result.append(activityCategory.getDescription());
            }
            result.append("\"");
            result.append("},");
        }
        result.append("{\"actCat\":0,\"actType\":0,\"projectRole\":0,\"description\":\"\"}");
        result.append("]");
        return result.toString();
    }

    @Test
    @Transactional
    public void testGetListOfActDescriptoin(){
        String currentResult = timeSheetService.getListOfActDescriptoin();
        String testResult = getListOfActDescriptoinForTest();
        assertEquals(testResult, currentResult);
    }

}
