package cc.souco.toolbox.compare;

import cc.souco.toolbox.common.FileKit;
import cc.souco.toolbox.common.StringKit;
import com.beust.jcommander.internal.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * 目录比对
 */
public class DirectoryCompare {
    private static final String DIRECTORY_FILE_PREFIX = "│";
    private static final String DIRECTORY_SUFFIX = "├";
    private static final String DIRECTORY_SUFFIX2 = "└";
    private static final String DIRECTORY_PREFIX = "─";
    private static final String FILE_PREFIX = "  ";

    public static void main(String[] args) {
        DirectoryCompare compare = new DirectoryCompare();

        String file1Path = "D:\\aa.txt";
        String file2Path = "D:\\zcl\\内蒙古\\网厅正式.txt";
        String resultPath = "C:\\Users\\zxsp4\\Desktop\\比对结果.txt";

        compare.compare(file1Path, file2Path, resultPath);
        FileKit.openDirectory(resultPath);
    }

    public void compare(String filepath, String compareFilepath, String resultPath) {
        List<String> file1 = getDirectoryFile(filepath);
        List<String> fileTemp = Lists.newArrayList();
        fileTemp.addAll(file1);
        List<String> file2 = getDirectoryFile(compareFilepath);

        file1.removeAll(file2);
        file2.removeAll(fileTemp);

        StringBuilder sb = new StringBuilder();
        sb.append(filepath).append("\t 独有的文件：\r\n");
        sb.append(StringKit.toString(file1));
        sb.append("\r\n");

        sb.append(compareFilepath).append("\t 独有的文件：\r\n");
        sb.append(StringKit.toString(file2));

        FileKit.toFile(resultPath, sb.toString());
    }

    public List<String> getDirectoryFile(String path) {
        List<String> files = Lists.newArrayList();
        DirectoryStack directory = new DirectoryStack();
        Reader reader = null;
        BufferedReader br = null;
        try {
            reader = new FileReader(path);
            br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                files.add(analyzeDirectory(directory, line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return files;
    }

    public String analyzeDirectory(DirectoryStack directory, String line){
        if (isEmptyDirectory(line)) {
            return null;
        }

        if (isDirectory(line)) {
            int dirIndex = getDirectoryIndex(line);
            int lineOffset = directory.getCurrentOffset();
            String dir = getDirectoryName(line);
            if (lineOffset == dirIndex) {
                directory.siblingDirectory(dirIndex, dir);
            } else if (lineOffset < dirIndex) {
                directory.subDirectory(dirIndex, dir);
            } else if (lineOffset > dirIndex) {
                directory.parentSiblingDirectory(dirIndex, dir);
            }
        } else {
            String filename = getFilename(line);
            return directory.toString() + filename;
        }
        return null;
    }

    private boolean isDirectory(String content) {
        if (content.contains(DIRECTORY_SUFFIX) || content.contains(DIRECTORY_SUFFIX2)) {
            return true;
        }
        return false;
    }

    private boolean isEmptyDirectory(String line) {
        if (StringUtils.isBlank(line)) {
            return true;
        } else if (!line.contains(DIRECTORY_FILE_PREFIX) && !line.contains(DIRECTORY_SUFFIX) && !line.contains(DIRECTORY_SUFFIX2)) {
            return true;
        }

        line = line.replaceAll(DIRECTORY_FILE_PREFIX, "");
        line = line.replaceAll(DIRECTORY_SUFFIX, "");
        line = line.replaceAll(DIRECTORY_SUFFIX2, "");

        if (StringUtils.isBlank(line.trim())) {
            return true;
        }
        return false;
    }

    private int getDirectoryIndex(String content) {
        if (content.contains(DIRECTORY_SUFFIX)) {
            return content.lastIndexOf(DIRECTORY_SUFFIX);
        }
        return content.lastIndexOf(DIRECTORY_SUFFIX2);
    }

    private String getDirectoryName(String content) {
        return content.substring(content.lastIndexOf(DIRECTORY_PREFIX) + DIRECTORY_PREFIX.length());
    }

    private String getFilename(String content) {
        return content.substring(content.lastIndexOf(FILE_PREFIX) + FILE_PREFIX.length());
    }
}
