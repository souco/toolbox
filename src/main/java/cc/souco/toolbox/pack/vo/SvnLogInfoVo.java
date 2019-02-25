package cc.souco.toolbox.pack.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.beust.jcommander.internal.Lists;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class SvnLogInfoVo {
    private Long revision;
    private String remark;
    private String author;
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date date;
    List<SvnFileInfo> files;


    public SvnLogInfoVo() {
    }

    public SvnLogInfoVo(SVNLogEntry entry) {
        this.revision = entry.getRevision();
        this.remark = entry.getMessage();
        this.author = entry.getAuthor();
        this.date = entry.getDate();

        files = Lists.newArrayList();
        for (Map.Entry<String, SVNLogEntryPath> item : entry.getChangedPaths().entrySet()) {
            SVNLogEntryPath path = item.getValue();
            int fileType = path.getKind().getID();
            if (fileType != SvnFileInfo.FILE_TYPE_DIR && fileType != SvnFileInfo.FILE_TYPE_FILE) {
                fileType = SvnFileInfo.FILE_TYPE_UNKNOWN;
            }

            char type = path.getType();
            Integer changeType;
            if (SVNLogEntryPath.TYPE_ADDED == type) {
                changeType = SvnFileInfo.CHANGE_TYPE_ADDED;
            } else if (SVNLogEntryPath.TYPE_DELETED == type) {
                changeType = SvnFileInfo.CHANGE_TYPE_DELETED;
            } else if (SVNLogEntryPath.TYPE_REPLACED == type) {
                changeType = SvnFileInfo.CHANGE_TYPE_REPLACED;
            } else {
                changeType = SvnFileInfo.CHANGE_TYPE_MODIFIED;
            }

            files.add(new SvnFileInfo(path.getPath(), changeType, fileType));
        }
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<SvnFileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<SvnFileInfo> files) {
        this.files = files;
    }
}
