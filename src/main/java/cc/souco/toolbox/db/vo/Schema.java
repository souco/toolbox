package cc.souco.toolbox.db.vo;

import com.beust.jcommander.internal.Lists;

import java.util.List;

public class Schema {

    private String name;
    private List<Table> tables;
    private List<String> views;
    private List<String> synonyms;

    // 行数有序表
    private List<Table> orderRowTables;

    // 行数最多
    private List<Table> maxRowTables;

    // 空表
    private List<Table> emptyRowTables;

    // 行数最少
    private List<Table> minRowTables;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public List<String> getViews() {
        if (views == null) {
            views = Lists.newArrayList();
        }
        return views;
    }

    public void setViews(List<String> views) {
        this.views = views;
    }

    public List<String> getSynonyms() {
        if (synonyms == null) {
            synonyms = Lists.newArrayList();
        }
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public List<Table> getOrderRowTables() {
        return orderRowTables;
    }

    public void setOrderRowTables(List<Table> orderRowTables) {
        this.orderRowTables = orderRowTables;
    }

    public List<Table> getMaxRowTables() {
        return maxRowTables;
    }

    public void setMaxRowTables(List<Table> maxRowTables) {
        this.maxRowTables = maxRowTables;
    }

    public List<Table> getEmptyRowTables() {
        return emptyRowTables;
    }

    public void setEmptyRowTables(List<Table> emptyRowTables) {
        this.emptyRowTables = emptyRowTables;
    }

    public List<Table> getMinRowTables() {
        return minRowTables;
    }

    public void setMinRowTables(List<Table> minRowTables) {
        this.minRowTables = minRowTables;
    }
}
