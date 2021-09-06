package com.ops.sc.common.utils;

import java.net.*;
import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


public class InetUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(InetUtil.class);

    private static final String POD_IP = "POD_IP";

    /**
     * 获取本地真实IP
     *
     * @return
     */
    public static String getHostIp() {
        String podIp = System.getenv(POD_IP);
        if (!Strings.isNullOrEmpty(podIp)) {
            return podIp;
        }
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip instanceof Inet4Address && !ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.error("Get client ip failed!", e);
        }
        return null;
    }

    /**
     * 校验地址
     *
     * @param address
     */
    public static void checkAddrValid(InetSocketAddress address) {
        if (null == address.getHostName() || 0 == address.getPort()) {
            throw new IllegalArgumentException("invalid address:" + address);
        }
    }

    public static String toStringAddress(SocketAddress address) {
        if (address == null) {
            return StringUtils.EMPTY;
        }
        return toIpAddress((InetSocketAddress) address);
    }

    public static String toIpAddress(SocketAddress address) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        return inetSocketAddress.getAddress().getHostAddress()+":"+inetSocketAddress.getPort();
    }

    public static InetSocketAddress toInetSocketAddress(String address) {
        int i = address.indexOf(':');
        String host;
        int port;
        if (i > -1) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
            port = 0;
        }
        return new InetSocketAddress(host, port);
    }
}
