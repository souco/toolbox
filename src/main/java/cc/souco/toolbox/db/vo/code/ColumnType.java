package cc.souco.toolbox.db.vo.code;

import java.util.HashMap;

/**
 * 对部分常用或较长的字段类型进行转换
 */
public enum ColumnType {
    N("NUMBER"),
    TS("TIMESTAMP"),
    INT("INTEGER"),
    VC("VARCHAR"),
    VC2("VARCHAR2"),
    NVC("NVARCHAR"),
    NVC2("NVARCHAR2"),
    LVC("LONGVARCHAR"),
    LVB("LONGVARBINARY"),
    TWT("TIME_WITH_TIMEZONE"),
    LNVC("LONGNVARCHAR"),
    TSWT("TIMESTAMP_WITH_TIMEZONE");

    // 成员变量
    private String fullName;

    private static HashMap<String, ColumnType> sortFullMap = new HashMap<>(12);
    static {
        for (ColumnType type : ColumnType.values()) {
            sortFullMap.put(type.fullName, type);
        }
    }

    public static ColumnType fromFullName(String fullName) {
        return sortFullMap.get(fullName);
    }

    public static String toName(String name) {
        if (sortFullMap.containsKey(name)) {
            return sortFullMap.get(name).name();
        }
        return name;
    }

    // 构造方法
    ColumnType(String fullName){
        this.fullName = fullName;
    }
}
