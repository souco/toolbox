package cc.souco.toolbox.db;

import cc.souco.toolbox.db.vo.Database;
import cc.souco.toolbox.db.vo.Table;
import cc.souco.toolbox.db.vo.TableColumn;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DbUtil {
    private final static Logger logger = LoggerFactory.getLogger(DbUtil.class);
    private static final String BOOLEAN_YES = "yes";
    private static final String TYPE_TABLE = "TABLE";

    private DatabaseMetaData dbMetaData = null;
    private Connection con = null;


    public DbUtil() {
        this.getDatabaseMetaData();
    }

    private void getDatabaseMetaData() {
        try {
            if (dbMetaData == null) {
                Class.forName(DbConfig.DRIVER);
                con = DriverManager.getConnection(DbConfig.URL, DbConfig.USERNAME, DbConfig.PASSWORD);
                dbMetaData = con.getMetaData();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Database listTables(String schemaName) {
        Database database = new Database();
        List tables = Lists.newArrayList();
        try {
            String databaseVersion = dbMetaData.getDatabaseProductVersion();
            database.setVersion(databaseVersion);

            ResultSet tableRs = dbMetaData.getTables(null, schemaName, "%%", new String[]{TYPE_TABLE});
            while (tableRs.next()) {
                String tableName = tableRs.getString("TABLE_NAME");  // 表名

                if (tableName.contains("BIN$")) {
                    continue;
                }

                String tableType = tableRs.getString("TABLE_TYPE");  // 表类型
                String tableRemarks = tableRs.getString("REMARKS");
                Table table = new Table(tableName, tableType, tableRemarks);

                // 获取主键信息
                Map<Integer, String> pkMap = Maps.newHashMap();
                ResultSet primaryKeyRs = dbMetaData.getPrimaryKeys(null, schemaName, tableName);
                while (primaryKeyRs.next()) {
                    String column_name = primaryKeyRs.getString("COLUMN_NAME");
                    Integer keyIndex = primaryKeyRs.getInt("KEY_SEQ");
                    pkMap.put(keyIndex, column_name);
                }
                StringBuilder pk = new StringBuilder(tableName).append("_PK");
                List<Map.Entry<Integer, String>> pkEntries = Lists.newArrayList(pkMap.entrySet());
                if (pkEntries.size() > 1) {
                    pkEntries.sort(Comparator.comparingInt(Map.Entry::getKey));
                    for (Map.Entry<Integer, String> entry : pkEntries) {
                        pk.append(".").append(entry.getValue());
                    }
                } else if (pkEntries.size() == 1) {
                    pk.append(".").append(pkEntries.get(0).getValue());
                }
                table.setPrimaryKey(pkEntries.isEmpty() ? "" : pk.toString());

                // 获取数据行数
                String rowCountSql = "select count(*) DATA_ROW_COUNT from " + tableName;
                PreparedStatement ps = con.prepareStatement(rowCountSql);
                System.out.println(rowCountSql);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    table.setRowCount(rs.getInt("DATA_ROW_COUNT"));
                }

                List<TableColumn> columns = Lists.newArrayList();
                ResultSet columnRs = dbMetaData.getColumns(null, schemaName, tableName, null);
                while (columnRs.next()) {
                    String columnName = columnRs.getString("COLUMN_NAME");//列名
                    String dataTypeName = columnRs.getString("TYPE_NAME");//java.sql.Types类型   名称
                    int columnSize = columnRs.getInt("COLUMN_SIZE");//列大小
                    String remarks = columnRs.getString("REMARKS");//列描述
                    String columnDef = columnRs.getString("COLUMN_DEF");//默认值
                    String isNullable = columnRs.getString("IS_NULLABLE");
                    boolean isNullAble = BOOLEAN_YES.equalsIgnoreCase(isNullable);
                    TableColumn tableColumn = new TableColumn(columnName, dataTypeName, columnSize, isNullAble, remarks, columnDef);
                    columns.add(tableColumn);
                }
                table.setColumns(columns);
                tables.add(table);
            }
            database.setTables(tables);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return database;
    }

    public static void main(String[] args) {
        DbUtil dbUtil = new DbUtil();
        Database db = dbUtil.listTables("JFINAL");
        System.out.println(JSONObject.toJSON(db));
    }
}