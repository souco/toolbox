package cc.souco.toolbox.common;

import java.util.Calendar;
import java.util.Date;

public class DateKit {

    public static String dateToMilliStr() {
        return dateToMilliStr(null);
    }

    public static String dateToMilliStr(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date == null ? new Date() : date);
        return "" + calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DAY_OF_MONTH) + calendar.get(Calendar.HOUR_OF_DAY)
                + calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND);
    }

	public static void main(String[] args) {

	}
}