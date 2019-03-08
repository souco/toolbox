package cc.souco.toolbox.common;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
                sb.append(data).append("\r\n");
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
        toFile(newFileSafety(fullFilename), value);
    }

    public static void toFile(File file, String value){
        try {
            FileWriter fileWriter = new FileWriter(file);
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

    /**
     * 文件复制
     * @param fromBasePath 被复制文件的基础路径
     * @param toBasePath 目的文件的基础路径
     * @param relationPath 文件的相对路径
     * @throws IOException
     */
    public static void copyFile(String fromBasePath, String toBasePath, String relationPath) throws IOException {
        File from = new File(fromBasePath + File.separator + relationPath);
        File to = newFileSafety(toBasePath + File.separator + relationPath);
        Files.copy(from.toPath(), to.toPath());
    }

    /**
     * 文件复制
     * @param fromFile 被复制的文件
     * @param fromBasePath 被复制文件的基础路径
     * @param toBasePath 目的文件的基础路径
     * @throws IOException
     */
    public static void copyFile(File fromFile, String fromBasePath, String toBasePath) throws IOException {
        File to = newFileSafety(fromFile.getAbsolutePath().replace(fromBasePath, toBasePath));
        Files.copy(fromFile.toPath(), to.toPath());
    }

    /**
     * 打开资源管理器到指定目录
     * @param folder : directory
     */
    public static void openDirectory(String folder) {
        File file = new File(folder);
        if (!file.exists()) {
            return;
        }
        Runtime runtime = null;
        try {
            runtime = Runtime.getRuntime();
            String osName = System.getProperties().getProperty("os.name");
            if (osName.startsWith("Windows")) {
                runtime.exec("cmd /c start explorer " + folder);
            } else {
                runtime.exec("nautilus " + folder);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (null != runtime) {
                runtime.runFinalization();
            }
        }
    }

    public static String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null){
            buffer.append(line);
        }
        in.close();
        return buffer.toString();
    }

    /**
     * 压缩文件或目录
     * @param srcDirName 压缩的根目录
     * @param fileName 根目录下的待压缩的文件名或文件夹名，其中*或""表示跟目录下的全部文件
     * @param descFileName 目标zip文件
     */
    public static void zipFiles(String srcDirName, String fileName, String descFileName) {
        // 判断目录是否存在
        if (srcDirName == null) {
            logger.debug("文件压缩失败，目录 " + srcDirName + " 不存在!");
            return;
        }

        File fileDir = new File(srcDirName);
        if (!fileDir.exists() || !fileDir.isDirectory()) {
            logger.debug("文件压缩失败，目录 " + srcDirName + " 不存在!");
            return;
        }

        String dirPath = fileDir.getAbsolutePath();
        File descFile = new File(descFileName);
        try {
            ZipOutputStream zouts = new ZipOutputStream(new FileOutputStream(
                    descFile));
            if ("*".equals(fileName) || "".equals(fileName)) {
                FileKit.zipDirectoryToZipFile(dirPath, fileDir, zouts);
            } else {
                File file = new File(fileDir, fileName);
                if (file.isFile()) {
                    FileKit.zipFilesToZipFile(dirPath, file, zouts);
                } else {
                    FileKit.zipDirectoryToZipFile(dirPath, file, zouts);
                }
            }
            zouts.close();
            logger.debug(descFileName + " 文件压缩成功!");
        } catch (Exception e) {
            logger.debug("文件压缩失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 将目录压缩到ZIP输出流
     * @param dirPath 目录路径
     * @param fileDir 文件信息
     * @param zos 输出流
     */
    public static void zipDirectoryToZipFile(String dirPath, File fileDir, ZipOutputStream zos) {
        if (fileDir.isDirectory()) {
            File[] files = fileDir.listFiles();
            // 空的文件夹
            if (files.length == 0) {
                // 目录信息
                ZipEntry entry = new ZipEntry(getEntryName(dirPath, fileDir));
                try {
                    zos.putNextEntry(entry);
                    zos.closeEntry();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    // 如果是文件，则调用文件压缩方法
                    FileKit.zipFilesToZipFile(dirPath, files[i], zos);
                } else {
                    // 如果是目录，则递归调用
                    FileKit.zipDirectoryToZipFile(dirPath, files[i], zos);
                }
            }
        }
    }

    /**
     * 将文件压缩到ZIP输出流
     * @param dirPath 目录路径
     * @param file 文件
     * @param zos 输出流
     */
    public static void zipFilesToZipFile(String dirPath, File file, ZipOutputStream zos) {
        FileInputStream fin;
        ZipEntry entry;
        // 创建复制缓冲区
        byte[] buf = new byte[4096];
        int readByte;
        if (file.isFile()) {
            try {
                // 创建一个文件输入流
                fin = new FileInputStream(file);
                // 创建一个ZipEntry
                entry = new ZipEntry(getEntryName(dirPath, file));
                // 存储信息到压缩文件
                zos.putNextEntry(entry);
                // 复制字节到压缩文件
                while ((readByte = fin.read(buf)) != -1) {
                    zos.write(buf, 0, readByte);
                }
                zos.closeEntry();
                fin.close();
                System.out
                        .println("添加文件 " + file.getAbsolutePath() + " 到zip文件中!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取待压缩文件在ZIP文件中entry的名字，即相对于跟目录的相对路径名
     * @param dirPath 目录名
     * @param file entry文件名
     * @return
     */
    private static String getEntryName(String dirPath, File file) {
        String dirPaths = dirPath;
        if (!dirPaths.endsWith(File.separator)) {
            dirPaths = dirPaths + File.separator;
        }
        String filePath = file.getAbsolutePath();
        // 对于目录，必须在entry名字后面加上"/"，表示它将以目录项存储
        if (file.isDirectory()) {
            filePath += "/";
        }
        int index = filePath.indexOf(dirPaths);

        return filePath.substring(index + dirPaths.length());
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