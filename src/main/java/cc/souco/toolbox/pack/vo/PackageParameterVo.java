package cc.souco.toolbox.pack.vo;

public class PackageParameterVo {
    ProjectConfig config;
    SvnLogInfoVo info;

    public ProjectConfig getConfig() {
        return config;
    }

    public void setConfig(ProjectConfig config) {
        this.config = config;
    }

    public SvnLogInfoVo getInfo() {
        return info;
    }

    public void setInfo(SvnLogInfoVo info) {
        this.info = info;
    }
}
