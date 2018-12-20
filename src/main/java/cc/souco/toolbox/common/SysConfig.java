package cc.souco.toolbox.common;

import com.beust.jcommander.internal.Lists;

import java.io.File;
import java.util.List;

public interface SysConfig {
    // 数据库相关
    String DRIVER = "oracle.jdbc.driver.OracleDriver";
    String URL = "jdbc:oracle:thin:localhost:1521:orcl";
    String USERNAME = "database_user_name";
    String PASSWORD = "database_password";
    List<String> SCHEMAS = Lists.newArrayList("SCHEMA1", "SCHEMA2");

    // 本地路径相关
    String BASE_PATH = "D:" + File.separator + "ProjectData" + File.separator;

    // DB 导出相关
    String DATABASE_TEMPLATE_BASIC = "databaseTemplateBasic.ftl";
    String DATABASE_TEMPLATE_BASIC_SINGLE = "databaseTemplateBasicSingle.ftl";
    String DATABASE_TEMPLATE_SIMPLE = "databaseTemplateSimpleSingle.ftl";
    String DATABASE_TEMPLATE_TABLE_INFO = "databaseTemplateTableInfo.ftl";

    List<String> EXCLUDES = Lists.newArrayList("aa", "bb", "cc");
}
