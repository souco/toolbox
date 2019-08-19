package cc.souco.toolbox.export;

import cc.souco.toolbox.common.Encodes;
import cc.souco.toolbox.common.FileKit;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class ExportWord {

    private Configuration configuration;
    private static Logger logger = LoggerFactory.getLogger(ExportWord.class);

    public ExportWord() {
        configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
    }

    /**
     * 导出 Word 文档
     * 格式为 doc 格式 (docx 格式打开文件有问题)
     *
     * @param template 导出的模板全路径，包含路径及文件名
     * @param exportFilepath 导出的文件全路径，包含路径及文件名
     * @param data 导出的数据
     */
    public ExportWord export(String template, String exportFilepath, Object data) {
        // 创建导出文件
        File outFile = FileKit.newFileSafety(exportFilepath);

        // 模板文件所在路径
        configuration.setClassForTemplateLoading(this.getClass(), "");
        Template exportTemplate;

        // 获取模板文件
        try {
            exportTemplate = configuration.getTemplate(File.separator + template);
        } catch (IOException e) {
            logger.error("获取模板文件失败");
            throw new RuntimeException(e);
        }

        Writer out;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
        } catch (FileNotFoundException e) {
            logger.error("输出生成文件失败");
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            logger.error("输出生成文件失败");
            throw new RuntimeException(e);
        }

        // 将填充数据填入模板文件并输出到目标文件
        try {
            exportTemplate.process(data, out);
            out.flush();
            out.close();
        } catch (TemplateException e) {
            logger.error("", e);
        } catch (IOException e) {
            logger.error("", e);
        }
        logger.info("Word 文件导出成功：" + exportFilepath);
        return this;
    }

    /**
     * 输出到客户端
     *
     * @param fileName 输出文件名
     */
    public ExportWord write(HttpServletResponse response, String fileName, String filepath) {
        response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(fileName));
        try {
            response.getOutputStream().write(FileUtils.readFileToByteArray(new File(filepath)));
        } catch (IOException e) {
            logger.error("", e);
        }
        return this;
    }
}
