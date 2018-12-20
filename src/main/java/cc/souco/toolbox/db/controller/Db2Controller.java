package cc.souco.toolbox.db.controller;

import cc.souco.toolbox.db.service.DbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/db2")
public class Db2Controller {

    private Logger logger = LoggerFactory.getLogger(Db2Controller.class);

    @Autowired
    private DbService dbService;

    @RequestMapping(value = {"/", "schemas"})
    public String schemas(Map<String, Object> model) {
        // List<String> schemas = dbService.findSchemas(null);
        // model.put("schemas", schemas);
        model.put("message", "hi");
        return "db/db";
    }

    @RequestMapping("/test")
    public String test() {


        return "test";
    }

}
