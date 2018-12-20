package cc.souco.toolbox.db.controller;

import cc.souco.toolbox.common.FileKit;
import cc.souco.toolbox.common.ListKit;
import cc.souco.toolbox.common.Ret;
import cc.souco.toolbox.common.SysConfig;
import cc.souco.toolbox.db.DbAnalyze;
import cc.souco.toolbox.db.service.DbService;
import cc.souco.toolbox.db.vo.Database;
import cc.souco.toolbox.db.vo.Schema;
import cc.souco.toolbox.db.vo.Table;
import cc.souco.toolbox.export.ExportWord;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController()
@RequestMapping(value = "/api/db")
public class DbController {

    private Logger logger = LoggerFactory.getLogger(DbController.class);

    @Autowired
    private DbService dbService;

    // @Autowired
    // private DbAnalyze analyze;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public List DbTest(@RequestParam(required = false) String name){
        return dbService.findSynonyms(name);
    }

    @RequestMapping(value = "/export/classic", method = RequestMethod.GET)
    public String exportClassic(){
        // Database database = new DbAnalyze().analyzeDatabase(SysConfig.SCHEMAS, SysConfig.EXCLUDES);
        Database database = new DbAnalyze().analyzeDatabase(SysConfig.SCHEMAS, Lists.newArrayList());
        database.setDate(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String filename = "数据库文档" + sdf.format(new Date()) + "(" + ListKit.join(SysConfig.SCHEMAS, ",") + ")";
        String txtFilepath = SysConfig.BASE_PATH + filename + ".txt";
        String wordFilepath = SysConfig.BASE_PATH + filename + ".doc";
        String dataStr = JSONObject.toJSON(database).toString();
        FileKit.toFile(txtFilepath, dataStr);
        logger.info(dataStr);
        new ExportWord().export(SysConfig.DATABASE_TEMPLATE_BASIC, wordFilepath, database);
        return "success";
    }

    @RequestMapping(value = "/export/range", method = RequestMethod.GET)
    public String exportRange(){
        Database database = new DbAnalyze().analyzeDatabase(SysConfig.SCHEMAS, SysConfig.EXCLUDES);
        database.setDate(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String filename = "数据库文档" + sdf.format(new Date()) + "(" + ListKit.join(SysConfig.SCHEMAS, ",") + ")";
        String txtFilepath = SysConfig.BASE_PATH + filename + ".txt";
        String wordFilepath = SysConfig.BASE_PATH + filename + ".doc";
        String dataStr = JSONObject.toJSON(database).toString();
        FileKit.toFile(txtFilepath, dataStr);
        logger.info(dataStr);
        new ExportWord().export(SysConfig.DATABASE_TEMPLATE_TABLE_INFO, wordFilepath, database);
        return "success";
    }

    @RequestMapping("schemas")
    public List<String> schemas(){
        List<String> schemas = dbService.findSchemas(null);
        return schemas;
    }

    @RequestMapping("tables")
    public Ret tables(HttpServletRequest request){
        List<String> schemas = (List<String>) request.getSession().getAttribute("schemas");

        if (schemas.isEmpty()) {
            return Ret.fail("请先选择要导出的模式！");
        }

        Database database = new Database();
        for (String schemaStr : schemas) {
            List<Table> tables = dbService.findTables(schemaStr);

            Schema schema = new Schema();
            schema.setName(schemaStr);
            schema.setTables(tables);
            database.getSchemas().add(schema);
        }

        request.getSession().setAttribute("database", database);
        return Ret.ok().set("database", database);
    }

    @RequestMapping("schemas/select")
    public Ret schemasSelect(HttpServletRequest request, @RequestParam(required = false, value = "schemas[]") List<String> schemas){
        if (null == schemas || schemas.size() == 0) {
            return Ret.fail("请选择要导出的模式！");
        }
        
        HttpSession session = request.getSession();
        session.setAttribute("schemas", schemas);

        return Ret.ok();
    }
    
    
}
