package cc.souco.toolbox.pack.vo;

public class PackageParameterVo {
    ProjectConfig config;
    SvnLogInfo info;

    public ProjectConfig getConfig() {
        return config;
    }

    public void setConfig(ProjectConfig config) {
        this.config = config;
    }

    public SvnLogInfo getInfo() {
        return info;
    }

    public void setInfo(SvnLogInfo info) {
        this.info = info;
    }
}
