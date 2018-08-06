package cc.souco.toolbox.db.vo;

import java.util.List;

public class Database {

    private String version;
    private List<Table> tables;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }
}
