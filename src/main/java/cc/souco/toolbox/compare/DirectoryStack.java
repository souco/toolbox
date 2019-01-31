package cc.souco.toolbox.compare;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 目录栈
 * 由数组构成，数组索引由低到高，对应父文件夹到子文件夹
 */
public class DirectoryStack {
    // 当前数组索引
    private Integer index;
    // 目录名称数组
    private DirectoryFile[] directory;

    /**
     * 构造方法
     */
    public DirectoryStack() {
        this.index = -1;
        this.directory = new DirectoryFile[16];
    }

    /**
     * 进入子目录
     * @param subDir 子目录名称
     * @return 子目录路径
     */
    public String subDirectory(Integer offset, String subDir) {
        index++;
        if (index >= directory.length) {
            this.directory = addArrayLength(directory);
        }

        directory[index] = new DirectoryFile(offset, subDir);
        return this.toString();
    }

    /**
     * 返回指定父级目录
     * 从当前目录往前的第一个相同名称文件夹所在的目录
     * @param parentDir 指定父级目录文件夹名称
     * @return 指定父级目录路径
     */
    public String parentSiblingDirectory(Integer lineOffset, String parentDir) {
        if (StringUtils.isBlank(parentDir)) {
            return this.toString();
        }

        for (int i = index; i >= 0; i--) {
            if (directory[i].getOffset() <= lineOffset) {
                index = i;
                directory[index] = new DirectoryFile(lineOffset, parentDir);
                break;
            }
        }
        return this.toString();
    }

    /**
     * 返回指定兄弟目录
     * @param dir 指定兄弟目录文件夹名称
     * @return 指定兄弟目录路径
     */
    public String siblingDirectory(Integer subIndex, String dir) {
        if (StringUtils.isBlank(dir)) {
            return this.toString();
        }
        directory[index] = new DirectoryFile(subIndex, dir);
        return this.toString();
    }

    /**
     * 获取当前目录名称前特殊字符的行偏移量
     * @return
     */
    public int getCurrentOffset() {
        if (-1 == index) {
            return index;
        }
        return this.directory[index].getOffset();
    }

    /**
     * @return 拼接目录
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(File.separator);
        for (int i = 0; i <= index; i++) {
            if (null != directory[i] && StringUtils.isNotBlank(directory[i].getName())) {
                sb.append(directory[i].getName()).append(File.separator);
            }
        }
        return sb.toString();
    }

    /**
     * 数组扩容
     * 扩大一倍容量
     * @param array 数组
     * @return newArray 新数组
     */
    public static DirectoryFile[] addArrayLength(DirectoryFile[] array){
        DirectoryFile[] newArray = new DirectoryFile[array.length * 2];
        // 将 array 数组从 0 位置至 array.length 位置，复制到 newArray 数组 0 位置到 array.length 位置。
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

}
