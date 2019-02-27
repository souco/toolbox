package cc.souco.toolbox.pack.vo;

import cc.souco.toolbox.common.StringKit;

import java.util.List;

public class ProjectConfig {
    private String name;
    private String location;
    private List<String> javaPath;
    private String compilePath;
    private String outputPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return StringKit.trimAndCorrectSlash(location);
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getJavaPath() {
        return javaPath;
    }

    public void setJavaPath(List<String> javaPath) {
        this.javaPath = javaPath;
    }

    public String getCompilePath() {
        return StringKit.trimAndCorrectSlash(compilePath);
    }

    public void setCompilePath(String compilePath) {
        this.compilePath = compilePath;
    }

    public String getOutputPath() {
        return StringKit.trimAndCorrectSlash(outputPath);
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
