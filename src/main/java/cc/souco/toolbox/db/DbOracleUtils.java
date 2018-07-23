package cc.souco.toolbox.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbOracleUtils {

    private Connection conn = null;

    public Connection OpenConn() {
        try {
            Class.forName(DbConfig.DRIVER);
            try {
                conn = DriverManager.getConnection(DbConfig.URL, DbConfig.USERNAME, DbConfig.PASSWORD);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public ResultSet executeQuery(String sql) {
        DbOracleUtils db = new DbOracleUtils();
        ResultSet rs = null;
        Connection con = db.OpenConn();
        try {
            Statement sm = con.createStatement();
            rs = sm.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 获取数据库中所有表的表名，并添加到列表结构中。
    public List getTableNameList(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTables(null, "SC_FGWCMS", null, null);
        List tableNameList = new ArrayList();
        while (rs.next()) {
            tableNameList.add(rs.getString("TABLE_NAME"));
        }
        return tableNameList;
    }

    // 获取数据表中所有列的列名，并添加到列表结构中。
    public List getColumnNameList(Connection conn, String tableName)
            throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();
        ResultSet rs = dbMetaData.getColumns(null, null, tableName, null);
        List columnNameList = new ArrayList();
        while (rs.next()) {
            columnNameList.add(rs.getString("COLUMN_NAME"));
        }
        return columnNameList;
    }

    public static void main(String s[]) throws SQLException {
        DbOracleUtils dbConn = new DbOracleUtils();
        Connection conn = dbConn.OpenConn();
        if (conn == null)
            System.out.println("连接失败");
        else
            System.out.println("连接成功");
        try {
            // List tableList = dbConn.getTableNameList(conn);//取出当前用户的所有表
            List tableList = dbConn.getColumnNameList(conn, "V_SHOW_DECLARATION");//表名称必须是大写的，取出当前表的所有列
            System.out.println(tableList.size());
            for (Object object : tableList) {
                String ss = (String) object;
                System.out.println(ss);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}