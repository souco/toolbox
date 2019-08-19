package cc.souco.toolbox.db.service;

import cc.souco.toolbox.common.CollectionKit;
import cc.souco.toolbox.common.StringKit;
import cc.souco.toolbox.db.vo.Column;
import cc.souco.toolbox.db.vo.CommentVo;
import cc.souco.toolbox.db.vo.Table;
import cc.souco.toolbox.db.vo.code.DbDocTemplate;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DbMysqlDialect extends DbDialect {
    private static final Logger logger = LoggerFactory.getLogger(DbMysqlDialect.class);

    @Override
    protected boolean testDbConnect() throws SQLException {
        String columnCommentsSql = "SELECT 1 from dual";
        PreparedStatement ps = getConnection().prepareStatement(columnCommentsSql);
        ResultSet rs = ps.executeQuery();
        int value = 0;
        if (rs.next()) {
            value = rs.getInt(1);
        }
        rs.close();
        ps.close();
        return value == 1;
    }

    @Override
    public List<String> findSchemas() throws SQLException {
        logger.info("findSchemas");
        List<String> schemas = super.findSchemas();
        if (schemas.isEmpty() && StringUtils.isNotBlank(getConnection().getCatalog())) {
            schemas.add(getConnection().getCatalog().toUpperCase());
        }
        return schemas;
    }

    @Override
    protected List<Table> buildTables(String schemaName, DbDocTemplate docTemplate) throws SQLException {
        List<Table> tables = super.buildTables(schemaName, docTemplate);
        buildTableRemark(tables);

        if (docTemplate.getShowPK()) {
            buildTablePrimaryKey(tables);
        }
        if (docTemplate.getShowRowCount()) {
            buildTableRowCountInfo(tables);
        }
        if (docTemplate.getShowEnumerateValue()) {
            buildColumnEnumerateValue(tables);
        }

        return tables;
    }

    private void buildTableRemark(List<Table> tables) throws SQLException {
        logger.info("build table remark");
        if (tables.isEmpty()) {
            return;
        }

        List<CommentVo> comments = Lists.newArrayList();
        String schema = tables.get(0).getSchema();
        PreparedStatement ps;
        ResultSet rs;// 获取字段注释
        String columnCommentsSql = "SELECT table_name TABLE_NAME, table_comment COMMENTS" +
                " FROM information_schema.TABLES" +
                " where upper(table_schema) = upper('" + schema + "')";
        ps = getConnection().prepareStatement(columnCommentsSql);
        rs = ps.executeQuery();
        while (rs.next()) {
            CommentVo vo = new CommentVo();
            vo.setTableName(rs.getString("TABLE_NAME"));
            vo.setRemarks(StringKit.clearSpecialStr(rs.getString("COMMENTS")));
            comments.add(vo);
        }
        rs.close();
        ps.close();

        Map<Object, List<CommentVo>> tableComments = CollectionKit.group(comments, CommentVo::getTableName);

        for (Table table : tables) {
            List<CommentVo> vos = tableComments.get(table.getName());
            if (vos != null && !vos.isEmpty()) {
                table.setRemarks(vos.get(0).getRemarks());
            } else if (vos == null) {
                logger.warn(table.toString());
            }
        }
    }

    private void buildTablePrimaryKey(List<Table> tables) throws SQLException {
        logger.info("build table primary key");
        if (tables.isEmpty()) {
            return;
        }

        for (Table table : tables) {
            Set<String> keys = Sets.newHashSet();
            ResultSet primaryKeyRs = getDatabaseMetaData().getPrimaryKeys(getConnection().getCatalog(), tables.get(0).getSchema(), table.getName());
            while (primaryKeyRs.next()) {
                String tableName = primaryKeyRs.getString("COLUMN_NAME");
                keys.add(tableName);
            }
            primaryKeyRs.close();

            table.setPrimaryKey(StringUtils.join(keys, ", "));
        }
    }

    private void buildTableRowCountInfo(List<Table> tables) throws SQLException {
        logger.info("build table row count info");

        if (tables.isEmpty()) {
            return;
        }

        Map<String, Integer> counts = Maps.newHashMap();
        String columnCommentsSql = "SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.tables WHERE table_schema = ?";
        PreparedStatement ps = getConnection().prepareStatement(columnCommentsSql);
        ps.setString(1, getConnection().getCatalog());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            Integer count = rs.getInt("TABLE_ROWS");
            counts.put(tableName, count);
        }
        rs.close();
        ps.close();

        for (Table table : tables) {
            table.setRowCount(counts.get(table.getName()));
        }
    }

    private void buildColumnEnumerateValue(List<Table> tables) throws SQLException {
        logger.info("build table column enumerate value");
        for (Table table : tables) {
            // 是否可枚举
            if (null != table.getRowCount() && table.getRowCount() > enumerableMinRowCount) {
                for (Column column : table.getColumns()) {
                    if (isEnumColumnType(column.getType())) {
                        column.setEnumerable(true);
                        List<String> enums = Lists.newArrayList();

                        // 行数
                        String enumerableValueSql = "select distinct " + column.getName().toUpperCase() + " from " + table.getName() + " limit 20";
                        PreparedStatement ps = getConnection().prepareStatement(enumerableValueSql);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            String value = rs.getString(column.getName().toUpperCase());
                            enums.add(StringKit.clearSpecialStr(value));

                            // fail fast
                            if (enums.size() > enumerableValueMaxCount) {
                                column.setEnumerable(false);
                                break;
                            }
                        }
                        rs.close();
                        ps.close();

                        String enumValues = StringUtils.join(enums, ",");
                        if (column.getEnumerable()
                                && !enums.isEmpty()
                                && enums.size() < enumerableValueMaxCount
                                && !StringKit.isContainChinese(enumValues)
                                && enumValues.length() < enumerableValueTotalCharCount) {
                            column.setEnumerateValue(StringUtils.join(enums, ","));
                        } else {
                            column.setEnumerateValue("");
                        }
                    }
                }
            }
        }
    }
}
