package cc.souco.toolbox.db.controller;

import cc.souco.toolbox.common.FileKit;
import cc.souco.toolbox.common.ListKit;
import cc.souco.toolbox.common.Ret;
import cc.souco.toolbox.db.service.DbService;
import cc.souco.toolbox.db.vo.Database;
import cc.souco.toolbox.db.vo.code.DbDocTemplate;
import cc.souco.toolbox.export.ExportWord;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller()
@RequestMapping("db")
public class DbController {
    private Logger logger = LoggerFactory.getLogger(DbController.class);
    private static boolean DB_STATUS_CHECK;

    @Value("${TEMP_PATH}")
    private String TEMP_PATH;

    @Autowired
    private DbService dbService;

    @RequestMapping(value = {"", "schemas"})
    public String schemas(Model model) {
        model.addAttribute("dbStatusCheck", DB_STATUS_CHECK);
        return "db/schemas";
    }

    @RequestMapping("templates")
    public String templates(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object schemas = session.getAttribute("schemas");
        if (null != schemas) {
            return "db/templates";
        }
        return "db/schemas";
    }

    @RequestMapping(value = "api/export", method = RequestMethod.GET)
    public String exportRange(HttpServletRequest request, HttpServletResponse response, @RequestParam("type") Integer type){
        List<String> schemas;
        String template;

        HttpSession session = request.getSession();
        Object schemasObj = session.getAttribute("schemas");
        if (null == schemasObj) {
            return "/schemas";
        } else {
            schemas = (List<String>) schemasObj;
        }

        DbDocTemplate docTemplate = DbDocTemplate.fromCode(type);

        Database database = dbService.buildDatabase(schemas, docTemplate);
        database.setDate(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String filename = "数据库文档" + sdf.format(new Date()) + "(" + ListKit.join(schemas, ",") + ")";
        String txtFilepath = TEMP_PATH + File.separator + filename + ".txt";
        String wordFilepath = TEMP_PATH + File.separator + filename + ".doc";
        String dataStr = JSONObject.toJSON(database).toString();
        FileKit.toFile(txtFilepath, dataStr);
        logger.info(dataStr);

        if (database.getSchemas().size() <= 1) {
            template = docTemplate.singleFilename();
        } else {
            template = docTemplate.filename();
        }
        new ExportWord().export(template, wordFilepath, database).write(response, filename + ".doc", wordFilepath);
        return null;
    }

    @RequestMapping("api/schemas")
    @ResponseBody
    public Ret schemasList(){
        Ret ret = Ret.ok();
        try {
            boolean result = dbService.testDbConnect();
            if (!result) {
                ret.setFail("数据库连接失败，请检查数据库配置！");
            }

            List<String> schemas = dbService.findSchemas();
            ret.put("schemas", schemas);
        } catch (Exception e) {
            ret.setFail(e.getMessage());
        }
        return ret;
    }

    @RequestMapping("/api/schemas/select")
    @ResponseBody
    public Ret schemasSelect(HttpServletRequest request, @RequestParam(required = false, value = "schemas[]") List<String> schemas){
        if (null == schemas || schemas.size() == 0) {
            return Ret.fail("请选择要导出的模式！");
        }
        
        HttpSession session = request.getSession();
        session.setAttribute("schemas", schemas);

        return Ret.ok();
    }

    @RequestMapping("api/docTemplates")
    @ResponseBody
    public Ret docTemplates(HttpServletRequest request){
        Ret ret = Ret.ok();

        List<String> schemas;
        HttpSession session = request.getSession();
        Object schemasObj = session.getAttribute("schemas");
        if (null == schemasObj) {
            schemas = Lists.newArrayList();
        } else {
            schemas = (List<String>) schemasObj;
        }

        try {
            List<DbDocTemplate> templates = Lists.newArrayList();
            DbDocTemplate[] values = DbDocTemplate.values();
            for (DbDocTemplate value : values) {
                if (schemas.size() == 1) {
                    if (StringUtils.isNotBlank(value.getSingle())) {
                        templates.add(value);
                    }
                } else {
                    if (StringUtils.isNotBlank(value.getFilename())) {
                        templates.add(value);
                    }
                }
            }

            ret.set("templates", templates);
        } catch (Exception e) {
            ret.setFail(e.getMessage());
        }
        return ret;
    }
}
