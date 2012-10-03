package com.aplana.timesheet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class MailUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);

    /**
     * Возвращает адреса имеил без дубликатов
     *
     * @param emails
     */
    public static String deleteEmailDublicates(String emails) {
        Set<String> uniqueEmails = new HashSet<String>();
        String[] splittedEmails = emails.split(",");
        for (String splitEmail : splittedEmails) {
            uniqueEmails.add(splitEmail.trim());
        }
        StringBuilder result = new StringBuilder();
        Iterator<String> iter = uniqueEmails.iterator();
        while (iter.hasNext()) {
            result.append(iter.next());
            if (iter.hasNext()) {
                result.append(",");
            }
        }
        logger.debug("splitted emails: {} ", result.toString());
        return result.toString();
    }

    /**
     * Единый метод для загрузки почтовых настроек
     * @param mailConfig
     */
    public static void loadMailConfig(Properties mailConfig){
        FileInputStream propertiesFile = null;
        try {
            propertiesFile = new FileInputStream(TimeSheetConstans.PROPERTY_PATH);
        } catch (FileNotFoundException e1) {
            logger.error("File timesheet.properties not found.");
        }
        try {
            mailConfig.load(propertiesFile);
        } catch (InvalidPropertiesFormatException e) {
            logger.error("Invalid timesheet.properties file format.");
        } catch (IOException e) {
            logger.error("Input-output error.");
        }
    }

}
