package com.ops.sc.server.dao;

import com.ops.sc.common.enums.BootMode;
import com.ops.sc.core.config.TransMessageConfig;


public class TransMsgDaoFactory {

    private static CommonTransMsgDao instance;

    public static CommonTransMsgDao getInstance() {
        return instance;
    }

    public static void registerBean(BootMode bootMode, CommonTransMsgDao commonTransMsgDao) {
        if (TransMessageConfig.getRunMode().equals(bootMode)) {
            instance = commonTransMsgDao;
        }
    }
}
