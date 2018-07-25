package cc.souco.toolbox.db;

import cc.souco.toolbox.db.vo.Table;
import cc.souco.toolbox.db.vo.TableColumn;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

public class DbUtil {
    private final static Logger logger = LoggerFactory.getLogger(DbUtil.class);

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

    public List<Table> listTables(String schemaName) {
        List tables = Lists.newArrayList();
        try {
            ResultSet tableRs = dbMetaData.getTables(null, schemaName, null, null);
            while (tableRs.next()) {
                String tableName = tableRs.getString("TABLE_NAME");  // 表名
                String tableType = tableRs.getString("TABLE_TYPE");  // 表类型
                String tableRemarks = tableRs.getString("REMARKS");
                Table table = new Table(tableName, tableType, tableRemarks);

                List<TableColumn> columns = Lists.newArrayList();
                ResultSet columnRs = dbMetaData.getColumns(null, schemaName, tableName, null);
                while (columnRs.next()) {
                    String columnName = columnRs.getString("COLUMN_NAME");//列名
                    String dataTypeName = columnRs.getString("TYPE_NAME");//java.sql.Types类型   名称
                    int columnSize = columnRs.getInt("COLUMN_SIZE");//列大小
                    String remarks = columnRs.getString("REMARKS");//列描述
                    String columnDef = columnRs.getString("COLUMN_DEF");//默认值
                    String isNullable = columnRs.getString("IS_NULLABLE");
                    boolean isNullAble = "yes".equalsIgnoreCase(isNullable);
                    TableColumn tableColumn = new TableColumn(columnName, dataTypeName, columnSize, isNullAble, remarks, columnDef);
                    columns.add(tableColumn);
                }
                table.setColumns(columns);
                tables.add(table);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }

    public static void main(String[] args) {
        DbUtil dbUtil = new DbUtil();
        List<Table> hr = dbUtil.listTables("JEEPLUS");
        // for (Table table : hr) {
            System.out.println(JSONObject.toJSON(hr));
        // }
    }
}