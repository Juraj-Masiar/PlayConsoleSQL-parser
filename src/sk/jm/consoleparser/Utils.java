package sk.jm.consoleparser;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Juraj on 7.9.2014.
 */
public class Utils {
    private static DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss] ");

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method will return substring that is between 'strStart' and 'strEnd'.
     * Example:
     * intput = 'Hello *** my name is ###.'
     * strStart = '*** '
     * strEnd = ' is'
     * result will be: 'my name'
     * In case there is no matching string, '' will be returned. In case 'useFullLengthInCaseEndNotFound' is true
     * then the rest of the input string will be returned
     */
    public static String substring(String input, String strStart, String strEnd, Boolean useFullLengthInCaseEndNotFound) {
        int x = 0, z = 0, y = input.length();
        if (StringUtils.isNotEmpty(strStart))
            x = (z = input.indexOf(strStart)) + strStart.length();
        if (StringUtils.isNoneEmpty(strEnd))
            y = input.indexOf(strEnd, x);
        if (y < 0 && useFullLengthInCaseEndNotFound)
            y = input.length();
        return y < 0 || z < 0 ? "" : input.substring(x, y);
    }

    public static String substring(String input, String strStart, String strEnd) {
        return substring(input, strStart, strEnd, false);
    }

    public static String time() {
        return dateFormat.format(new Date());
    }

}
