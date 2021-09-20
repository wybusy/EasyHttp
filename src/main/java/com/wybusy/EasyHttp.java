package com.wybusy;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.AbstractExecutionAwareRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.regex.Pattern;

public class EasyHttp {
    /**
     * ## Http Https请求
     *
     */

    /**
     * ##### - get
     * get方式，支持http,https方式
     *
     * @param urlPath
     * @param headers
     * @return String
     */
    public static String get(String urlPath, Map<String, String> headers) {
        return send(
                true,
                urlPath,
                null,
                false,
                headers
        );
    }

    /**
     * ##### - post
     * post方式，支持http,https方式
     *
     * @param urlPath
     * @param params
     * @param paramsIsJson
     * @param headers
     * @return
     */
    public static String post(String urlPath, String params, boolean paramsIsJson, Map<String, String> headers) {
        return send(
                false,
                urlPath,
                params,
                paramsIsJson,
                headers
        );
    }

    /**
     * ##### - downloadFile
     * 下载文件
     *
     * @param fileUrl
     * @param savePath
     * @param saveName
     * @throws IOException
     */
    public static void downloadFile(String fileUrl, String savePath, String saveName) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url
                .openConnection();
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
        InputStream in = conn.getInputStream(); //创建连接、输入流
        File file = new File(savePath, saveName);
        Path filePath = Paths.get(savePath);
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectories(filePath);
            } catch (IOException e) {}
        }
        FileOutputStream f = new FileOutputStream(file.toString());//创建文件输出流
        byte[] bb = new byte[1024]; //接收缓存
        int len;
        while ((len = in.read(bb)) > 0) { //接收
            f.write(bb, 0, len); //写入文件
        }
        f.close();
        in.close();
    }

    private static String send(boolean methodIsGet, String urlPath, String params, boolean paramsIsJson, Map<String, String> headers) {
        String result = "";
        //创建httpclient
        CloseableHttpClient httpClient = (Pattern.matches("(?i)^https://.*", urlPath))
                ? httpsClient()
                : HttpClients.createDefault();

        //创建http post/get
        AbstractExecutionAwareRequest httpRequest = (methodIsGet) ?
                new HttpGet(urlPath) :
                new HttpPost(urlPath);

        //模拟浏览器设置头
        httpRequest.setHeader("Accept", "*/*");
        httpRequest.setHeader("Connection", "Keep-Alive");
        httpRequest.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
        if (!methodIsGet)
            httpRequest.setHeader("Content-Type", paramsIsJson ? "application/json; charset=UTF-8" : "application/x-www-form-urlencoded");

        if (headers != null) {
            for (String key : headers.keySet()) {
                httpRequest.setHeader(key, headers.get(key));
            }
        }

        if (!methodIsGet) {
            //构建表单
            //将表达请求放入到httpost
            StringEntity se = null;
            try {
                se = new StringEntity(params);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ((HttpPost) httpRequest).setEntity(se);
        }
        //返回类型
        CloseableHttpResponse response = null;

        try {
            if (methodIsGet)
                response = httpClient.execute((HttpGet) httpRequest);
            else
                response = httpClient.execute((HttpPost) httpRequest);
            result = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static CloseableHttpClient httpsClient() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(
                    ctx, NoopHostnameVerifier.INSTANCE);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(ssf).build();
            return httpclient;
        } catch (Exception e) {
            return HttpClients.createDefault();
        }
    }
}
/**
 * 替换规则
 * ^ *
 * ^[^\*].*\n
 * \* ?/?
 *
 * @(.*)\n -> > \1    \n
 */