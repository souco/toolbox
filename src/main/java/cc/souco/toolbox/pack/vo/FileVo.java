package cc.souco.toolbox.pack.vo;

public class FileVo {
    String relationPath;
    String absolutePath;

    public String getRelationPath() {
        return relationPath;
    }

    public void setRelationPath(String relationPath) {
        this.relationPath = relationPath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    @Override
    public String toString() {
        return "FileVo{" +
                "relationPath='" + relationPath + '\'' +
                ", absolutePath='" + absolutePath + '\'' +
                '}';
    }
}
