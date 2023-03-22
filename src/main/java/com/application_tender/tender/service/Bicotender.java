package com.application_tender.tender.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class Bicotender {
    private final String url ="https://www.bicotender.ru/api3/tenders/";
    private final String account = "/?login=client4412324&password=9429466337efb778f2a9ad013a3329a0";
    public  JSONObject loadTender(Long number){
        JSONObject ob = new JSONObject();
        String in = null;

        try {
            org.apache.commons.httpclient.HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(url+number + account);

            //Add any parameter if u want to send it with Post req.


            int statusCode = client.executeMethod(method);

            if (statusCode != -1) {
                InputStream str = method.getResponseBodyAsStream();
                in = new String(str.readAllBytes(), StandardCharsets.US_ASCII);

            }

            assert in != null;
            try {
                ob = new JSONObject(in);
            }
            catch (JSONException e){
                return null;
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
        return ob;
    }
}
