package com.ruoyi.ruoyigui.util;

import java.util.ArrayList;
import java.util.List;

public class ExpList {
    public static List<String> get_exp() {
        List<String> list = new ArrayList<>();
        list.add("Ruoyi dept edit sql注入");
        list.add("Ruoyi role export sql注入");
        list.add("Ruoyi role list sql注入");
        list.add("Ruoyi gen createTable sql盲注");
        list.add("Ruoyi monitor catch getNames thymeleaf注入");
        list.add("Ruoyi monitor catch getValue thymeleaf注入");
        list.add("Ruoyi monitor catch getKeys thymeleaf注入");
        list.add("Ruoyi localrefresh task thymeleaf注入");
        list.add("Ruoyi job snakeyaml rce漏洞");
        list.add("Ruoyi job jdbc template漏洞");
        list.add("Ruoyi job gentable template漏洞");
        list.add("Ruoyi download resource fileread漏洞");
        list.add("Ruoyi job fileread漏洞");
        list.add("Ruoyi job gentable jndi rce漏洞");
        list.add("Ruoyi shiro默认key");
        return list;
    }
}
