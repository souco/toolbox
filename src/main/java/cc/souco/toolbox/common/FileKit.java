package cc.souco.toolbox.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileKit {

    public static final Logger logger = LoggerFactory.getLogger(FileKit.class);

    public static String toString(String filepath){
        return toString(newFileSafety(filepath));
    }

    public static String toString(File file){
        StringBuilder sb = new StringBuilder();
        Reader reader = null;
        BufferedReader br = null;
        try {
            reader = new FileReader(file);
            br = new BufferedReader(reader);
            String data;
            while ((data = br.readLine()) != null) {
                sb.append(data);
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
        return sb.toString();
    }

    public static void toFile(String fullFilename, String value){
        try {
            FileWriter fileWriter = new FileWriter(newFileSafety(fullFilename));
            fileWriter.write(value);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File newFileSafety(String filepath) {
        File file = new File(filepath);
        File fileDir = file.getParentFile();
        // 如果文件目录不存在，创建多级目录
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            logger.error("文件创建失败，文件输出路径创建失败：" + filepath);
            throw new IllegalArgumentException("文件创建失败，文件输出路径创建失败");
        }
        return file;
    }

	public static void main(String[] args) {

	}
}