package com.aplana.timesheet.util;

/**
 *
 * @author aimamutdinov
 */
public class TimeSheetConstans {
	/**
	 * путь к property файлу
	 */
	public static String PROPERTY_PATH = "./webapps/timesheet.properties";
    /**
     * Имя куки для отоброжения всех пользователей в том числе и уволенных
     */
    public static String COOKIE_SHOW_ALLUSER = "SHOW_ALLUSER";
    /**
     * Проектная деятельность
     */
    public static Integer DETAIL_TYPE_PROJECT = 12;
    /**
     * Пресейловая деятельность
     */
    public static Integer DETAIL_TYPE_PRESALE = 13;
    /**
     * Вне проектная деятельность
     */
    public static Integer DETAIL_TYPE_OUTPROJECT = 14;
    
    /**
     * Куки которая ставится для того чтобы запомнить человека
     */
    public static String COOKIE_REMEMBER = "REMEMBER";
    public static String POST_REMEMBER = "remember";
}
