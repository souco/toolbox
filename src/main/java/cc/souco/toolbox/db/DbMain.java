package cc.souco.toolbox.db;

import cc.souco.toolbox.common.FileKit;
import cc.souco.toolbox.common.ListKit;
import cc.souco.toolbox.common.SysConfig;
import cc.souco.toolbox.db.vo.Column;
import cc.souco.toolbox.db.vo.Database;
import cc.souco.toolbox.db.vo.Schema;
import cc.souco.toolbox.db.vo.Table;
import cc.souco.toolbox.db.vo.code.ColumnType;
import cc.souco.toolbox.export.ExportWord;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DbMain {

    private static Logger logger = LoggerFactory.getLogger(DbMain.class);

    public static void main(String[] args) {
        DbAnalyze dbUtil = new DbAnalyze();
        Database database = dbUtil.analyzeDatabase(SysConfig.SCHEMAS);

        // 从txt文件读取
        // database = getDataFromFile();

        database.setDate(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String filename = "数据库文档" + sdf.format(new Date()) + "(" + ListKit.join(SysConfig.SCHEMAS, ",") + ")";
        String txtFilepath = SysConfig.BASE_PATH + filename + ".txt";
        String wordFilepath = SysConfig.BASE_PATH + filename + ".doc";
        String dataStr = JSONObject.toJSON(database).toString();
        FileKit.toFile(txtFilepath, dataStr);
        logger.info(dataStr);
        new ExportWord().export(SysConfig.DATABASE_TEMPLATE_TABLE_INFO, wordFilepath, database);
    }

    /**
     * 从文本文件获取数据库信息
     * 用于有时导出失败，但是读取的数据库信息已json的形式保存到了文本文件，此时可直接从文本文件读取。
     * @param filepath
     * @return
     */
    public static Database getDataFromFile(String filepath){
        String databaseStr = FileKit.toString(filepath);
        Database database = (Database)JSONObject.parse(databaseStr);
        List<Schema> schemas = database.getSchemas();
        for (Schema schema : schemas) {
            List<Table> tables = schema.getTables();
            for (Table table : tables) {
                List<Column> columns = table.getColumns();
                for (Column column : columns) {
                    column.setType(ColumnType.toName(column.getType()));
                }
            }
        }
        return database;
    }

    /**
     * 摇摆检查错误数据
     * @param schema 数据库模式
     * @param filepath 导出的文件路径
     */
    public static void checkError(Schema schema, String filepath){
        Map<String, Object> map = Maps.newHashMap();
        List<Table> tables = schema.getTables();
        tables = tables.subList(tables.size() - 84, tables.size()-82);
        schema.setTables(tables);
        map.put("database", schema);

        do {
            System.out.println("table size : " + tables.size());
            System.out.println(JSONObject.toJSON(schema));
            new ExportWord().export(SysConfig.DATABASE_TEMPLATE_BASIC, filepath, map);

            tables = ListKit.preHalf(tables);
            schema.setTables(tables);
            map.put("database", schema);
        } while (tables.size() > 2);

        for (Table table : tables) {
            schema.setTables(Lists.newArrayList(table));
            System.out.println(JSONObject.toJSON(schema));
            new ExportWord().export(SysConfig.DATABASE_TEMPLATE_BASIC, filepath, map);
        }
    }
}
