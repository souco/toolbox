package cc.souco.toolbox.compare;

/**
 * 目录文件夹对象
 */
public class DirectoryFile {
    // 目录特殊字符行偏移量
    private Integer offset;
    // 目录名称
    private String name;

    public DirectoryFile() {
    }

    public DirectoryFile(Integer offset, String name) {
        this.offset = offset;
        this.name = name;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DirectoryFile{" +
                "offset='" + offset + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
