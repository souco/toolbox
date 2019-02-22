package cc.souco.toolbox.index.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class IndexController {
    private Logger logger = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping(value = {"/", "/index"})
    public String schemas(Map<String, Object> model) {
        model.put("message", "hi");
        return "index";
    }

    @RequestMapping("/test")
    public String test() {

        return "test";
    }

}
