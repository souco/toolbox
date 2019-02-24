package cc.souco.toolbox.pack.service;

import cc.souco.toolbox.pack.UseSvnKit;
import com.beust.jcommander.internal.Lists;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNRevisionRange;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class SvnService {

    /**
     * 测试svn账户信息是否正确
     * @param username svn用户名
     * @param password svn密码
     * @param location svn项目路径
     * @return 是否测试通过
     */
    public boolean testSvnConfig(String username, String password, String location) {
        try {
            SVNLogClient logClient = getSvnLogClient(username, password);
            File[] files = {new File(location)};
            SVNRevision end = SVNRevision.create(-1L);
            logClient.doLog(files, null, end, true, true, 1, null);
        } catch (SVNAuthenticationException authException) {
            return false;
        } catch (SVNException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private SVNLogClient getSvnLogClient(String username, String password){
        BasicAuthenticationManager authManager = BasicAuthenticationManager.newInstance(username, password.toCharArray());
        SVNClientManager clientManager = SVNClientManager.newInstance(null, authManager);
        return clientManager.getLogClient();
    }

    private SVNLogClient getSvnLogClient(){
        BasicAuthenticationManager authManager = BasicAuthenticationManager.newInstance(UseSvnKit.SVN_NAME, UseSvnKit.SVN_PASSWORD.toCharArray());
        SVNClientManager clientManager = SVNClientManager.newInstance(null, authManager);
        return clientManager.getLogClient();
    }

    public List<SVNLogEntry> findSvnLog(Long startRevision, Long endRevision, int limit){
        List<SVNLogEntry> svnLogEntries = Lists.newArrayList();

        File[] files = {new File(UseSvnKit.PROJECT_DIR)};

        SVNRevision start = null;
        SVNRevision end = null;
        if (null != startRevision) {
            start = SVNRevision.create(0L);
        }
        if (null != endRevision) {
            end = SVNRevision.create(-1L);
        }

        try {
            getSvnLogClient().doLog(files, start, end, true, true, limit, svnLogEntry -> svnLogEntries.add(svnLogEntry));
        } catch (SVNException e) {
            e.printStackTrace();
        }

        return svnLogEntries;
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
}
