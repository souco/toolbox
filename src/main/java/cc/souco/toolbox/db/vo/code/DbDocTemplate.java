package cc.souco.toolbox.db.vo.code;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

/**
 * 对部分常用或较长的字段类型进行转换
 */
public enum DbDocTemplate {
    SIMPLE("经典", null, "databaseTemplateSimpleSingle.ftl", false, false, false, false, false)
    , BASIC("详细", "databaseTemplateBasic.ftl", "databaseTemplateBasicSingle.ftl", true, true, true, true, true)
    ;

    // 成员变量
    private String abbr;  // 中文简称
    private String filename;  // 模板文件名
    private String single;  // 单实例模板文件名
    private Boolean showPK;
    private Boolean showRowCount;
    private Boolean showEnumerateValue;
    private Boolean showViews;
    private Boolean showSynonyms;

    DbDocTemplate(String abbr, String filename, String single, Boolean showPk, Boolean showRowCount, Boolean showEnumerateValue, Boolean showViews, Boolean showSynonyms) {
        this.abbr = abbr;
        this.filename = filename;
        this.single = single;
        this.showPK = showPk;
        this.showRowCount = showRowCount;
        this.showEnumerateValue = showEnumerateValue;
        this.showViews = showViews;
        this.showSynonyms = showSynonyms;
    }

    /**
     * 按索引获取
     * @param code
     * @return
     */
    public static DbDocTemplate fromCode(Integer code) {
        DbDocTemplate[] values = DbDocTemplate.values();

        if (code == null || code >= values.length || code < 0) {
            return null;
        }
        return values[code];
    }

    /**
     * 按名称获取
     * @param name
     * @return
     */
    public static DbDocTemplate fromName(String name) {
        DbDocTemplate[] values = DbDocTemplate.values();

        // fail fast
        if (StringUtils.isBlank(name)) {
            return null;
        }

        for (DbDocTemplate value : values) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 获取索引
     * @return
     */
    public Integer toCode() {
        return this.ordinal();
    }

    /**
     * 获取模板文件名
     * @return
     */
    public String filename() {
        return this.filename;
    }

    /**
     * 获取单实例版模板文件名
     * @return
     */
    public String singleFilename() {
        if (StringUtils.isNotBlank(this.single)) {
            return single;
        }
        return filename;
    }

    @JSONField
    public String getAbbr() {
        return abbr;
    }

    @JSONField
    public String getFilename() {
        return filename;
    }

    @JSONField
    public String getSingle() {
        return single;
    }

    public Boolean getShowPK() {
        return showPK;
    }

    public Boolean getShowRowCount() {
        return showRowCount;
    }

    public Boolean getShowEnumerateValue() {
        return showEnumerateValue;
    }

    public Boolean getShowViews() {
        return showViews;
    }

    public Boolean getShowSynonyms() {
        return showSynonyms;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("name", name());
        object.put("abbr", abbr);
        object.put("filename", filename);
        object.put("single", single);
        return object.toString();
    }

    /**
     *将该枚举全部转化成json
     * @return
     */
    public static String toJson(){
        JSONArray jsonArray = new JSONArray();
        for (DbDocTemplate e : DbDocTemplate.values()) {
            JSONObject object = new JSONObject();
            object.put("name", e.name());
            object.put("abbr", e.getAbbr());
            object.put("filename", e.getFilename());
            object.put("single;", e.getSingle());
            jsonArray.add(object);
        }
        return jsonArray.toString();
    }
}

