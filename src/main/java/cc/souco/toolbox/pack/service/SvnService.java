package cc.souco.toolbox.pack.service;

import cc.souco.toolbox.common.DateKit;
import cc.souco.toolbox.common.FileKit;
import cc.souco.toolbox.common.PathKit;
import cc.souco.toolbox.common.StringKit;
import cc.souco.toolbox.pack.UseSvnKit;
import cc.souco.toolbox.pack.vo.ProjectConfig;
import cc.souco.toolbox.pack.vo.SvnFileInfo;
import cc.souco.toolbox.pack.vo.SvnLogInfo;
import cc.souco.toolbox.pack.vo.SvnUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.beust.jcommander.internal.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class SvnService {

    private static Logger logger = LoggerFactory.getLogger(SvnService.class);

    @Value("${user.file.prefix}")
    private String userFilePrefix;
    /**
     * 测试svn账户信息是否正确
     * @param user svn用户信息
     * @return 是否测试通过
     */
    public boolean testSvnConfig(SvnUser user) {
        String location = user.getLocation();
        try {
            File file = new File(location);
            if (!file.exists()) {
                throw new RuntimeException("SVN工作目录不存在！");
            } else if (!file.isDirectory()) {
                location = file.getParentFile().getAbsolutePath();
            }
        } catch (Exception e) {
            throw new RuntimeException("SVN工作目录不存在！");
        }

        try {
            SVNClientManager manager = getSvnClientManager(user.getUsername(), user.getPassword());
            manager.getWCClient().doInfo(new File(user.getLocation()), SVNRevision.HEAD);
        } catch (SVNAuthenticationException authException) {
            throw new RuntimeException("用户名或密码不正确！");
        } catch (SVNException e) {
            throw new RuntimeException(location + "不是SVN工作目录！");
        }
        return true;
    }

    private SVNLogClient getSvnLogClient(String username, String password){
        BasicAuthenticationManager authManager = BasicAuthenticationManager.newInstance(username, password.toCharArray());
        SVNClientManager clientManager = SVNClientManager.newInstance(null, authManager);
        return clientManager.getLogClient();
    }

    private SVNClientManager getSvnClientManager(String username, String password){
        BasicAuthenticationManager authManager = BasicAuthenticationManager.newInstance(username, password.toCharArray());
        return SVNClientManager.newInstance(null, authManager);
    }

    @Deprecated
    private SVNLogClient getSvnLogClient(){
        BasicAuthenticationManager authManager = BasicAuthenticationManager.newInstance(UseSvnKit.SVN_NAME, UseSvnKit.SVN_PASSWORD.toCharArray());
        SVNClientManager clientManager = SVNClientManager.newInstance(null, authManager);
        return clientManager.getLogClient();
    }

    public List<SvnLogInfo> findSvnLog(SvnUser user, String location, Long startRevision, Long endRevision, int limit){
        List<SvnLogInfo> LogInfos = Lists.newArrayList();

        File[] files = {new File(location)};

        SVNClientManager svnClientManager = getSvnClientManager(user.getUsername(), user.getPassword());
        SVNInfo svnInfo;
        try {
            svnInfo = svnClientManager.getWCClient().doInfo(new File(location), SVNRevision.HEAD);
        } catch (SVNException e) {
            e.printStackTrace();
            throw new RuntimeException("获取SVN信息失败！");
        }

        SVNRevision start = null;
        SVNRevision end;
        if (null != startRevision) {
            start = SVNRevision.create(startRevision);
        } else {
            start = SVNRevision.create(new Date());
        }
        if (null == endRevision) {
            // 当前分支最新的版本号
            // end = svnInfo.getCommittedRevision();
            // end = SVNRevision.create(-1L);
            end = SVNRevision.create(0);
        } else {
            end = SVNRevision.create(endRevision);
        }

        try {
            String finalProjectPrePath = svnInfo.getPath();
            svnClientManager.getLogClient().doLog(files, start, end, true, true, limit, svnLogEntry -> LogInfos.add(new SvnLogInfo(finalProjectPrePath, svnLogEntry)));
        } catch (SVNException e) {
            e.printStackTrace();
        }

        return LogInfos;
    }

    public List<SVNLogEntry> findSvnLogInRevisions(Set<Long> revisions, Integer limit){
        List<SVNLogEntry> svnLogEntries = Lists.newArrayList();

        File[] files = {new File(UseSvnKit.PROJECT_DIR)};

        if (revisions.isEmpty()) {
            return Lists.newArrayList();
        }

        Collection<SVNRevisionRange> revisionRanges = Lists.newArrayList();
        for (Long revisionNumber : revisions) {
            SVNRevision revision = SVNRevision.create(revisionNumber);
            SVNRevisionRange range = new SVNRevisionRange(revision, revision);
            revisionRanges.add(range);
        }

        try {
            getSvnLogClient().doLog(files, revisionRanges, SVNRevision.HEAD, true, true, true, limit, null, svnLogEntry -> svnLogEntries.add(svnLogEntry));
        } catch (SVNException e) {
            e.printStackTrace();
        }

        return svnLogEntries;
    }

    /**
     * 从文件中读取项目配置信息，如果文件不存在，返回空列表
     * @return 文件配置信息列表
     */
    public ProjectConfig getCurProjectConfigs(Integer projectSelect) {
        List<ProjectConfig> configs = getProjectConfigs();
        if (configs == null || configs.isEmpty()) {
            throw new RuntimeException("请先选择或配置项目信息！");
        }
        return configs.get(projectSelect);
    }

    /**
     * 从文件中读取项目配置信息，如果文件不存在，返回空列表
     * @return 文件配置信息列表
     */
    public List<ProjectConfig> getProjectConfigs() {
        String filepath = PathKit.getSpringBootJarParentPath() + File.separator + userFilePrefix + "projects.json";
        File file = FileKit.newFileSafety(filepath);
        if (!file.exists()) {
            return getProjectConfigsFromResource();
        }
        return JSONArray.parseArray(FileKit.toString(file), ProjectConfig.class);
    }

    /**
     * 从文件中读取项目配置信息，如果文件不存在，返回空列表
     * @return 文件配置信息列表
     */
    public List<ProjectConfig> getProjectConfigsFromResource() {
        String projectsStr;
        try {
            InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream("projects.json");
            projectsStr = FileKit.inputStream2String(resourceStream);
            if (resourceStream != null) {
                resourceStream.close();
            }
        } catch (Exception e) {
            return Lists.newArrayList();
        }
        return JSONArray.parseArray(projectsStr, ProjectConfig.class);
    }

    public void saveProjectConfigs(List<ProjectConfig> configs) {
        String filepath = PathKit.getSpringBootJarParentPath() + File.separator + userFilePrefix + "projects.json";
        File file = FileKit.newFileSafety(filepath);
        FileKit.toFile(file, JSONArray.toJSONString(configs));
    }

    /**
     * 从文件中读取SVN配置信息，如果文件不存在，返回新建对象
     * @return
     */
    public SvnUser getSvnUser() {
        String filepath = PathKit.getSpringBootJarParentPath() + File.separator + userFilePrefix + "svn.json";
        File file = FileKit.newFileSafety(filepath);
        if (!file.exists()) {
            return getSvnUserFromResource();
        }
        return JSON.parseObject(FileKit.toString(file), SvnUser.class);
    }

    /**
     * 从文件中读取SVN配置信息，如果文件不存在，返回新建对象
     * @return
     */
    public SvnUser getCurSvnUser() {
        SvnUser user = getSvnUser();
        if (user == null || StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
            throw new RuntimeException("请先选择或配置SVN账户信息！");
        }
        return user;
    }

    /**
     * 从文件中读取SVN配置信息，如果文件不存在，返回新建对象
     * @return
     */
    public SvnUser getSvnUserFromResource() {
        String userJsonString;
        try {
            InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream("svn.json");
            userJsonString = FileKit.inputStream2String(resourceStream);
            if (resourceStream != null) {
                resourceStream.close();
            }
        } catch (Exception e) {
            return new SvnUser();
        }
        return JSON.parseObject(userJsonString, SvnUser.class);
    }

    public void saveSvnUser(SvnUser user) {
        String svnFilename = PathKit.getSpringBootJarParentPath() + File.separator + userFilePrefix + "svn.json";
        File svnUserFile = FileKit.newFileSafety(svnFilename);
        FileKit.toFile(svnUserFile, JSONArray.toJSONString(user));
    }

    /**
     * 打包更新包
     * @param info 打包的版本及文件信息
     * @param config 打包的项目配置信息
     */
    public void packageUpdate(SvnLogInfo info, ProjectConfig config) {
        // 绝对路径
        String dateTimeProjectName = DateKit.dateToMilliStr() + "_" + config.getName();
        String finallyOutputBaseDir = config.getOutputPath() + File.separator + dateTimeProjectName;

        StringBuilder all = new StringBuilder();
        StringBuilder failure = new StringBuilder();
        StringBuilder delete = new StringBuilder();
        StringBuilder success = new StringBuilder();

        for (SvnFileInfo fileInfo : info.getFiles()) {
            // 文件夹，略过（文件夹会在创建子文件的时候创建）
            if (SvnFileInfo.FILE_TYPE_DIR == fileInfo.getFileType()) {
                continue;
            }

            String path = StringKit.trimAndCorrectSlash(fileInfo.getPath());

            // 在项目根路径下的相对路径
            String fromRelationPath;
            List<File> files = Lists.newArrayList();
            String fromPath;
            if (config.isStartWithJavaCodeDir(path)) {
                fromRelationPath = config.getCompilePath() + File.separator + StringKit.trimSlash(path.substring(config.getJavaCodeDirLength(path)));
                if (path.endsWith(".java")) {
                    fromRelationPath = fromRelationPath.replace(".java", ".class");

                    // 检测java内部类导致的多个字节码文件
                    fromPath = config.getLocation() + File.separator + fromRelationPath;
                    File file = new File(fromPath);
                    String filename = file.getName();
                    String classFilenamePrefix = filename.substring(0, filename.lastIndexOf(".")) + "$";
                    File[] subClassFiles = file.getParentFile().listFiles((dir, name) -> name.startsWith(classFilenamePrefix));
                    if (subClassFiles != null && subClassFiles.length > 0) {
                        files.addAll(Lists.newArrayList(subClassFiles));
                    }
                }
            } else {
                fromRelationPath = path;
            }
            files.add(0, new File(config.getLocation() + File.separator + fromRelationPath));

            // 删除的文件，记录，然后跳过
            if (SvnFileInfo.CHANGE_TYPE_DELETED == fileInfo.getChangeType()) {
                delete.append(fileInfo.getChangeTypeStr()).append(" ").append(fromRelationPath).append("\r\n");
                continue;
            }

            for (File file : files) {
                try {
                    FileKit.copyFile(file, config.getLocation(), finallyOutputBaseDir);
                    logger.info("copy success: " + fromRelationPath);
                    success.append(fileInfo.getChangeTypeStr()).append(" ").append(file.getAbsolutePath().substring(config.getLocation().length())).append("\r\n");
                } catch (IOException e) {
                    logger.error("copy error: " + fromRelationPath);
                    failure.append(fileInfo.getChangeTypeStr()).append(" ").append(fromRelationPath).append("\r\n");
                }
            }
        }

        // 生成打包信息
        all.append("最新版本号：").append(info.getRevision()).append("\r\n")
                .append("最新提交人：").append(info.getAuthor()).append("\r\n")
                .append("最后提交时间：").append(DateKit.format(info.getDate(), DateKit.DATE_TIME_FORMAT)).append("\r\n")
                .append("最后提交说明：").append(info.getRemark()).append("\r\n")
                .append("更新包打包时间：").append(DateKit.format(DateKit.DATE_TIME_FORMAT)).append("\r\n");

        all.append("\r\n").append("打包失败的文件：").append("\r\n");
        if (failure.length() > 0) {
            all.append(failure);
        } else {
            all.append("无").append("\r\n");
        }

        all.append("\r\n").append("已被删除的文件：").append("\r\n");
        if (delete.length() > 0) {
            all.append(delete);
        } else {
            all.append("无").append("\r\n");
        }

        all.append("\r\n").append("打包成功的文件：").append("\r\n");
        if (success.length() > 0) {
            all.append(success);
        } else {
            all.append("无").append("\r\n");
        }

        FileKit.toFile(finallyOutputBaseDir + File.separator + "更新说明.txt", all.toString());

        // 如果配置了打包后打开文件夹，则打开文件夹
        if (config.getOpenDir()) {
            FileKit.openDirectory(finallyOutputBaseDir);
        }

        // 更新最后打包记录
        updateLastPackProjectConfig(config);
    }

    /**
     * 更新最后打包的配置信息
     * @param lastPackConfig 最后一次打包的配置
     */
    private void updateLastPackProjectConfig(ProjectConfig lastPackConfig) {
        List<ProjectConfig> configs = getProjectConfigs();
        boolean hashMark = false;
        for (ProjectConfig config : configs) {
            if (!hashMark && config.equals(lastPackConfig)) {
                config.setIsLastPack(true);
                hashMark = true;
            } else {
                config.setIsLastPack(false);
            }
        }
        saveProjectConfigs(configs);
    }
}
