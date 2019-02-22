package cc.souco.toolbox.db;

import cc.souco.toolbox.common.StringKit;
import cc.souco.toolbox.common.SysConfig;
import cc.souco.toolbox.db.vo.Column;
import cc.souco.toolbox.db.vo.Database;
import cc.souco.toolbox.db.vo.Schema;
import cc.souco.toolbox.db.vo.Table;
import cc.souco.toolbox.db.vo.code.ColumnType;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// @Service
public class DbAnalyze {
    private final static Logger logger = LoggerFactory.getLogger(DbAnalyze.class);
    private static final String BOOLEAN_YES = "yes";
    private static final String TYPE_TABLE = "TABLE";
    private static final int TABLE_ENUM_TABLE_ROW_COUNT = 30;
    private static final int COLUMN_ENUM_TABLE_ROW_COUNT = 20;
    private static final int ENUMERATE_VALUE_LENGTH = 200;
    private static final boolean IS_TABLE_COUNT = false;
    private static final boolean IS_CACL_ENUM = false;
    private static final int TABLE_COUNT = 15;
    private DatabaseMetaData dbMetaData = null;
    private Connection con = null;


    // @Autowired
    // private DbDao dbDao;


    public DbAnalyze() {
        this.getDatabaseMetaData();
    }

    private void getDatabaseMetaData() {
        try {
            if (dbMetaData == null) {
                Class.forName(SysConfig.DRIVER);
                con = DriverManager.getConnection(SysConfig.URL, SysConfig.USERNAME, SysConfig.PASSWORD);
                dbMetaData = con.getMetaData();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Table> listTables(String schemaName, String tablePattern) {
        return listTables(schemaName, tablePattern, Lists.newArrayList(), Lists.newArrayList());
    }


    public List<Table> listTables(String schemaName, List<String> excludes, List<String> excluded) {
        return listTables(schemaName, "%%", excludes , excluded);
    }

    public List<Table> listTables(String schemaName, String tablePattern, List<String> excludes, List<String> excluded) {
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

                // 记录实际排除的表
                // logger.info("excludes contains " + tableName + "? " + excludes.contains(tableName));
                if (excludes != null && excludes.contains(tableName)) {
                    excluded.add(tableName);
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

        List<Column> columns = Lists.newArrayList();
        ResultSet columnRs = dbMetaData.getColumns(null, table.getSchema(), table.getName(), null);
        while (columnRs.next()) {
            String columnName = columnRs.getString("COLUMN_NAME");//列名
            String dataTypeName = columnRs.getString("TYPE_NAME");//java.sql.Types类型   名称
            int columnSize = columnRs.getInt("COLUMN_SIZE");//列大小
            if (StringUtils.isNotBlank(dataTypeName) && dataTypeName.startsWith(ColumnType.TS.toString())) {
                if (dataTypeName.contains("(") && dataTypeName.contains(")")) {
                    String size = dataTypeName.substring(dataTypeName.indexOf("(") + 1, dataTypeName.indexOf(")"));
                    columnSize = NumberUtils.toInt(size, columnSize);
                }
                dataTypeName = ColumnType.TS.name();
            }
            String remarks = columnRs.getString("REMARKS");//列描述
            String columnDef = columnRs.getString("COLUMN_DEF");//默认值
            String isNullable = columnRs.getString("IS_NULLABLE");
            boolean isNullAble = BOOLEAN_YES.equalsIgnoreCase(isNullable);
            Column tableColumn = new Column(columnName, ColumnType.toName(dataTypeName), columnSize, isNullAble, remarks, columnDef);
            columns.add(tableColumn);
        }
        columnRs.close();

        // 获取表字段的详细信息
        for(Column column : columns){
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
    private void getTableColumnDetail(Table table, Column tableColumn) throws SQLException {
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
        if (IS_CACL_ENUM && null != table.getRowCount() && table.getRowCount() > 0 && isEnumColumnType(dataTypeName)) {
            // 行数
            String enumRowCountSql = "select count(distinct " + columnName.toUpperCase() + ") DATA_ROW_COUNT from \"" + table.getSchema() + "\".\"" + table.getName() + "\"";
            try {
                ps = con.prepareStatement(enumRowCountSql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    int colEnumRowCount = rs.getInt("DATA_ROW_COUNT");
                    tableColumn.setEnumerable(colEnumRowCount < COLUMN_ENUM_TABLE_ROW_COUNT);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("error SQL : " + enumRowCountSql);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        if (tableColumn.getEnumerable()) {
            // 行数
            String enumRowCountSql = "select distinct " + columnName.toUpperCase() + " from \"" + table.getSchema() + "\".\"" + table.getName() + "\"";
            ps = con.prepareStatement(enumRowCountSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String value = rs.getString(columnName.toUpperCase());
                if (StringKit.isSpecialString(value)) {
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
                tableColumn.setEnumerateValue(enumerateValue);
            }
        }
    }

    /**
     * 是否枚举类型的表字段类型
     * 目前仅对数字和字符类型的字段做枚举值读取。主要判断 number,varchar 及其衍生类型
     * @param dataTypeName 类型名称
     * @return boolean 是/否
     */
    private boolean isEnumColumnType(String dataTypeName){
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

    public Database analyzeDatabase(List<String> schemas, List<String> excludes) {
        Database database = new Database();

        for (String schemaStr : schemas) {
            Schema schema = new Schema();

            // 获取数据库版本信息
            try {
                String databaseVersion = dbMetaData.getDatabaseProductVersion();
                database.setVersion(databaseVersion);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // 设置数据库 schema
            schema.setName(schemaStr);
            List<String> excluded = schema.getExcluded();
            schema.setTables(listTables(schemaStr, excludes, excluded));

            // List<String> synonyms = dbDao.findSynonyms(schemaStr);
            List<String> synonyms = Lists.newArrayList();
            schema.setSynonyms(synonyms);

            database.getSchemas().add(schema);
        }

        setDataBaseTableInfo(database);

        return database;
    }

    /**
     * 获取行数最多的20张表
     * @param schemas
     * @return
     */
    public List<Table> getAllTable(List<Schema> schemas) {
        List<Table> tables = Lists.newArrayList();
        for (Schema schema : schemas) {
            tables.addAll(schema.getTables());
        }
        return tables;
    }

    /**
     * 分析表数据的行数
     * 前20%多行数的表
     * 后30%少行数的表
     * 空表
     * @param database
     * @return
     */
    public void setDataBaseTableInfo(Database database) {
        for (Schema schema : database.getSchemas()) {
            List<Table> tables = Lists.newArrayList(schema.getTables());

            tables.sort(Comparator.comparingInt(Table::getRowCount).reversed());
            schema.setOrderRowTables(tables);

            // 无数据的表
            tables = Lists.newArrayList(schema.getTables());
            List<Table> emptyRowTables = ListUtils.select(tables, table -> table.getRowCount() == 0);
            schema.setEmptyRowTables(emptyRowTables);


            // 数据最多的表
            tables = Lists.newArrayList(schema.getTables());
            tables = ListUtils.select(tables, table -> table.getRowCount() != 0);
            tables.sort(Comparator.comparingInt(Table::getRowCount));
            List<Table> maxRowTables = tables.subList(0, tables.size() > 20 ? 20 : tables.size() / 2);
            schema.setMaxRowTables(maxRowTables);

            // 少数据的表
            tables = Lists.newArrayList(schema.getTables());
            tables.sort(Comparator.comparingInt(Table::getRowCount));
            List<Table> minRowTable = tables.subList(0, tables.size() > 20 ? 20 : tables.size() / 2);
            schema.setMinRowTables(minRowTable);
        }
    }

    public static void main(String[] args) {
        DbAnalyze dbUtil = new DbAnalyze();
        List<Table> tables = dbUtil.listTables(SysConfig.SCHEMAS.get(0), "T_SHARE_FILETYPE");
        System.out.println(JSONObject.toJSON(tables));
    }
}