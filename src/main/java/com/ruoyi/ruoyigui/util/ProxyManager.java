package com.ruoyi.ruoyigui.util;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.Socket;

public class ProxyManager extends Authenticator {
    private static String proxyHost;
    private static String proxyPort;
    private static String proxyUser;
    private static String proxyPass;

    public ProxyManager() {
    }

    public static void setGlobalProxy(String type, String host, String port, String user, String pass) {
        proxyHost = host;
        proxyPort = port;
        proxyUser = user;
        proxyPass = pass;
        System.setProperty("proxySet", "true");
        if (type.equals("SOCKS")) {
            setSocksProxy();
        } else if (type.equals("HTTP")) {
            setHttpProxy();
        }

        Authenticator.setDefault(new ProxyManager());
    }

    public static void removeGlobalProxy() {
        proxyHost = null;
        proxyPort = null;
        proxyUser = null;
        proxyPass = null;
        System.setProperty("proxySet", "false");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");
        Authenticator.setDefault((Authenticator) null);
        ProxySelector.setDefault(ProxySelector.getDefault());
    }

    private static void setSocksProxy() {
        System.setProperty("socksProxyHost", proxyHost);
        System.setProperty("socksProxyPort", proxyPort);
        System.setProperty("java.net.socks.username", proxyUser);
        System.setProperty("java.net.socks.password", proxyPass);
    }

    private static void setHttpProxy() {
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        System.setProperty("http.proxyUserName", proxyUser);
        System.setProperty("http.proxyPassword", proxyPass);
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);
        System.setProperty("https.proxyUserName", proxyUser);
        System.setProperty("https.proxyPassword", proxyPass);
    }

    public static boolean validateProxy(String type, String host, String port, String user, String pass) {
        int portNum = Integer.parseInt(port);
        if (portNum >= 1 && portNum <= 65535) {
            try {
                Socket socket = new Socket();
                Throwable var7 = null;

                boolean var8;
                try {
                    socket.connect(new InetSocketAddress(host, portNum), 5000);
                    var8 = true;
                } catch (Throwable var18) {
                    var7 = var18;
                    throw var18;
                } finally {
                    if (socket != null) {
                        if (var7 != null) {
                            try {
                                socket.close();
                            } catch (Throwable var17) {
                                var7.addSuppressed(var17);
                            }
                        } else {
                            socket.close();
                        }
                    }

                }

                return var8;
            } catch (IOException var20) {
                return false;
            }
        } else {
            return false;
        }
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return proxyUser != null && proxyPass != null ? new PasswordAuthentication(proxyUser, proxyPass.toCharArray()) : null;
    }
}
