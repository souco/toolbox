package cc.souco.toolbox.common;

import java.util.List;

public class ListKit {

    /**
     * 取列表前半部分
     * @param objects 列表
     * @return objects 的前半部分列表
     */
    public static <E> List<E> preHalf(List<E> objects) {
        if (objects.size() > 2) {
            return objects.subList(0, objects.size() / 2);
        }
        return objects;
    }

    /**
     * 取列表后半部分
     * @param objects 列表
     * @return objects 的后半部分列表
     */
    public static <E> List<E> suffixHalf(List<E> objects) {
        if (objects.size() > 2) {
            return objects.subList(objects.size() / 2, objects.size());
        }
        return objects;
    }


    public static String join(Iterable<String> objects, String label) {
        if (null == objects) {
            return null;
        } else if (null == label) {
            label = "";
        }

        StringBuffer sb = new StringBuffer();
        for (String value : objects) {
            sb.append(value).append(label);
        }

        return sb.length() == 0 ? "" : sb.substring(0, sb.length() - label.length());
    }

	public static void main(String[] args) {

	}
}