package com.aplana.timesheet.controller.report;

import com.aplana.timesheet.controller.JasperReportModelAndViewGenerator;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author eshangareev
 * @version 1.0
 */
@Component
public class JasperReportModelAndViewGeneratorsFactory {

    final Map<Integer, JasperReportModelAndViewGenerator> generatorMap = new HashMap<Integer, JasperReportModelAndViewGenerator>();

    @Autowired
    private ListableBeanFactory listableBeanFactory;

    @PostConstruct
    private void init() {
        for (
            Map.Entry<String, JasperReportModelAndViewGenerator> entry :
                listableBeanFactory.getBeansOfType( JasperReportModelAndViewGenerator.class ).entrySet()
        ) {
            generatorMap.put( entry.getValue().getReportId(), entry.getValue() );
        }
    }

    public JasperReportModelAndViewGenerator getJasperReportModelAndViewGeneratorById( Integer id ) {
        return generatorMap.get( id );
    }

}
