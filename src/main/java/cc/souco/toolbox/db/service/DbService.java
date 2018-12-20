package cc.souco.toolbox.db.service;

import cc.souco.toolbox.db.dao.DbDao;
import cc.souco.toolbox.db.vo.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DbService {

    @Autowired
    private DbDao dbDao;

    public List<Map<String, Object>> testService(){
        return dbDao.queryList();
    }

    public List<String> findSynonyms(String schema){
        return dbDao.findSynonyms(schema);
    }

    public List<String> findSchemas(String schema){
        return dbDao.findSchemas(schema);
    }

    public List<Table> findTables(String schema){
        return dbDao.findTables(schema);
    }


}
