package cc.souco.toolbox.pack.service;

import cc.souco.toolbox.pack.UseSvnKit;
import com.beust.jcommander.internal.Lists;
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

public class SvnService {

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
