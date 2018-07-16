package cc.souco.toolbox.log;

import org.springframework.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogAnalyzeUtil {
    public static final String LOG_FILE_PATH = "D:\\Logs\\xj";
    // public static final String LOG_IP_REGEX = "ip\[";
    public static final Pattern ipRegEx = Pattern.compile("ip\\[\\S+\\]");
    public static final Pattern contentTypeRegEx = Pattern.compile("contentType\\[\\S+\\]");

    public static void main(String[] args) {
        String fileName = "sp.log_2018-07-15.log";

        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(LOG_FILE_PATH + File.separator + fileName)), "UTF-8"));

            String line = null;
            HashMap<String, Integer> ipCount = new HashMap<>();
            HashMap<String, Integer> contentCount = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                if (!StringUtils.isEmpty(line) && line.contains("[INFO]")) {
                    Matcher ipMatcher = ipRegEx.matcher(line);
                    Matcher contentMatcher = contentTypeRegEx.matcher(line);
                    if (ipMatcher.find()) {
                        String ip = ipMatcher.group().replace("ip[", "").replace("]", "");
                        Integer count = ipCount.getOrDefault(ip, 0);
                        ipCount.put(ip, ++count);
                    }
                    if (contentMatcher.find()) {
                        String contentType = contentMatcher.group().replace("contentType[", "").replace("]", "");
                        Integer count = contentCount.getOrDefault(contentType, 0);
                        contentCount.put(contentType, ++count);
                    }
                }
            }

            Set<Map.Entry<String, Integer>> entries = ipCount.entrySet();
            for (Map.Entry<String, Integer> entry : entries) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }

            Set<Map.Entry<String, Integer>> contentTypes = contentCount.entrySet();
            for (Map.Entry<String, Integer> entry : contentTypes) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
