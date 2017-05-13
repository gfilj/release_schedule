package com.netease.engine.vo;


/**
 * Created by wangmaocheng on 2016/11/7.
 */
public class ProxyIpInfo {
    private String ip;
    private int port;
    private String username;
    private String password;
    private int anonymous = -1;

    private long expire = -1;

    public ProxyIpInfo() {

    }

    public ProxyIpInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public ProxyIpInfo(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(int anonymous) {
        this.anonymous = anonymous;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProxyIpInfo) {
            ProxyIpInfo proxyIpInfo = (ProxyIpInfo) obj;
            return ip.equals(proxyIpInfo.getIp()) && port == proxyIpInfo.getPort();
        }
        return super.equals(obj);
    }
}
