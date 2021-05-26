package com.application_tender.tender.controller;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.application_tender.tender.subsidiaryModels.ValCurs;
import com.application_tender.tender.subsidiaryModels.Valute;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;


import javax.xml.bind.JAXBContext;

import javax.xml.bind.Unmarshaller;
@Controller
public class GetCurrency {
    @Value("${file.currensy}")
    private String pathname;
    public Map<String,Double> currency(String date){
        Map<String,Double> returnCurrency = new HashMap<>();
        String url = "http://www.cbr.ru/scripts/XML_daily.asp?";
        InputStream in = null;
        ValCurs currency;
            try {
            HttpClient client = new HttpClient();
            PostMethod method = new PostMethod(url);

            //Add any parameter if u want to send it with Post req.
            method.addParameter("date_req", date);

            int statusCode = client.executeMethod(method);

            if (statusCode != -1) {
                in = method.getResponseBodyAsStream();
            }
            File file = new File(pathname);
            copyInputStreamToFile(in, file);
            assert in != null;

            JAXBContext jaxbContext = JAXBContext.newInstance(ValCurs.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            currency = (ValCurs) unmarshaller.unmarshal(file);
            List<Valute> list = currency.getValute();
            for(Valute valute: list){
                returnCurrency.put(valute.CharCode, Double.valueOf(valute.Value.replace(',','.'))/valute.Nominal);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnCurrency;
    }

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }
}
