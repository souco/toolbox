package cc.souco.toolbox.db.service;

import cc.souco.toolbox.common.StringKit;
import cc.souco.toolbox.db.vo.Column;
import cc.souco.toolbox.db.vo.Database;
import cc.souco.toolbox.db.vo.Schema;
import cc.souco.toolbox.db.vo.Table;
import cc.souco.toolbox.db.vo.code.ColumnType;
import cc.souco.toolbox.db.vo.code.DbDocTemplate;
import com.beust.jcommander.internal.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Service
public class DbDialect {
    private static final Logger logger = LoggerFactory.getLogger(DbDialect.class);
    public static final String TYPE_TABLE = "TABLE";
    public static final String TYPE_VIEW = "VIEW";
    public static final String TYPE_SYNONYM = "SYNONYM";
    public static final String BOOLEAN_YES = "yes";

    @Value("${jdbc.driver-class-name}")
    private String jdbcDriverClassName;
    @Value("${jdbc.url}")
    private String jdbcUrl;
    @Value("${jdbc.username}")
    private String jdbcUsername;
    @Value("${jdbc.password}")
    private String jdbcPassword;

    @Value("${db.enumerableMinRowCount}")
    protected Integer enumerableMinRowCount;
    @Value("${db.enumerableValueMaxCount}")
    protected Integer enumerableValueMaxCount;
    @Value("${db.enumerableValueTotalCharCount}")
    protected Integer enumerableValueTotalCharCount;
    @Value("${table.include}")
    protected String tableInclude;
    @Value("${table.exclude}")
    protected String tableExclude;

    private DatabaseMetaData dbMetaData = null;
    private Connection con = null;

    protected Connection getConnection() {
        if (con == null) {
            try {
                Class.forName(jdbcDriverClassName);
                con = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
            } catch (Exception e) {
                logger.error("数据库连接失败！", e);
                throw new RuntimeException("数据库连接失败！", e);
            }
        }
        return con;
    }


    protected DatabaseMetaData getDatabaseMetaData() {
        if (dbMetaData == null) {
            try {
                dbMetaData = getConnection().getMetaData();
            } catch (SQLException e) {
                logger.error("数据库连接失败！", e);
                throw new RuntimeException("数据库连接失败！", e);
            }
        }
        return dbMetaData;
    }

    protected boolean testDbConnect() throws SQLException {
        return true;
    }

    /**
     * 获取数据库的对象
     *
     * @return 对象列表
     */
    public List<String> findSchemas() throws SQLException {
        List<String> schemas = Lists.newArrayList();
        ResultSet rs = getDatabaseMetaData().getSchemas();
        while (rs.next()) {
            String schema = rs.getString("TABLE_SCHEM");  // 表名
            schemas.add(schema);
        }
        rs.close();
        return schemas;
    }

