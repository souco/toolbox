package cc.souco.toolbox.common;


import cc.souco.toolbox.common.sub.EqualComparator;
import cc.souco.toolbox.common.sub.GroupKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CollectionKit {

    /**
     * list 集合去重
     * @param list 需要去重的集合
     * @param c 比较T对象是否相同的方法
     * @param <T> T对象列表
     */
    public static <T> void removeDuplicates(List<T> list, EqualComparator<? super T> c) {
        // 如果其中一个集合为空，则返回另一集合
        if (list == null || list.size() < 2) {
            return;
        }

        for (int rightIndex = list.size() - 1; rightIndex >= 0; rightIndex--) {
            T rightItem = list.get(rightIndex);
            for (int leftIndex = 0; leftIndex < rightIndex; leftIndex++) {
                T leftItem = list.get(leftIndex);
                if (c.equals(rightItem, leftItem)) {
                    list.remove(rightIndex);
                    break;
                }
            }
        }
    }

    /**
     * 求集合 list1 和 list2 的并集
     * @param list1 列表1
     * @param list2 列表2
     * @param c 比较两个对象是否相同的函数
     * @return 并集集合
     */
    public static <T> List<T> union(List<T> list1, List<T> list2, EqualComparator<? super T> c) {
        // 如果其中一个集合为空，则返回另一集合
        if (list1 == null || list1.isEmpty()) {
            return list2;
        } else if (list2 == null || list2.isEmpty()) {
            return list1;
        }

        for (T item : list1) {
            // 遍历 list2，剔除其中与 list1 相同的元素
            for (int index = 0; index < list2.size(); index++) {
                T cur = list2.get(index);
                if (c.equals(item, cur)) {
                    list2.remove(cur);
                    break;
                }
            }
        }
        list1.addAll(list2);
        return list1;
    }

    /**
     * 求多个集合的并集
     * @param c 比较函数
     * @param lists 求并集的列表
     * @return 并集
     */
    public static <T> List<T> union(EqualComparator<? super T> c, List<T>... lists) {
        // 如果其中一个集合为空，则返回另一集合
        List<T> result = new ArrayList<>();
        for (List<T> list : lists) {
            if (list.size() > 0) {
                // 从后往前遍历
                for (int index = list.size() - 1; index >= 0; index--) {
                    T cur = list.get(index);
                    for (T item : result) {
                        if (c.equals(item, cur)) {
                            list.remove(cur);
                            break;
                        }
                    }
                }
                result.addAll(list);
            }
        }
        return result;
    }

    /**
     * 求集合 list1 和 list2 的差集
     * @param list1 集合1
     * @param list2 集合2
     * @param c 比较方法，判断两个集合元素是否相同，方法返回 bool
     * @return 对象T的列表
     */
    public static <T> List<T> minus(List<T> list1, List<T> list2, EqualComparator<? super T> c) {
        // 如果其中一个集合为空，则返回另一集合
        if (list1 == null || list1.isEmpty()) {
            return new ArrayList<>();
        } else if (list2 == null || list2.isEmpty()) {
            return list1;
        }

        for (T item : list2) {
            // 遍历 list1，剔除其中与 list2 相同的元素
            for (int index = 0; index < list1.size(); index++) {
                T cur = list1.get(index);
                if (c.equals(item, cur)) {
                    list1.remove(cur);
                    break;
                }
            }
        }
        return list1;
    }

    /**
     * 求集合 list 按 key 进行分组
     * @param list 集合
     * @param k 获取 key 值的方法
     * @return Map<key的value, listItem>
     */
    public static <T> Map<Object, List<T>> group(List<T> list, GroupKey<? super T> k) {
        // 如果其中一个集合为空，则返回另一集合
        if (list == null || list.isEmpty()) {
            return new HashMap<>();
        }

        HashMap<Object, List<T>> result = new LinkedHashMap<>();
        for (T item : list) {
            Object key = k.key(item);
            if (!result.containsKey(key)) {
                ArrayList<T> items = new ArrayList<>();
                items.add(item);
                result.put(key, items);
            } else {
                result.get(key).add(item);
            }
        }
        return result;
    }
}
