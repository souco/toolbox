package cc.souco.toolbox.db.vo;

import java.util.List;

public class Table {
    private String name;
    private String type;
    private String remarks;
    private Integer rowCount;
    private List<TableColumn> columns;

    public Table() {
    }

    public Table(String name, String type, String remarks, Integer rowCount) {
        this.name = name;
        this.type = type;
        this.remarks = remarks;
        this.rowCount = rowCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<TableColumn> columns) {
        this.columns = columns;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", remarks='" + remarks + '\'' +
                ", rowCount='" + rowCount + '\'' +
                ", columns=" + columns +
                '}';
    }
}
