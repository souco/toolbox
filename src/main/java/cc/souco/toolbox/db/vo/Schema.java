package cc.souco.toolbox.db.vo;

import java.util.List;

public class Schema {

    private String name;
    private List<Table> tables;

    // 行数最多
    private List<Table> maxRow;

    // 行数最多
    private List<Table> emptyRow;

    // 行数最少
    private List<Table> minRow;

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

    public List<Table> getMaxRow() {
        return maxRow;
    }

    public void setMaxRow(List<Table> maxRow) {
        this.maxRow = maxRow;
    }

    public List<Table> getEmptyRow() {
        return emptyRow;
    }

    public void setEmptyRow(List<Table> emptyRow) {
        this.emptyRow = emptyRow;
    }

    public List<Table> getMinRow() {
        return minRow;
    }

    public void setMinRow(List<Table> minRow) {
        this.minRow = minRow;
    }
}
