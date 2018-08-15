package cc.souco.toolbox.db;

import cc.souco.toolbox.db.vo.Database;
import cc.souco.toolbox.db.vo.Table;
import cc.souco.toolbox.db.vo.TableColumn;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DbUtil {
    private final static Logger logger = LoggerFactory.getLogger(DbUtil.class);
    private static final String BOOLEAN_YES = "yes";
    private static final String TYPE_TABLE = "TABLE";
    private static final int TABLE_ENUM_TABLE_ROW_COUNT = 30;
    private static final int COLUMN_ENUM_TABLE_ROW_COUNT = 20;
    private static final int ENUMERATE_VALUE_LENGTH = 200;
    private static final boolean IS_TABLE_COUNT = false;
    private static final int TABLE_COUNT = 20;
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
        return listTables(schemaName, "%%");
    }

    public List<Table> listTables(String schemaName, String tablePattern) {
        List<Table> tables = Lists.newArrayList();
        try {
            int count = 0;
            ResultSet tableRs = dbMetaData.getTables(null, schemaName, tablePattern, new String[]{TYPE_TABLE});
            while (tableRs.next()) {
                String tableName = tableRs.getString("TABLE_NAME");  // 表名

                // 排除系统表
                if (tableName.contains("BIN$") || tableName.contains("SESSION")) {
                    continue;
                }

                String tableType = tableRs.getString("TABLE_TYPE");  // 表类型
                String tableRemarks = tableRs.getString("REMARKS");
                Table table = new Table(tableName, tableType, tableRemarks);

                table.setSchema(schemaName);
                tables.add(table);
                if (IS_TABLE_COUNT && ++count >= TABLE_COUNT) {
                    break;
                }
            }
            tableRs.close();

            // 获取表的详细信息
            for (Table table : tables) {
                getTableDetail(table);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }

    /**
     * 获取表详细信息
     * @param table 表对象
     * @throws SQLException
     */
    private void getTableDetail(Table table) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;// 获取主键信息
        Map<Integer, String> pkMap = Maps.newHashMap();
        ResultSet primaryKeyRs = dbMetaData.getPrimaryKeys(null, table.getSchema(), table.getName());
        while (primaryKeyRs.next()) {
            String column_name = primaryKeyRs.getString("COLUMN_NAME");
            Integer keyIndex = primaryKeyRs.getInt("KEY_SEQ");
            pkMap.put(keyIndex, column_name);
        }
        primaryKeyRs.close();

        StringBuilder pk = new StringBuilder(table.getName()).append("_PK");
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
        String rowCountSql = "select count(*) DATA_ROW_COUNT from \"" + table.getSchema() + "\".\"" + table.getName() + "\"";
        ps = con.prepareStatement(rowCountSql);
        System.out.println(rowCountSql);
        rs = ps.executeQuery();
        if (rs.next()) {
            table.setRowCount(rs.getInt("DATA_ROW_COUNT"));
        }
        rs.close();
        ps.close();

        // 获取表注释
        String tableCommentsSql = "select a.comments " +
                " from all_tab_comments a " +
                " left join all_tables b on b.table_name = a.table_name and b.owner = a.owner " +
                " where upper(a.table_type) = 'TABLE'" +
                " and upper(a.table_name) = upper('" + table.getName() + "')" +
                " and upper(a.owner) = upper('" + table.getSchema() + "')";
        ps = con.prepareStatement(tableCommentsSql);
        rs = ps.executeQuery();
        if (rs.next()) {
            table.setRemarks(rs.getString("COMMENTS"));
        }
        rs.close();
        ps.close();

        List<TableColumn> columns = Lists.newArrayList();
        ResultSet columnRs = dbMetaData.getColumns(null, table.getSchema(), table.getName(), null);
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
        columnRs.close();

        // 获取表字段的详细信息
        for(TableColumn column : columns){
            getTableColumnDetail(table, column);
        }

        table.setColumns(columns);
    }

    /**
     * 获取表字段的详细信息
     * @param table 表对象
     * @param tableColumn 表的列对象
     * @throws SQLException
     */
    private void getTableColumnDetail(Table table, TableColumn tableColumn) throws SQLException {
        String columnName = tableColumn.getName();
        String dataTypeName = tableColumn.getType();
        PreparedStatement ps;
        ResultSet rs;// 获取字段注释
        String columnCommentsSql = "select a.comments from all_col_comments a " +
                " left join all_tab_columns b on b.column_name = a.column_name and b.table_name = a.table_name " +
                " where upper(a.table_name) = upper('" + table.getName() + "')" +
                " and upper(a.column_name) = upper('" + columnName + "')" +
                " and upper(a.owner) = upper('" + table.getSchema() + "')";
        ps = con.prepareStatement(columnCommentsSql);
        rs = ps.executeQuery();
        if (rs.next()) {
            tableColumn.setRemarks(rs.getString("COMMENTS"));
        }
        rs.close();
        ps.close();

        // 是否可枚举
        if (null != table.getRowCount() && table.getRowCount() > TABLE_ENUM_TABLE_ROW_COUNT && ("VARCHAR2".equals(dataTypeName) || "NUMBER".equals(dataTypeName))) {
            // 行数
            String enumRowCountSql = "select count(distinct " + columnName.toUpperCase() + ") DATA_ROW_COUNT from \"" + table.getSchema() + "\".\"" + table.getName() + "\"";
            ps = con.prepareStatement(enumRowCountSql);
            rs = ps.executeQuery();
            if (rs.next()) {
                int colEnumRowCount  = rs.getInt("DATA_ROW_COUNT");
                tableColumn.setEnumerable(colEnumRowCount < COLUMN_ENUM_TABLE_ROW_COUNT);
            }
            rs.close();
            ps.close();
        }

        StringBuilder sb = new StringBuilder();
        if (tableColumn.getEnumerable()) {
            // 行数
            String enumRowCountSql = "select distinct " + columnName.toUpperCase() + " from \"" + table.getSchema() + "\".\"" + table.getName() + "\"";
            ps = con.prepareStatement(enumRowCountSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String value = rs.getString(columnName.toUpperCase());
                if (isSpecial(value)) {
                    sb = new StringBuilder();
                    break;
                } else {
                    sb.append(value).append(",");
                }
            }
            rs.close();
            ps.close();
        }
        if (sb.length() > 0) {
            String enumerateValue = sb.substring(0, sb.length() - 1);
            if (StringUtils.isBlank(enumerateValue) || enumerateValue.length() > ENUMERATE_VALUE_LENGTH) {
                tableColumn.setEnumerateValue("");
            } else {
                tableColumn.setEnumerateValue(transform(enumerateValue));
            }
        }
    }

    public List<Database> analyzeDatabase(List<String> schemas) {
        List<Database> databases = Lists.newArrayList();

        for (String schema : schemas) {
            Database database = new Database();

            // 获取数据库版本信息
            try {
                String databaseVersion = dbMetaData.getDatabaseProductVersion();
                database.setVersion(databaseVersion);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // 设置数据库 schema
            database.setSchema(schema);
            database.setTables(listTables(schema));

            databases.add(database);
        }

        return databases;
    }

    private String transform(String str){
        if(str.contains("<")||str.contains(">")||str.contains("&")){
            str=str.replaceAll("&", "&amp;");
            str=str.replaceAll("<", "&lt;");
            str=str.replaceAll(">", "&gt;");
        }
        return str;
    }

    /**
     * 目标字符串是否包含字符 \
     * @param value
     * @return
     */
    public static boolean isSpecial(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\\') {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        DbUtil dbUtil = new DbUtil();
        List<Table> db = dbUtil.listTables("SC_FGW", "C_INS_BUSINESS_INFO");
        System.out.println(JSONObject.toJSON(db));
    }
}