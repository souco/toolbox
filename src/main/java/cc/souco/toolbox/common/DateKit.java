package cc.souco.toolbox.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateKit {

    public static final String DATE_TO_MILLI_FORMAT = "yyyyMMddHHmmss";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String dateToMilliStr() {
        return dateToMilliStr(null);
    }

    public static String dateToMilliStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TO_MILLI_FORMAT);
        return sdf.format(date == null ? new Date() : date);
    }

    public static String format() {
        return format(null, null);
    }

    public static String format(Date date) {
        return format(date, null);
    }

    public static String format(String pattern) {
        return format(null, pattern);
    }

    public static String format(Date date, String pattern) {
        if (date == null) {
            date = new Date();
        }
        if (pattern == null) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

	public static void main(String[] args) {

	}
}