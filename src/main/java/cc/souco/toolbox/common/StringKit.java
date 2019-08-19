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

    public static boolean containsSpecialChar(String value) {
        if (value != null && !"".equals(value)) {
            Pattern p = Pattern.compile("\t|\r|\n|\f|\b|/|&|<|>|\\\\");
            Matcher m = p.matcher(value);
            return m.find();
        }
        return false;
    }

    public static String clearSpecialStr(String value) {
        if (value != null && !"".equals(value)) {
            Pattern p = Pattern.compile("\t|\r|\n|\f|\b");
            Matcher m = p.matcher(value);
            String temp = m.replaceAll("");

            Pattern p2 = Pattern.compile("[/&<>\\\\]");
            Matcher m2 = p2.matcher(temp);
            return m2.replaceAll("");
        }
        return value;
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

    /**
     * 移除文本首尾的斜杠或反斜杠，并修正字符串中的斜杠或反斜杠为操作系统中的目录分隔符
     * @param content
     * @return
     */
    public static String trimAndCorrectSlash(String content) {
        String result = trimSlash(content);
        return correctSlash(result);
    }

    /**
     * 移除文本首尾的斜杠或反斜杠
     * @param content
     * @return
     */
    public static String trimSlash(String content) {
        String result = removeSlashAndBackslashPrefix(content);
        return removeSlashAndBackslashSuffix(result);
    }

    /**
     * 移除文本首部的斜杠或反斜杠
     * @param content
     * @return
     */
    public static String removeSlashAndBackslashPrefix(String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }

        if (content.startsWith("/") || content.startsWith("\\")) {
            content = content.substring(1);
        } else {
            return content;
        }
        return removeSlashAndBackslashPrefix(content);
    }

    /**
     * 移除文本末尾的斜杠或反斜杠
     * @param content
     * @return
     */
    public static String removeSlashAndBackslashSuffix(String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }

        if (content.endsWith("/")) {
            content = content.substring(0, content.lastIndexOf("/"));
        } else if(content.endsWith("\\")) {
            content = content.substring(0, content.lastIndexOf("\\"));
        } else {
            return content;
        }
        return removeSlashAndBackslashSuffix(content);
    }

    /**
     * 修正字符串中的斜杠或反斜杠为操作系统中的目录分隔符
     * @param content 文本
     * @return 修正后的文本
     */
    public static String correctSlash(String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }

        if ("/".equals(File.separator)) {
            return content.replaceAll("\\\\", "/");
        } else if ("\\".equals(File.separator)) {
            return content.replaceAll("/", "\\\\");
        }
        return content;
    }

    /**
     * 是否包含中文
     * @param value 字符串
     * @return 布尔值
     */
    public static boolean isContainChinese(String value) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(value);
        if (m.find()) {
            return true;
        }
        return false;
    }

	public static void main(String[] args) {

	}
}