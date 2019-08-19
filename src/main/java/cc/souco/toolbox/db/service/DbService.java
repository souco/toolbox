package cc.souco.toolbox.db.service;

import cc.souco.toolbox.db.vo.Database;
import cc.souco.toolbox.db.vo.code.DbDocTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class DbService {

    @Value("${jdbc.type}")
    private String jdbcType;

    @Autowired
    private DbOracleDialect oracleDialect;
    @Autowired
    private DbMysqlDialect mysqlDialect;


    private DbDialect getService() {
        switch (jdbcType.toLowerCase()) {
            case "oracle":
                return oracleDialect;
            case "mysql":
                return mysqlDialect;
            default:
                return oracleDialect;
        }
    }

    public boolean testDbConnect() throws SQLException {
        return getService().testDbConnect();
    }

    public Database buildDatabase(List<String> schemas, DbDocTemplate docTemplate) {
        return getService().buildDatabase(schemas, docTemplate);
    }

    public List<String> findSchemas() throws SQLException {
        return getService().findSchemas();
    }

}
