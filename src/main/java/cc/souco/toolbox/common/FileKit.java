package cc.souco.toolbox.common;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

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

    public static String getFileExtension(String fileFullName) {
        if (StringUtils.isBlank(fileFullName)) {
            throw new RuntimeException("fileFullName is empty");
        }
        return getFileExtension(new File(fileFullName));
    }

    public static String getFileExtension(File file) {
        if (null == file) {
            throw new NullPointerException();
        }
        String fileName = file.getName();
        int dotIdx = fileName.lastIndexOf('.');
        return (dotIdx == -1) ? "" : fileName.substring(dotIdx + 1);
    }

    /**
     * 按指定间隔行数 rowCount 插入 text 行
     * @param inFile 输入文件
     * @param outFile 输出文件
     * @param text 插入文本
     * @param rowCount 间隔行数
     * @param cutRow 切分文件行数
     * @throws IOException 文件IO异常
     */
    public static void addText2FileInRowCount(File inFile, File outFile, String text, Long rowCount, Long cutRow, List<Map<String, String>> replaces) throws IOException {
        String outputFileName = outFile.getName();
        long timer = System.currentTimeMillis();
        int bufferSize = 20 * 1024 * 1024;//设读取文件的缓存为20MB

        //建立缓冲文本输入流
        FileInputStream fileInputStream = new FileInputStream(inFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream);
        BufferedReader input = new BufferedReader(inputStreamReader, bufferSize);

        FileWriter output = new FileWriter(outFile);
        String line;
        int lineCount = 0;
        //逐行读取，逐行输出
        while ((line = input.readLine()) != null) {
            // 文本内容替换处理
            for(Map<String, String> map : replaces) {
                line = line.replaceAll(map.get("key"), map.get("value"));
            }

            output.append(line).append("\r");

            // 按指定间隔插入文本
            lineCount++;
            if (lineCount % rowCount == 0 && lineCount > 0) {
                output.append(text).append("\r");
                output.flush();
            }

            if (cutRow != null && lineCount % cutRow == 0 && lineCount > 0) {
                output.append(text).append("\r");
                output.flush();

                String name = outFile.getName();
                int index = name.lastIndexOf(".");
                System.out.println("file write :" + outputFileName);
                outputFileName = name.substring(0, index) + "_" + lineCount + "." + getFileExtension(name);
                output = new FileWriter(outFile.getParent() + File.separator + outputFileName);
            }
        }

        output.append(text).append("\r");
        output.flush();
        output.close();
        input.close();
        System.out.println("file write :" + outputFileName);
        System.out.println("process finish.");
        timer = System.currentTimeMillis() - timer;
        System.out.println("处理时间：" + timer);
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
        String basePath = "D:\\xx\\xx\\xx\\xx\\";
        File inFile = newFileSafety(basePath + "xx.sql");
        File outFile = newFileSafety(basePath + "xx.sql");

        // 文本内容替换处理
        List<Map<String, String>> replaces = Lists.newArrayList();
        Map<String, String> replace = Maps.newHashMap();
        replace.put("key", "``");
        replace.put("value", "xx");
        replaces.add(replace);
        replace = Maps.newHashMap();
        replace.put("key", "`");
        replace.put("value", "");
        replaces.add(replace);

        try {
            addText2FileInRowCount(inFile, outFile, "commit;", 200L, 100000L, replaces);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}