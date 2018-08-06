package cc.souco.toolbox.db.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(value = "/db")
public class DbController {

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String DbTest(@RequestParam(required = false) String userName){
        return "Hello " + (userName == null ? "souco" : userName);
    }
}
