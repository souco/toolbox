package cc.souco.toolbox.common;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringKit {

    /**
     * 目标字符串是否包含字符 \ < > &  /
     * @param value
     * @return
     */
    public static boolean isSpecialString(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char label = value.charAt(i);
            if (label == '\\'
                    || label ==  '<'
                    || label ==  '>'
                    || label ==  '&'
                    || label == '/'
                    || label == '\r'
                    || label == '\t'
                    // || label == '\u0001'
                    // || label == '\u0002'
                    || label == '\f' ) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取指定字符串出现的次数
     *
     * @param srcText 源字符串
     * @param findText 要查找的字符串
     * @return
     */
    public static int appearCount(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }

    /**
     * 遍历集合，转化为字符串
     * @param collection 集合
     * @return 字符串
     */
    public static String toString(Collection collection) {
        if (null == collection || collection.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) {
            sb.append(obj.toString()).append("\r\n");
        }
        return sb.toString();
    }

    public static String removeSlashAndBackslashPrefix(String content) {
        if (content.startsWith("/") || content.startsWith("\\")) {
            content = content.substring(1);
        } else {
            return content;
        }
        return removeSlashAndBackslashPrefix(content);
    }

    public static String removeSlashAndBackslashSuffix(String content) {
        if (content.endsWith("/")) {
            content = content.substring(0, content.lastIndexOf("/"));
        } else if(content.endsWith("\\")) {
            content = content.substring(0, content.lastIndexOf("\\"));
        } else {
            return content;
        }
        return removeSlashAndBackslashSuffix(content);
    }

    public static String correctSlash(String content) {
        if ("/".equals(File.separator)) {
            return content.replaceAll("\\\\", "/");
        } else if ("\\\\".equals(File.separator)) {
            return content.replaceAll("/", "\\\\");
        }
        return content;
    }

	public static void main(String[] args) {

	}
}