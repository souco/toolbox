package cc.souco.toolbox.db.vo;

import java.util.List;

public class Database {

    private String date;
    private String version;
    private List<String> schemas;
    private List<Table> tables;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSchemas() {
        return schemas.toString();
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }
}
