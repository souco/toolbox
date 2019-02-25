package cc.souco.toolbox.pack;

import cc.souco.toolbox.common.DateKit;
import cc.souco.toolbox.common.FileKit;
import cc.souco.toolbox.pack.vo.FileVo;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Sets;
import org.apache.commons.lang3.StringUtils;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UseSvnKit {

    public static String SVN_URL = "";
    public static String SVN_URL_BASE = "";
    public static final String SVN_COMMAND = "svn info ";
    public static final String SVN_NAME = "souco";
    public static final String SVN_PASSWORD = "souco";
    public static final String SVN_URL_BASE_PATTERN = "Relative URL: ^";
    public static final String SVN_URL_PATTERN = "URL: ";
    public static final String PACKAGE_DIR = "D:\\work\\pack";
    public static Set<String> filePaths;
    public static final Long END_REVISION = -1L;  // -1 表示最后一个版本

    // 各个项目打包参数
    public static final String PROJECT_DIR = "E:\\Code\\xx";
    public static final List<String> PROJECT_JAVA_CODE_DIR_LIST = Lists.newArrayList("\\src1","\\src2");
    public static final String PROJECT_JAVA_CODE_COMPILE_DIR = "\\WebRoot\\WEB-INF\\classes";
    public static final Long START_REVERSION = 31748L;

    private static SVNRepository repository = null;

    public static void main(String[] args) {

        try {
            testSvnClient1();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*SvnService svnService = new SvnService();
        // svnService.findSvnLog(null, -1L, 10);

        Set<Long> revisions = Sets.newHashSet();
        revisions.add(30488L);
        revisions.add(30624L);
        List<SVNLogEntry> svnLogInRevisions = svnService.findSvnLogInRevisions(revisions, 100);
        for (SVNLogEntry entry : svnLogInRevisions) {
            System.out.println(entry);
        }*/
    }

    public static void testSvnClient1() throws IOException {
        try {
            setSvnPath();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ISVNAuthenticationManager authentication = SVNWCUtil.createDefaultAuthenticationManager(SVN_NAME, SVN_PASSWORD);
        SVNClientManager manager = SVNClientManager.newInstance(null, authentication);
        SVNLogClient logClient = manager.getLogClient();

        SVNRepositoryFactoryImpl.setup();
        DAVRepositoryFactory.setup();
        FSRepositoryFactory.setup();

        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(SVN_URL));
        } catch (SVNException e) {
            e.printStackTrace();
        }
        repository.setAuthenticationManager(authentication);

        try {
            filterCommitHistoryTest(repository);
            // listEntries(repository, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<FileVo> toCopyPathList = Lists.newArrayList();
        for (String path : filePaths) {
            FileVo file = new FileVo();
            path = path.replaceAll("/", "\\\\");
            if (isStartWithJavaCodeDir(path)) {
                String subDir = PROJECT_JAVA_CODE_COMPILE_DIR + path.substring(getJavaCodeDirLength(path));

                if (subDir.endsWith(".java")) {
                    subDir = subDir.replace(".java", ".class");
                }

                file.setRelationPath(subDir);
                file.setAbsolutePath(PROJECT_DIR + subDir);
                toCopyPathList.add(file);
            } else {
                file.setRelationPath(path);
                file.setAbsolutePath(PROJECT_DIR + path);
                toCopyPathList.add(file);
            }
        }

        String toPath = PACKAGE_DIR + File.separator + DateKit.dateToMilliStr();

        StringBuffer copyHistory = new StringBuffer();
        StringBuffer errorHistory = new StringBuffer();
        for (FileVo vo : toCopyPathList) {
            File from = new File(vo.getAbsolutePath());
            File to = new File(toPath + vo.getRelationPath());
            if (!to.getParentFile().exists()) {
                to.getParentFile().mkdirs();
            }

            // 如果是目录，创建目录
            if (from.isDirectory() && !from.exists()) {
                from.mkdir();
                copyHistory.append(from.getAbsolutePath()).append("\n");
                continue;
            }

            // 复制文件
            try {
                Files.copy(from.toPath(), to.toPath());
                copyHistory.append(from.getAbsolutePath()).append("\n");
            } catch (Exception e) {
                errorHistory.append(from.getAbsolutePath()).append("\n");
                continue;
            }
        }

        StringBuffer logText = new StringBuffer();
        logText.append("复制的文件记录：\n");
        logText.append(copyHistory).append("\n\n");
        logText.append("出错的文件记录：\n");
        logText.append(errorHistory).append("\n");
        FileKit.toFile(toPath + File.separator + "更新文件说明.txt", logText.toString());
        System.out.println("file://\t" + toPath);
        FileKit.openDirectory(toPath);
    }

    private static boolean isStartWithJavaCodeDir(String path){
        for (String dir : PROJECT_JAVA_CODE_DIR_LIST) {
            if(path.startsWith(dir)){
                return true;
            }
        }
        return false;
    }

    private static int getJavaCodeDirLength(String path){
        for (String dir : PROJECT_JAVA_CODE_DIR_LIST) {
            if(path.startsWith(dir)){
                return dir.length();
            }
        }
        throw new RuntimeException();
    }

    public static void listEntries(SVNRepository repository, String path) throws SVNException {
        System.out.println(path);
        Collection entries = repository.getDir(path, -1, null, (Collection) null);
        Iterator iterator = entries.iterator();
        while (iterator.hasNext()) {
            SVNDirEntry entry = (SVNDirEntry) iterator.next();
            System.out.println("/" + (path.equals("") ? "" : path + "/") + entry.getName() +
                    " ( author: '" + entry.getAuthor() + "'; revision: " + entry.getRevision() +
                    "; date: " + entry.getDate() + ")");
            if (entry.getKind() == SVNNodeKind.DIR) {
                listEntries(repository, (path.equals("")) ? entry.getName() : path + "/" + entry.getName());
            }
        }
    }

    public static void setSvnPath() throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(SVN_COMMAND + PROJECT_DIR);
        InputStream is = p.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("GBK")));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);

            if (StringUtils.isNotBlank(line)) {
                if (line.startsWith(SVN_URL_PATTERN)) {
                    SVN_URL = line.substring(line.indexOf(SVN_URL_PATTERN) + SVN_URL_PATTERN.length());
                    System.out.println("SVN_URL: " + SVN_URL);
                } else if (line.startsWith(SVN_URL_BASE_PATTERN)) {
                    SVN_URL_BASE = line.substring(line.indexOf(SVN_URL_BASE_PATTERN) + SVN_URL_BASE_PATTERN.length());
                    System.out.println("SVN_URL_BASE: " + SVN_URL_BASE);
                }
            }
        }
        p.waitFor();
        is.close();
        reader.close();
    }

    /**
     * 获取svn提交记录
     *
     * @param repository
     * @throws Exception
     */
    public static void filterCommitHistoryTest(SVNRepository repository) throws Exception {
        // 过滤条件
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        final Date begin = format.parse("2018-12-01");
        final Date end = format.parse("2018-12-18");
        final String author = "";
        final List<String> history = new ArrayList<String>();
        //String[] 为过滤的文件路径前缀，为空表示不进行过滤
        repository.log(new String[]{""},
                START_REVERSION,
                END_REVISION,
                true,
                true,
                new ISVNLogEntryHandler() {
                    @Override
                    public void handleLogEntry(SVNLogEntry svnlogentry) throws SVNException {
                        //依据提交时间进行过滤
                        /*if (svnlogentry.getDate().after(begin) && svnlogentry.getDate().before(end)) {
                            // 依据提交人过滤
                            if (!"".equals(author)) {
                                if (author.equals(svnlogentry.getAuthor())) {
                                    fillResult(svnlogentry);
                                }
                            } else {
                                fillResult(svnlogentry);
                            }
                        }*/
                        fillResult(svnlogentry);
                    }

                    public void fillResult(SVNLogEntry svnlogentry) {
                        //getChangedPaths为提交的历史记录MAP key为文件名，value为文件详情
                        history.addAll(svnlogentry.getChangedPaths().keySet());
                    }
                });

        filePaths = Sets.newHashSet();
        for (String path : history) {
            System.out.println(path);

            filePaths.add(path.substring(SVN_URL_BASE.length()));
        }
    }
}
