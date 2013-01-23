package com.aplana.timesheet.util.report;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Report7Period implements Comparable {
    public static Integer PERIOD_TYPE_MONTH = 1;
    public static Integer PERIOD_TYPE_KVARTAL = 3;
    public static Integer PERIOD_TYPE_HALF_YEAR = 6;
    public static Integer PERIOD_TYPE_YEAR = 12;
    private Date start;
    private Date end;
    private Integer type;
    private Integer number;
    private String name;

    public Report7Period(Integer number, Date start, Date end, Integer type) throws Exception {
        this.start = start;
        this.end = end;
        this.type = type;
        this.number = number;
        this.name = this.generateName();
    }

    public Report7Period(String name) {
        this.name = name;
        this.number = Integer.MAX_VALUE;
    }

    public String getName() throws Exception {
        if (this.name == null) {
            setName(generateName());
        }
        return this.name;
    }

    @Override
    public String toString() {
        try {
            return getName();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return super.toString();
    }

    private void setName(String name) {
        this.name = name;
    }

    private String generateName() throws Exception {
        if (type.equals(PERIOD_TYPE_MONTH)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM.YYYY");
            return sdf.format(this.start);
        } else if (type.equals(PERIOD_TYPE_KVARTAL)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM");
            Integer number = new Integer(sdf.format(this.start));
            SimpleDateFormat sdf2 = new SimpleDateFormat("YYYY");
            if (number > 0 && number < 4) {
                return "1-ый квартал " + sdf2.format(this.start);
            } else if (number > 2 && number < 7) {
                return "2-ой квартал" + sdf2.format(this.start);
            } else if (number > 5 && number < 8) {
                return "3-ий квартал " + sdf2.format(this.start);
            } else if (number > 7) {
                return "4-ый квартал " + sdf2.format(this.start);
            }
        } else if (type.equals(PERIOD_TYPE_HALF_YEAR)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM");
            SimpleDateFormat sdf2 = new SimpleDateFormat("YYYY");
            Integer number = new Integer(sdf.format(this.start));
            if (number > 0 && number < 7) {
                return "1-ый квартал " + sdf2.format(this.start);
            } else {
                return "2-ой квартал" + sdf2.format(this.start);
            }
        } else if (type.equals(PERIOD_TYPE_YEAR)) {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY");
            return sdf.format(this.start) + "г.";
        }
        throw new Exception("Can't get period name not valid type");
    }

    public Integer getNumber() {
        return this.number;
    }

    public int compareTo(Object value) {
        Report7Period o = (Report7Period)value;
        if (o.getNumber().equals(this.getNumber())) {
            return 0;
        } else if(o.getNumber() > this.getNumber()) {
            return 1;
        } else {
            return -1;
        }
    }

}
