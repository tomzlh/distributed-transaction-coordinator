package com.ops.sc.ta.clone.rollback;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("imageRecoverFactory")
public class ImageRecoverFactory implements InitializingBean {

    @Autowired
    private List<ImageRecover> recoverList;

    private Map<String, ImageRecover> recoverMap = new ConcurrentHashMap<>();

    public ImageRecover getRecover(String sqlType) {
        ImageRecover recover = recoverMap.get(sqlType);
        if (recover != null) {
            return recover;
        }

        throw new IllegalArgumentException("Not support sqlType=" + sqlType);
    }

    @Override
    public void afterPropertiesSet() {
        for (ImageRecover recover : recoverList) {
            recoverMap.put(recover.getSqlType().name(), recover);
        }
    }
}