package cc.souco.toolbox.pack;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;

public class UseSvnKit {

    public static final String SVN_URL = "http://localhost:8888/testSvn";
    public static final String FILE_URL = "G:\\code\\thisCode";
    public static final String SVN_COMMAND = "svn info";
    public static final String SVN_NAME = "souco";
    public static final String SVN_PASSWORD = "souco";
    public static final Long startVersion = 0L;
    public static final Long endVersion = -1L;

    public static void main(String[] args) {

        testSvnClient2();
    }

    public static void testSvnClient1() {
        ISVNAuthenticationManager authentication = SVNWCUtil.createDefaultAuthenticationManager(SVN_NAME, SVN_PASSWORD);
        SVNClientManager manager = SVNClientManager.newInstance(null, authentication);
        SVNLogClient logClient = manager.getLogClient();

        SVNURL svnurl = null;
        try {
            svnurl = SVNURL.parseURIDecoded(SVN_URL);
        } catch (SVNException e) {
            e.printStackTrace();
        }

        // logClient.doLog(svnurl, null, null, null ,null, null, null, null, null);

    }

    public static void testSvnClient2() {
        DAVRepositoryFactory.setup();
        SVNRepository repository;
        try {
            // repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(SVN_URL));
            // SVNURL svnurl = SVNURL.fromFile(new File(FILE_URL));
            // repository = SVNRepositoryFactory.create(svnurl);
            // ISVNAuthenticationManager authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(SVN_NAME, SVN_PASSWORD);
            // repository.setAuthenticationManager(authenticationManager);

            // Collection logEntrys = null;
            // logEntrys = repository.log(new String[]{""}, null, startVersion, endVersion, true, true);

            // listEntries(repository, "");

            // Process p = Runtime.getRuntime().exec(SVN_COMMAND + FILE_URL);
            /*Process p = Runtime.getRuntime().exec("ls G:\\code");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            System.out.println(sb.toString());*/

            Process p = Runtime.getRuntime().exec("svn info G:\\codeWork\\jeeplus_base");
            InputStream is = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("GBK")));
            String line;
            while((line = reader.readLine())!= null){
                System.out.println(line);
            }
            p.waitFor();
            is.close();
            reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void listEntries(SVNRepository repository, String path) throws SVNException {
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
}
