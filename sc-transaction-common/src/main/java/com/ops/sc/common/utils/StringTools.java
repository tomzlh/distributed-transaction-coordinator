package com.ops.sc.common.utils;

import com.ops.sc.common.constant.Constants;
import com.ops.sc.common.constant.EtcdConstants;
import com.ops.sc.common.reg.zk.ZookeeperRegistryCenter;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;


public class StringTools {

    private static final String APPNAME_SPLITTER = "/";

    private static final Integer APPNAME_POSITION = 3;

    private static final Integer UNIQUE_ID_POSITION = 4;

    public static String makeRegisterAppName(String appName) {
        if(!appName.startsWith(ZookeeperRegistryCenter.PREFIX)){
            return ZookeeperRegistryCenter.PREFIX + appName+APPNAME_SPLITTER+UUIDGenerator.generateUUID();
        }
        return appName+APPNAME_SPLITTER+UUIDGenerator.generateUUID();
    }

    public static String getAppNameOnly(String appNameUnique) {
        return appNameUnique.split(APPNAME_SPLITTER)[APPNAME_POSITION];
    }

    public static String getUniqueIdOnly(String appNameUnique) {
        return appNameUnique.split(APPNAME_SPLITTER)[UNIQUE_ID_POSITION];
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj.getClass().isPrimitive()) {
            return String.valueOf(obj);
        }
        if (obj instanceof String) {
            return (String)obj;
        }
        if (obj instanceof Number || obj instanceof Character || obj instanceof Boolean) {
            return String.valueOf(obj);
        }
        if (obj instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(obj);
        }
        if (obj instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            if (!((Collection)obj).isEmpty()) {
                for (Object o : (Collection)obj) {
                    sb.append(toString(o)).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Map) {
            Map<Object, Object> map = (Map)obj;
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            if (!map.isEmpty()) {
                map.forEach((key, value) -> {
                    sb.append(toString(key)).append("->")
                            .append(toString(value)).append(",");
                });
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("}");
            return sb.toString();
        }
        StringBuilder sb = new StringBuilder();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            sb.append(field.getName());
            sb.append("=");
            try {
                Object f = field.get(obj);
                if (f.getClass() == obj.getClass()) {
                    sb.append(f.toString());
                } else {
                    sb.append(toString(f));
                }
            } catch (Exception e) {
            }
            sb.append(";");
        }
        return sb.toString();
    }

    public static boolean isEmpty(String str) {
        int length;

        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlank(String str) {
        int length;

        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Bytes to int int.
     *
     * @param bytes  the bytes
     * @param offset the offset
     * @return the int
     */
    public static int bytesToInt(byte[] bytes, int offset) {
        int ret = 0;
        for (int i = 0; i < 4 && i + offset < bytes.length; i++) {
            ret <<= 8;
            ret |= (int)bytes[i + offset] & 0xFF;
        }
        return ret;
    }

    /**
     * Int to bytes.
     *
     * @param i      the
     * @param bytes  the bytes
     * @param offset the offset
     */
    public static void intToBytes(int i, byte[] bytes, int offset) {
        bytes[offset] = (byte)((i >> 24) & 0xFF);
        bytes[offset + 1] = (byte)((i >> 16) & 0xFF);
        bytes[offset + 2] = (byte)((i >> 8) & 0xFF);
        bytes[offset + 3] = (byte)(i & 0xFF);
    }
}
