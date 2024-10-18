package com.ruoyi.ruoyigui.common;

import com.ruoyi.ruoyigui.entity.Result;

public interface BasePayload {
    Result checkVUL(String url) throws Exception;

    Result exeVUL(String url, String cmd) throws Exception;

    Result getShell(String url) throws Exception;
}
