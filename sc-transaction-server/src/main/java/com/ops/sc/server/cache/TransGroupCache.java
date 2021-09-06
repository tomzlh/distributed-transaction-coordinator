package com.ops.sc.server.cache;

import com.ops.sc.common.model.TransGroup;
import com.ops.sc.server.dao.TransGroupDao;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TransGroupCache {

    @Resource
    private TransGroupDao transGroupDao;

    private Map<String, TransGroup> transGroupMap=new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
         List<TransGroup> groupList=transGroupDao.getAllValidTransGroup();
         if(groupList!=null&&!groupList.isEmpty()){
             for(TransGroup transGroup:groupList) {
                 transGroupMap.put(transGroup.getGroupId(),transGroup);
             }
         }
    }


    public TransGroup getTransGroup(String groupId){
        TransGroup transGroup=transGroupMap.get(groupId);
        if(transGroup==null){
            transGroup = transGroupDao.getTransGroupByGroupId(groupId);
            if(transGroup!=null) {
                transGroupMap.put(transGroup.getGroupId(), transGroup);
            }
        }
        return transGroup;
    }
}
