package cc.souco.toolbox.pack.vo;

public class SvnFileInfo {
    private String path;
    private Integer changeType;
    private Integer fileType;

    public static final Integer CHANGE_TYPE_ADDED = 0;
    public static final Integer CHANGE_TYPE_DELETED = 1;
    public static final Integer CHANGE_TYPE_MODIFIED = 2;
    public static final Integer CHANGE_TYPE_REPLACED = 3;
    public static final Integer FILE_TYPE_DIR = 0;
    public static final Integer FILE_TYPE_FILE = 1;
    public static final Integer FILE_TYPE_UNKNOWN = 2;

    public SvnFileInfo() {
    }

    public SvnFileInfo(String path, Integer changeType, Integer fileType) {
        this.path = path;
        this.changeType = changeType;
        this.fileType = fileType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getChangeType() {
        return changeType;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }
}
