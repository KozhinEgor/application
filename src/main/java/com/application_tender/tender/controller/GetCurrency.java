package com.application_tender.tender.controller;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.application_tender.tender.subsidiaryModels.ValCurs;
import com.application_tender.tender.subsidiaryModels.Valute;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;
import springfox.documentation.schema.Xml;

import javax.xml.bind.JAXBContext;

import javax.xml.bind.JAXBContextFactory;
import javax.xml.bind.Unmarshaller;
@Controller

public class GetCurrency {
    @Value("${file.currensy}")
    private String pathname;
    @Autowired
    private RestTemplate restTemplate;

    public Map<String,Double> currency(String date){
        Map<String,Double> returnCurrency = new HashMap<>();
        String url = "http://www.cbr.ru/scripts/XML_daily.asp?";
        InputStream in = null;
        ValCurs currency;
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
                ResponseEntity<String> method = restTemplate.exchange(UriComponentsBuilder
                                .fromHttpUrl("http://www.cbr.ru/scripts/XML_daily.asp").queryParam("date_req", date).toUriString(),
                        HttpMethod.GET,
                        null,
                        String.class);

            //Add any parameter if u want to send it with Post req.


            if (method.getStatusCode().equals(HttpStatus.OK)) {
                StringReader sr = new StringReader(method.getBody());
                JAXBContext jaxbContext = JAXBContext.newInstance(ValCurs.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                ValCurs response = (ValCurs) unmarshaller.unmarshal(sr);
//                in = new ByteArrayInputStream(method.getBody());
//                File file = new File(pathname);
//                copyInputStreamToFile(in, file);
//                assert in != null;

//                JAXBContext jaxbContext = JAXBContext.newInstance(ValCurs.class);
//                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//                currency = (ValCurs) unmarshaller.unmarshal(file);
                List<Valute> list = response.getValute();
                for(Valute valute: list){
                    returnCurrency.put(valute.CharCode, Double.valueOf(valute.Value.replace(',','.'))/valute.Nominal);
                }
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
