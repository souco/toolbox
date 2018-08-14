package cc.souco.toolbox.export;

import cc.souco.toolbox.db.DbUtil;
import cc.souco.toolbox.db.vo.Database;
import cc.souco.toolbox.db.vo.Table;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExportWord {

    private Configuration configuration;

    public ExportWord() {
        configuration = new Configuration();
        configuration.setDefaultEncoding("UTF-8");
    }

    public void export(Object dataMap, String exportPath) {
        // 模板文件所在路径
        configuration.setClassForTemplateLoading(this.getClass(), "");
        Template t = null;
        try {
            // 获取模板文件
            t = configuration.getTemplate("/databaseDetailTemplate.ftl");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fullPath = exportPath + File.separator + "导出测试" + System.currentTimeMillis() + ".doc";
        File outFile = new File(fullPath); //导出文件
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        try {
            // 将填充数据填入模板文件并输出到目标文件
            t.process(dataMap, out);
            out.flush();
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        System.out.println(fullPath);
    }

    public static void main(String[] args) {
        DbUtil dbUtil = new DbUtil();
        String[] schemas = {"aa", "bb"};
        List<Database> databases = dbUtil.analyzeDatabase(schemas);
        Map<String, Object> map = Maps.newHashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = sdf.format(new Date());
        map.put("date", date);
        map.put("schemas", Arrays.toString(schemas));
        map.put("version", databases.get(0).getVersion());
        map.put("databases", databases);

        System.out.println(JSONObject.toJSON(databases));
        new ExportWord().export(map, "E:/ProjectData");
    }

    public static void checkError(Database db){
        Map<String, Object> map = Maps.newHashMap();
        List<Table> tables = db.getTables();
        tables = tables.subList(tables.size() - 84, tables.size()-82);
        db.setTables(tables);
        map.put("database", db);

        do {
            System.out.println("table size : " + tables.size());
            System.out.println(JSONObject.toJSON(db));
            new ExportWord().export(map, "E:/ProjectData");

            tables = listSuffixHafh(tables);
            db.setTables(tables);
            map.put("database", db);
        } while (tables.size() > 2);

        for (Table table : tables) {
            db.setTables(Lists.newArrayList(table));
            System.out.println(JSONObject.toJSON(db));
            new ExportWord().export(map, "E:/ProjectData");
        }
    }

    public static List<Table> listPreHafh(List<Table> tables) {
        if (tables.size() > 2) {
            return tables.subList(0, tables.size() / 2);
        }
        return tables;
    }

    public static List<Table> listSuffixHafh(List<Table> tables) {
        if (tables.size() > 2) {
            return tables.subList(tables.size() / 2, tables.size());
        }
        return tables;
    }
}
