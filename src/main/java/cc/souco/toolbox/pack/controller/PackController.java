package cc.souco.toolbox.pack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pack")
public class PackController {

    @RequestMapping("")
    public String pack(){
        return "pack/index";
    }
}
