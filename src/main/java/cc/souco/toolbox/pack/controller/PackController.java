package cc.souco.toolbox.pack.controller;

import cc.souco.toolbox.common.Ret;
import cc.souco.toolbox.pack.service.SvnService;
import cc.souco.toolbox.pack.vo.*;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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
    public Ret testSvnConfig(@RequestBody SvnUser user){
        Ret ret = Ret.ok();
        try {
            boolean result = svnService.testSvnConfig(user);
            if (result) {
                ret.set("msg", "测试成功");
            } else {
                return ret.setFail("用户名或密码不正确！");
            }
        } catch (Exception e) {
            return ret.setFail(e.getMessage());
        }
        return ret;
    }

    @ResponseBody
    @RequestMapping("/getSvnConfig")
    public Ret getSvnConfig(){
        Ret ret = Ret.ok();
        SvnUser svnUser = svnService.getSvnUser();
        return ret.set("user", svnUser);
    }

    @ResponseBody
    @RequestMapping("/saveSvnConfig")
    public Ret saveSvnConfig(@RequestBody SvnUser user){
        Ret ret = Ret.ok();
        try {
            svnService.saveSvnUser(user);
            ret.set("msg", "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ret.setFail(e.getMessage());
        }
        return ret;
    }

    @ResponseBody
    @RequestMapping("/getProjectConfig")
    public Ret getProjectConfig(){
        Ret ret = Ret.ok();
        List<ProjectConfig> configs = svnService.getProjectConfigs();
        return ret.set("configs", configs);
    }

    @ResponseBody
    @RequestMapping("/saveProjectConfig")
    public Ret saveProjectConfig(String configs){
        Ret ret = Ret.ok();
        try {
            List<ProjectConfig> configList = JSON.parseArray(configs, ProjectConfig.class);
            svnService.saveProjectConfigs(configList);
            ret.set("msg", "保存成功").set("configs", configList);
        } catch (Exception e) {
            e.printStackTrace();
            return ret.setFail(e.getMessage());
        }
        return ret;
    }

    @ResponseBody
    @RequestMapping("/listSvnLog")
    public Ret listSvnLog(Integer projectSelect){
        Ret ret = Ret.ok();
        try {
            ProjectConfig config;
            SvnUser user = svnService.getSvnUser();
            List<ProjectConfig> configs = svnService.getProjectConfigs();
            if (configs != null && !configs.isEmpty()) {
                config = configs.get(projectSelect);
            } else {
                throw new RuntimeException("请先选择或配置项目信息！");
            }

            if (user == null || StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
                throw new RuntimeException("请先选择或配置SVN账户信息！");
            }

            List<SvnLogInfo> infos = svnService.findSvnLog(user, config.getLocation(), null, null, 10);
            ret.set("msg", "保存成功").set("infos", infos);
        } catch (Exception e) {
            e.printStackTrace();
            return ret.setFail(e.getMessage());
        }
        return ret;
    }

    @ResponseBody
    @RequestMapping("/packageUpdate")
    public Ret packageUpdate(@RequestBody PackageParameterVo packageVo){
        Ret ret = Ret.ok();
        try {
            svnService.packageUpdate(packageVo.getInfo(), packageVo.getConfig());
            ret.set("msg", "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ret.setFail(e.getMessage());
        }
        return ret;
    }
}
