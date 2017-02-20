package com.jm.bid.common.service;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by joe on 11/18/14.
 */
public class SMSClient {

    private static final Logger logger = LoggerFactory.getLogger(SMSClient.class);

    private String server;
    private String sn;
    private String pwd;

    public SMSClient(String server, String sn, String password) {
        this.server = server;
        this.sn = sn;
        try {
            this.pwd = this.getMD5(sn + password);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getMD5(String sourceStr) throws UnsupportedEncodingException {
        String resultStr = "";
        try {
            byte[] temp = sourceStr.getBytes();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(temp);
            byte[] b = md5.digest();
            for (int i = 0; i < b.length; i++) {
                char[] digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
                char[] ob = new char[2];
                ob[0] = digit[(b[i] >>> 4) & 0X0F];
                ob[1] = digit[b[i] & 0X0F];
                resultStr += new String(ob);
            }
            return resultStr;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public SMSResponse sendSMS(String phone, String content, String signature) throws Exception {
        SMSResponse res = new SMSResponse();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(server);
        httpPost.addHeader("SOAPAction", "http://entinfo.cn/mdsmssend");

        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        xml += "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
        xml += "<soap:Body>";
        xml += "<mdsmssend xmlns=\"http://entinfo.cn/\">";
        xml += "<sn>" + sn + "</sn>";
        xml += "<pwd>" + pwd + "</pwd>";
        xml += "<mobile>" + phone + "</mobile>";
        xml += "<content>" + content + signature + "</content>";
        xml += "</mdsmssend>";
        xml += "</soap:Body>";
        xml += "</soap:Envelope>";

        StringEntity stringEntity = new StringEntity(xml, ContentType.create("text/xml", "UTF-8"));
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        String out = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        int code = 0;
        try {
            code = Integer.parseInt(out);
        } catch (Exception ex) {
            logger.error("短信发送失败,响应结果:{},手机号码:{}", out, phone,ex);
        }

        if (code < 0) {
            res.setSuccess(false);
        } else {
            res.setSuccess(true);
        }
        res.setCode(out);
        return res;
    }
}
