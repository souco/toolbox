package cc.souco.toolbox.pack.service;

import cc.souco.toolbox.common.FileKit;
import cc.souco.toolbox.common.StringKit;
import cc.souco.toolbox.pack.UseSvnKit;
import cc.souco.toolbox.pack.vo.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.beust.jcommander.internal.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class SvnService {

    private static Logger logger = LoggerFactory.getLogger(SvnService.class);

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
        }
        if (null == endRevision) {
            end = SVNRevision.create(-1L);
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

    public List<ProjectConfig> getProjectConfigs() {
        File projectsFile = null;
        try {
            projectsFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "projects.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String projectsStr = FileKit.toString(projectsFile);
        return JSONArray.parseArray(projectsStr, ProjectConfig.class);
    }

    public void saveProjectConfigs(List<ProjectConfig> configs) {
        File projectsFile = null;
        try {
            projectsFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "projects.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileKit.toFile(projectsFile, JSONArray.toJSONString(configs));
    }

    public SvnUser getSvnUser() {
        File userJsonFile = null;
        try {
            userJsonFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "svn.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String userJsonStr = FileKit.toString(userJsonFile);
        return JSON.parseObject(userJsonStr, SvnUser.class);
    }

    public void saveSvnUser(SvnUser user) {
        File svnUserFile = null;
        try {
            svnUserFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "svn.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileKit.toFile(svnUserFile, JSONArray.toJSONString(user));
    }

    public void packageUpdate(SvnLogInfo info, ProjectConfig config) {
        for (SvnFileInfo fileInfo : info.getFiles()) {
            if (SvnFileInfo.CHANGE_TYPE_DELETED == fileInfo.getChangeType() || SvnFileInfo.FILE_TYPE_DIR == fileInfo.getFileType()) {
                continue;
            }

            String path = StringKit.correctSlash(fileInfo.getPath());
            String compilePath;
            if (isStartWithJavaCodeDir(config, path)) {
                compilePath = config.getCompilePath() + File.separator + path.substring(getJavaCodeDirLength(config, path));
            } else {
                compilePath = path;
            }
            String absolutePath = config.getLocation() + File.separator + path;

            File from = new File(absolutePath);
            File to = new File(config.getOutputPath() + File.separator + compilePath);
            if (!to.getParentFile().exists()) {
                to.getParentFile().mkdirs();
            }

            // 复制文件
            try {
                Path copy = Files.copy(from.toPath(), to.toPath());
                logger.info("copy :" + copy);
            } catch (Exception e) {
                logger.info("copy error:" + from.toPath());
                continue;
            }
        }
    }

    private static boolean isStartWithJavaCodeDir(ProjectConfig config, String path){
        for (String dir : config.getJavaPath()) {
            dir = StringKit.correctSlash(StringKit.removeSlashAndBackslashPrefix(StringKit.removeSlashAndBackslashSuffix(dir)));
            if(path.startsWith(dir)){
                return true;
            }
        }
        return false;
    }

    private static int getJavaCodeDirLength(ProjectConfig config, String path){
        path = StringKit.correctSlash(StringKit.removeSlashAndBackslashPrefix(StringKit.removeSlashAndBackslashSuffix(path)));
        for (String dir : config.getJavaPath()) {
            if(path.startsWith(dir)){
                return dir.length();
            }
        }
        throw new RuntimeException();
    }
}
