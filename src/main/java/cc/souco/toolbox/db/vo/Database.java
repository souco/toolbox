package cc.souco.toolbox.db.vo;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Database {

    private String version;
    private Date date = new Date();
    private List<Schema> schemas;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getDate() {
        return date;
    }

    public String getDateStr() {
        return DateFormatUtils.format(date, "yyyy-MM-dd hh:mm:ss");
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Schema> getSchemas() {
        if (schemas == null) {
            schemas = new ArrayList<>();
        }
        return schemas;
    }

    public String getSchemasStr(){
        String schema = "";
        for (Schema item : getSchemas()) {
            schema += item.getName() + ",";
        }
        return schema.length() > 0 ? schema.substring(0, schema.length() - 1) : schema;
    }

    public void setSchemas(List<Schema> schemas) {
        this.schemas = schemas;
    }
}
