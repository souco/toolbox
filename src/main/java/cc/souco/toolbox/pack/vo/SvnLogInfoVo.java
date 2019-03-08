package cc.souco.toolbox.pack.vo;

import java.util.List;

public class SvnLogInfoVo extends SvnLogInfo{
    private List<SvnLogInfo> infos;

    public List<SvnLogInfo> getInfos() {
        return infos;
    }

    public void setInfos(List<SvnLogInfo> infos) {
        this.infos = infos;
    }
}
