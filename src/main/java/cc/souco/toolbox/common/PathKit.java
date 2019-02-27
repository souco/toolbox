package cc.souco.toolbox.common;

import org.springframework.boot.system.ApplicationHome;

public class PathKit {

    public static String getSpringBootJarParentPath(){
        return new ApplicationHome(StringKit.class).getSource().getParentFile().getAbsolutePath();
    }
}
