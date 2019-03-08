package cc.souco.toolbox.pack.vo;

import cc.souco.toolbox.common.StringKit;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class ProjectConfig {
    private String name;
    private String location;
    private List<String> javaPath;
    private String compilePath;
    private String outputPath;
    private Long lastPackRevision;
    private boolean isLastPack;
    private boolean openDir;
    private boolean isNeedZip;

    public ProjectConfig() {
        this.isLastPack = false;
        this.openDir = false;
    }

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

    public boolean getIsLastPack() {
        return isLastPack;
    }

    public void setIsLastPack(boolean isLastPack) {
        this.isLastPack = isLastPack;
    }

    public boolean getOpenDir() {
        return openDir;
    }

    public void setOpenDir(boolean openDir) {
        this.openDir = openDir;
    }

    public Long getLastPackRevision() {
        return lastPackRevision;
    }

    public void setLastPackRevision(Long lastPackRevision) {
        this.lastPackRevision = lastPackRevision;
    }

    public boolean getIsNeedZip() {
        return isNeedZip;
    }

    public void setIsNeedZip(boolean needZip) {
        isNeedZip = needZip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProjectConfig that = (ProjectConfig) o;

        return new EqualsBuilder()
                .append(openDir, that.openDir)
                .append(name, that.name)
                .append(location, that.location)
                .append(javaPath, that.javaPath)
                .append(compilePath, that.compilePath)
                .append(outputPath, that.outputPath)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(location)
                .append(javaPath)
                .append(compilePath)
                .append(outputPath)
                .append(openDir)
                .toHashCode();
    }

    /**
     * 判断路径是否已java源码路径开始
     * @param path 被判断的路径
     * @return 是否
     */
    public boolean isStartWithJavaCodeDir(String path){
        for (String dir : this.getJavaPath()) {
            dir = StringKit.trimAndCorrectSlash(dir);
            if(path.startsWith(dir)){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取java代码基础相对目录的长度
     * @param path 传入路径
     * @return 长度
     */
    public int getJavaCodeDirLength(String path){
        for (String dir : this.getJavaPath()) {
            dir = StringKit.trimAndCorrectSlash(dir);
            if(path.startsWith(dir)){
                return dir.length();
            }
        }
        throw new RuntimeException();
    }
}
