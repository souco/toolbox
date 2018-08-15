package cc.souco.toolbox.common;

import org.apache.commons.lang3.StringUtils;

public class StringKit {

    /**
     * 目标字符串是否包含字符 \
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
                    || label == '\u0001'
                    || label == '\u0002'
                    || label == '\f' ) {
                return true;
            }
        }
        return false;
    }

	public static void main(String[] args) {

	}
}