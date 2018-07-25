package cc.souco.toolbox.export;

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

    public void export(Map dataMap, String exportPath) {
        // 模板文件所在路径
        configuration.setClassForTemplateLoading(this.getClass(), "");
        Template t = null;
        try {
            // 获取模板文件
            t = configuration.getTemplate("/test.ftl");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File outFile = new File(exportPath + File.separator + "导出测试" + System.currentTimeMillis() + ".doc"); //导出文件
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        try {
            // 将填充数据填入模板文件并输出到目标文件
            t.process(dataMap, out);
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Map<Object, Object> dataMap = Maps.newHashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        dataMap.put("valueDate", sdf.format(new Date()));
        dataMap.put("valueDateBaseName", "jeeplus");

        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("valueTableName", "表名" + i);
            map.put("valueTableCommons", "表注释" + i);
            list.add(map);
        }
        dataMap.put("tables", list);

        new ExportWord().export(dataMap, "C:/Users/souco/Desktop");
    }
}
