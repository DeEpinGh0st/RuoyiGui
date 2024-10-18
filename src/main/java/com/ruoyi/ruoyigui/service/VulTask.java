package com.ruoyi.ruoyigui.service;

import com.ruoyi.ruoyigui.common.BasePayload;
import com.ruoyi.ruoyigui.entity.Result;
import com.ruoyi.ruoyigui.util.Tools;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.swing.*;

public class VulTask extends Task<Void> {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    private final ComboBox<String> comboBox;
    private final TextField url_txt;
    private final TextField cmd_txt;
    private final TextArea res_txt;

    public VulTask(ComboBox<String> comboBox, TextField url_txt, TextField cmd_txt, TextArea res_txt) {
        this.comboBox = comboBox;
        this.url_txt = url_txt;
        this.cmd_txt = cmd_txt;
        this.res_txt = res_txt;
    }

    public void logcmd(String info) {
        Platform.runLater(() -> this.res_txt.appendText(info + "\n"));
    }

    private void showAlert(String content) {
        Platform.runLater(() -> {
            this.alert.setTitle("提示:");
            this.alert.setHeaderText("信息");
            this.alert.setContentText(content);
            this.alert.showAndWait();
        });
    }

    public void gen_sql_vul() throws Exception {
        String url = this.url_txt.getText();
        String version = this.comboBox.getSelectionModel().getSelectedItem();
        String sql = this.cmd_txt.getText();
        String res;
        if (version.startsWith("Ruoyi gen createTable sql")) {
            logcmd("[+]盲注较消耗时间，请等待！\n");
        }
        if (version.startsWith("ALL")) {
            showAlert("请选择具体漏洞！");
            return;
        }
        BasePayload bp = Tools.getPayload(version);
        Result vul = bp.exeVUL(url, sql);
        if (vul.isRes()) {
            res = vul.getPayload();
            logcmd("[+] " + res);
        } else {
            showAlert("命令执行失败!");
        }
    }

    @Override
    protected Void call() throws Exception {
        gen_sql_vul();
        return null;
    }
}