    public Database buildDatabase(List<String> schemas, DbDocTemplate docTemplate) {
        Database database = new Database();

        String databaseVersion = null;
        try {
            databaseVersion = getDatabaseMetaData().getDatabaseProductVersion();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        database.setVersion(databaseVersion);

        for (String schemaStr : schemas) {
            database.getSchemas().add(buildSchema(docTemplate, schemaStr));
        }
        return database;
    }

    protected Schema buildSchema(DbDocTemplate docTemplate, String schemaName) {
        logger.info("build schema " + schemaName);
        List<Table> tables = null;
        List<String> views = Lists.newArrayList();
        Schema schema = new Schema();
        // 获取数据库版本信息
        try {
            tables = buildTables(schemaName, docTemplate);
            if (docTemplate.getShowViews()) {
                views = buildViews(schemaName);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        schema.setTables(tables);
        schema.setName(schemaName);
        schema.setViews(views);
        return schema;
    }

    protected List<Table> buildTables(String schemaName, DbDocTemplate docTemplate) throws SQLException {
        logger.info("build table base info", schemaName);
        List<Table> tables = Lists.newArrayList();
        try {
            ResultSet tableRs = getDatabaseMetaData().getTables(getConnection().getCatalog(), schemaName, "%%", new String[]{TYPE_TABLE});

            List<String> includeTables = Lists.newArrayList();
            List<String> excludeTables = Lists.newArrayList();
            if (StringUtils.isNotBlank(tableInclude)) {
                includeTables.addAll(Arrays.asList(tableInclude.split(",")));
            } else if (StringUtils.isNotBlank(tableExclude)) {
                excludeTables.addAll(Arrays.asList(tableExclude.split(",")));
            }

            while (tableRs.next()) {
                String tableName = tableRs.getString("TABLE_NAME");  // 表名

                // 排除系统表
                if (tableName.contains("BIN$") || tableName.contains("SESSION")) {
                    continue;
                } else if (!includeTables.isEmpty()) {
                    // include 过滤
                    if (!includeTables.contains(tableName.toLowerCase())) {
                        continue;
                    }
                } else if (!excludeTables.isEmpty()) {
                    // exclude 过滤
                    if (excludeTables.contains(tableName.toLowerCase())) {
                        continue;
                    }
                }

                String tableType = tableRs.getString("TABLE_TYPE");  // 表类型
                String tableRemarks = tableRs.getString("REMARKS");
                Table table = new Table(tableName, tableType, tableRemarks);

                table.setSchema(schemaName);
                tables.add(table);
            }
            tableRs.close();

            // 获取表的详细信息
            for (Table table : tables) {
                buildTableDetail(table);
            }
        } catch (SQLException e) {
            logger.error("", e);
        }

        return tables;
    }

    protected List<String> buildViews(String schemaName) throws SQLException {
        logger.info("build views", schemaName);
        List<String> views = Lists.newArrayList();
        ResultSet tableRs = getDatabaseMetaData().getTables(getConnection().getCatalog(), schemaName, "%%", new String[]{TYPE_VIEW});
        while (tableRs.next()) {
            String tableName = tableRs.getString("TABLE_NAME");  // 表名

            // 排除系统
            if (tableName.contains("BIN$") || tableName.contains("SESSION")) {
                continue;
            }
            views.add(tableName);
        }
        tableRs.close();
        return views;
    }

    /**
     * 获取表详细信息
     *
     * @param table 表对象
     * @throws SQLException
     */
    private void buildTableDetail(Table table) throws SQLException {
        List<Column> columns = Lists.newArrayList();
        ResultSet columnRs = getDatabaseMetaData().getColumns(getConnection().getCatalog(), table.getSchema(), table.getName(), "%%");
        while (columnRs.next()) {
            String columnName = columnRs.getString("COLUMN_NAME");
            String dataTypeName = columnRs.getString("TYPE_NAME");
            int columnSize = columnRs.getInt("COLUMN_SIZE");  // 列大小
            if (StringUtils.isNotBlank(dataTypeName) && dataTypeName.startsWith(ColumnType.TS.toString())) {
                if (dataTypeName.contains("(") && dataTypeName.contains(")")) {
                    String size = dataTypeName.substring(dataTypeName.indexOf("(") + 1, dataTypeName.indexOf(")"));
                    columnSize = NumberUtils.toInt(size, columnSize);
                }
                dataTypeName = ColumnType.TS.name();
            }
            String remarks = StringKit.clearSpecialStr(columnRs.getString("REMARKS"));  // 列描述
            if (StringUtils.isNotBlank(remarks) && remarks.contains("?")) {
                remarks = "";
            }
            String columnDef = columnRs.getString("COLUMN_DEF");  // 默认值
            String isNullable = columnRs.getString("IS_NULLABLE");
            boolean isNullAble = BOOLEAN_YES.equalsIgnoreCase(isNullable);
            Column tableColumn = new Column(columnName, ColumnType.toName(dataTypeName), columnSize, isNullAble, remarks, columnDef);
            columns.add(tableColumn);
        }
        columnRs.close();
        table.setColumns(columns);
    }

    /**
     * 是否枚举类型的表字段类型
     * 目前仅对数字和字符类型的字段做枚举值读取。主要判断 number,varchar 及其衍生类型
     * @param dataTypeName 类型名称
     * @return boolean 是/否
     */
    protected boolean isEnumColumnType(String dataTypeName){
        ColumnType columnType;
        try {
            columnType = ColumnType.valueOf(dataTypeName);
        } catch (IllegalArgumentException e) {
            // 不在枚举类型范围内，不统计枚举值信息
            return false;
        }

        switch (columnType){
            case N: return true;
            case INT: return true;
            case VC: return true;
            case VC2: return true;
            case NVC: return true;
            case NVC2: return true;
            case LVC: return true;
            case LNVC: return true;
            default: return false;
        }
    }
}
