package cc.souco.toolbox.pack.controller;

import cc.souco.toolbox.common.Ret;
import cc.souco.toolbox.pack.service.SvnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/pack")
public class PackController {

    @Autowired
    private SvnService svnService;

    @RequestMapping("")
    public String pack(){
        return "pack/index";
    }

    @ResponseBody
    @RequestMapping("/testSvnConfig")
    public Ret testSvnConfig(String username, String password, String location){
        Ret ret = Ret.ok();
        try {
            boolean result = svnService.testSvnConfig(username, password, location);
            if (result) {
                ret.set("msg", "测试成功");
            } else {
                return ret.setFail("用户名或密码不正确！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ret.setFail(e.getMessage());
        }
        return ret;
    }
}
