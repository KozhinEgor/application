package com.application_tender.tender.service;

import org.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Service;

@Service
public class Bicotender {
    private final String url ="https://www.bicotender.ru/api3/tenders/";
    private final String account = "/?login=client4002241&password=d857ff489099c57ebf960b2a0dbf9270";
    public  JSONObject loadTender(Long number){
        JSONObject ob = new JSONObject();
        String in = null;


        try {
            org.apache.commons.httpclient.HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(url+number + account);

            //Add any parameter if u want to send it with Post req.


            int statusCode = client.executeMethod(method);

            if (statusCode != -1) {
                in = method.getResponseBodyAsString();
            }

            assert in != null;

            ob = new JSONObject(in);



        } catch (Exception e) {
            e.printStackTrace();
        }
        return ob;
    }
}
