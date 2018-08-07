package cc.souco.toolbox.db.vo;

import java.util.List;

public class Table {
    private String name;  // 表名
    private String schema;
    private String type;  // 类型
    private String remarks;  // 表注释
    private String primaryKey;  // 主键
    private Integer rowCount;  // 行数
    private List<TableColumn> columns;  // 表的列属性

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

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
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
                ", primaryKey='" + primaryKey + '\'' +
                ", rowCount=" + rowCount +
                ", columns=" + columns +
                '}';
    }
}
