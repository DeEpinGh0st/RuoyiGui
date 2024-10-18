package com.ruoyi.ruoyigui.util;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.Proxy;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class CustomHttpClient {
    private int connectTimeout = 10000; // 默认连接超时时间为10秒
    private int readTimeout = 10000; // 默认读取超时时间为10秒
    private boolean trustAllCertificates = false; // 是否信任所有SSL证书
    private static Map<String, String> globalHeaders = new java.util.HashMap<>();
    private Proxy proxy;

    public CustomHttpClient() {
        // 设置默认的 User-Agent
        globalHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36");
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    public void setHttpProxy(String host, int port) {
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    public void setSocksProxy(String host, int port) {
        this.proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
    }


    // 静态方法设置全局 headers
    public static void setGlobalHeader(String name, String value) {
        globalHeaders.put(name, value);
    }

    public static Map<String, String> getGlobalHeaders() {
        return new java.util.HashMap<>(globalHeaders); // 返回一个副本
    }

    public static void setGlobalHeaders(Map<String, String> headers) {
        globalHeaders = headers;
    }

    public static void clearGlobalHeaders() {
        globalHeaders.clear();
    }

    public void setJson() {
        if (globalHeaders == null) {
            globalHeaders = new java.util.HashMap<>();
        }
        globalHeaders.put("Content-Type", "application/json");
    }


    public HttpResponse get(String url) throws IOException {
        HttpURLConnection connection = createConnection(url, "GET", globalHeaders);
        return executeRequest(connection);
    }

    public HttpResponse post(String url, String postData) throws IOException {
        HttpURLConnection connection = createConnection(url, "POST", globalHeaders);
        // 设置为POST请求
        connection.setDoOutput(true);

        // 写入请求体数据
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(postData.getBytes(StandardCharsets.UTF_8));
        }

        return executeRequest(connection);
    }

    private HttpURLConnection createConnection(String url, String method, Map<String, String> globalHeaders) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection connection;
        if (proxy != null) {
            connection = (HttpURLConnection) urlObj.openConnection(proxy);
        } else {
            connection = (HttpURLConnection) urlObj.openConnection();
        }

        // 设置请求方法
        connection.setRequestMethod(method);

        // 设置连接超时和读取超时时间
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        // 设置请求头
        if (globalHeaders != null) {
            for (Map.Entry<String, String> entry : globalHeaders.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        // 处理HTTPS信任证书
        if (url.toLowerCase().startsWith("https://") && trustAllCertificates) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            httpsConnection.setSSLSocketFactory(TrustAllSSLSocketFactory.get());
            httpsConnection.setHostnameVerifier((hostname, session) -> true);
        }

        return connection;
    }

    public HttpResponse uploadFile(String url, File file, String fieldName, String fileName) throws IOException {
        String boundary = "------------------------" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";

        HttpURLConnection connection = createConnection(url, "POST", globalHeaders);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {

            // Add form field
            writer.append("--").append(boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"")
                    .append(fileName).append("\"").append(LINE_FEED);
            writer.append("Content-Type: ").append(Files.probeContentType(file.toPath())).append(LINE_FEED);
            writer.append(LINE_FEED).flush();

            // Write file content
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            writer.append(LINE_FEED).flush();
            writer.append("--").append(boundary).append("--").append(LINE_FEED);
            writer.flush();
        }

        return executeRequest(connection);
    }

    private HttpResponse executeRequest(HttpURLConnection connection) throws IOException {
        int statusCode = connection.getResponseCode();

        // 获取响应头
        Map<String, List<String>> headers = connection.getHeaderFields();

        // 读取响应体
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                statusCode < HttpURLConnection.HTTP_BAD_REQUEST ? connection.getInputStream() : connection.getErrorStream()))) {

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return new HttpResponse(statusCode, response.toString(), headers);
        }
    }

    public static class HttpResponse {
        private final int statusCode;
        private final String body;
        private final Map<String, List<String>> headers;

        public HttpResponse(int statusCode, String body, Map<String, List<String>> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public String getHeader(String headerName) {
            if (headers != null) {
                List<String> values = headers.get(headerName);
                if (values != null && !values.isEmpty()) {
                    return values.get(0);
                }
            }
            return null;
        }
    }

    public static class TrustAllSSLSocketFactory {
        public static SSLSocketFactory get() {
            try {
                // 创建一个信任所有证书的SSLContext
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }}, new SecureRandom());

                return sslContext.getSocketFactory();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException("Failed to create TrustAllSSLSocketFactory", e);
            }
        }
    }

    public static void main(String[] args) {
        try {
            // 示例用法
            CustomHttpClient httpClient = new CustomHttpClient();
            String url = "http://httpbin.org/get";

            // 使用代理
//            httpClient.setSocksProxy("127.0.0.1", 10808);
            httpClient.setHttpProxy("127.0.0.1", 8081);

            // 忽略证书
            httpClient.setTrustAllCertificates(true);
            File test = new File("C:\\Users\\12415\\Desktop\\fsdownload\\yaml-payload.jar");
            CustomHttpClient.setGlobalHeader("Cookie", "JSESSIONID=06c7c24b-eb57-454e-a18e-c8f877fac3d5");
            // 发送GET请求
            HttpResponse getResponse = httpClient.uploadFile("http://192.168.119.1/common/upload", test, "file", "test.rar");
            System.out.println("GET Response Code: " + getResponse.getStatusCode());
            System.out.println("GET Response Headers: " + getResponse.getHeaders());
            System.out.println("GET Response Header: " + getResponse.getHeader("Server"));
            System.out.println("GET Response Body: " + getResponse.getBody());

            // 发送POST请求
            String postData = "title=test&body=test&userId=1";

            httpClient.setJson();
            HttpResponse postResponse = httpClient.post(url, postData);
            System.out.println("POST Response Code: " + postResponse.getStatusCode());
            System.out.println("POST Response Body: " + postResponse.getBody());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}