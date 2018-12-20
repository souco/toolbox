package cc.souco.toolbox.db.dao;

import cc.souco.toolbox.db.vo.Table;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Mapper
@Service
public interface DbDao {

    List<Map<String, Object>> queryList();

    List<String> findSynonyms(@Param("schema") String schema);

    List<String> findSchemas(@Param("schema") String schema);

    List<Table> findTables(@Param("schema") String schema);
}
