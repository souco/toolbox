package cc.souco.toolbox.export;

import cc.souco.toolbox.common.ListKit;
import cc.souco.toolbox.db.DbUtil;
import cc.souco.toolbox.db.vo.Database;
import cc.souco.toolbox.db.vo.Table;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExportWord {

    private Configuration configuration;
    private static Logger logger = LoggerFactory.getLogger(ExportWord.class);

    public ExportWord() {
        configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
    }

    public void export(Object dataMap, String exportPath, String filename) {
        // 创建导出文件
        String fullPath = exportPath + File.separator + filename + ".doc";
        File outFile = new File(fullPath);

        // 模板文件所在路径
        configuration.setClassForTemplateLoading(this.getClass(), "");
        Template exportTemplate;

        // 获取模板文件
        try {
            exportTemplate = configuration.getTemplate("/databaseDetailTemplate.ftl");
        } catch (IOException e) {
            logger.error("获取模板文件失败");
            throw new RuntimeException(e);
        }

        Writer out;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
        } catch (FileNotFoundException e) {
            logger.error("输出生成文件失败");
            throw new RuntimeException(e);
        }

        // 将填充数据填入模板文件并输出到目标文件
        try {
            exportTemplate.process(dataMap, out);
            out.flush();
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
        }
        logger.info(fullPath);
    }

    public static void main(String[] args) {
        List<String> schemas = Lists.newArrayList("aa", "bb");
        DbUtil dbUtil = new DbUtil();
        List<Database> databases = dbUtil.analyzeDatabase(schemas);
        Map<String, Object> map = Maps.newHashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = sdf.format(new Date());
        map.put("date", date);
        map.put("schemas", schemas.toString());
        map.put("version", databases.get(0).getVersion());
        map.put("databases", databases);
        logger.info(JSONObject.toJSON(databases).toString());

        sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String filename = "数据库文档" + sdf.format(new Date()) + "(" + ListKit.join(schemas, ",") + ")";
        new ExportWord().export(map, "E:/ProjectData", filename);
        logger.info(filename);
    }

    /**
     * 摇摆检查错误数据
     * @param db
     */
    public static void checkError(Database db, String filename){
        Map<String, Object> map = Maps.newHashMap();
        List<Table> tables = db.getTables();
        tables = tables.subList(tables.size() - 84, tables.size()-82);
        db.setTables(tables);
        map.put("database", db);

        do {
            System.out.println("table size : " + tables.size());
            System.out.println(JSONObject.toJSON(db));
            new ExportWord().export(map, "E:/ProjectData/Gen", filename);

            tables = ListKit.preHalf(tables);
            db.setTables(tables);
            map.put("database", db);
        } while (tables.size() > 2);

        for (Table table : tables) {
            db.setTables(Lists.newArrayList(table));
            System.out.println(JSONObject.toJSON(db));
            new ExportWord().export(map, "E:/ProjectData/Gen", filename);
        }
    }

}
