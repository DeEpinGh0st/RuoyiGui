package com.ruoyi.ruoyigui.util;

import com.ruoyi.ruoyigui.exploit.*;
import com.ruoyi.ruoyigui.common.BasePayload;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Tools {
    private static final Map<String, BasePayload> payloadMap = new HashMap<>();

    static {
        payloadMap.put("Ruoyi dept edit sql注入", new ruoyi_system_dept_sql());
        payloadMap.put("Ruoyi role export sql注入", new ruoyi_system_role1_sql());
        payloadMap.put("Ruoyi role list sql注入", new ruoyi_system_role_sql());
        payloadMap.put("Ruoyi gen createTable sql盲注", new ruoyi_gen_createTable_sql());
        payloadMap.put("Ruoyi monitor catch getNames thymeleaf注入", new ruoyi_monitor_catch_getNames_thymeleaf());
        payloadMap.put("Ruoyi monitor catch getValue thymeleaf注入", new ruoyi_monitor_catch_getValue_thymeleaf());
        payloadMap.put("Ruoyi monitor catch getKeys thymeleaf注入", new ruoyi_monitor_catch_getKeys_thymeleaf());
        payloadMap.put("Ruoyi localrefresh task thymeleaf注入", new ruoyi_localrefresh_task_thymeleaf());
        payloadMap.put("Ruoyi job snakeyaml rce漏洞", new ruoyi_job_snakeyaml_rce());
        payloadMap.put("Ruoyi job jdbc template漏洞", new ruoyi_job_jdbc_template());
        payloadMap.put("Ruoyi job gentable template漏洞", new ruoyi_job_gentable_template());
        payloadMap.put("ruoyi_job_gentable_template", new ruoyi_job_jdbc_template());
        payloadMap.put("Ruoyi download resource fileread漏洞", new ruoyi_download_resource_fileread());
        payloadMap.put("Ruoyi job fileread漏洞", new ruoyi_job_fileread());
        payloadMap.put("Ruoyi job gentable jndi rce漏洞", new ruoyi_job_gentable_jndi_rce());
        payloadMap.put("Ruoyi shiro默认key", new ruoyi_shiro_default_key());
    }

    public static BasePayload getPayload(String select) {
        return payloadMap.get(select);
    }

    public static boolean checkTheURL(String weburl) {
        return weburl.startsWith("http");
    }

    public static String addTheURL(String weburl) {
        if (!weburl.startsWith("http")) {
            weburl = "http" + "://" + weburl;
        }
        return weburl;
    }

    public static List<String> read_file(String file) {
        List<String> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String url;
            while ((url = br.readLine()) != null) {
                url = addTheURL(url);
                list.add(url);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String getRandomString(int length) {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }

        return sb.toString();
    }

    public static String extractErrorMessage(String response) {
        String errorMessage = "";
        Pattern pattern = Pattern.compile("XPATH syntax error: '(.*?)'");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            errorMessage = matcher.group(1);
        }

        return errorMessage;
    }

    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding not supported", e);
        }
    }


    public static String fullUrlEncode(String url) {
        StringBuilder encodedUrl = new StringBuilder();

        for (char c : url.toCharArray()) {
            encodedUrl.append("%").append(String.format("%02X", (int) c));
        }

        return encodedUrl.toString();
    }

    public static String getName() {
        return "hei_" + (int) (Math.random() * 10000.0);
    }

    public static String bytes2HexString(byte[] b) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < b.length; ++i) {
            result.append(String.format("%02X", b[i]));
        }

        return result.toString();
    }

    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];

        for (int i = 0; i < l; ++i) {
            ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }

        return ret;
    }

    public static String string2HexString(String strPart) {
        try {
            return bytes2HexString(strPart.getBytes());
        } catch (Exception var2) {
            return "";
        }
    }

    public static String hexString2String(String src) {
        try {
            byte[] bts = hexString2Bytes(src);
            return new String(bts);
        } catch (Exception var2) {
            return src;
        }
    }
}
