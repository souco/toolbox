package cc.souco.toolbox.db.vo;

import java.util.List;

public class Table {
    private String name;
    private String type;
    private String remarks;
    private List<TableColumn> columns;

    public Table() {
    }

    public Table(String name, String type, String remarks) {
        this.name = name;
        this.type = type;
        this.remarks = remarks;
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

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", remarks='" + remarks + '\'' +
                ", columns=" + columns +
                '}';
    }
}
