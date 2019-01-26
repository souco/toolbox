package cc.souco.toolbox.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateKit {

    private static final String DATE_TO_MILLI_FORMAT = "yyyyMMddHHmmss";

    public static String dateToMilliStr() {
        return dateToMilliStr(null);
    }

    public static String dateToMilliStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TO_MILLI_FORMAT);
        return sdf.format(date == null ? new Date() : date);
    }

	public static void main(String[] args) {

	}
}