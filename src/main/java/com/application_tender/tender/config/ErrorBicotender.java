package com.application_tender.tender.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
public class ErrorBicotender extends RuntimeException {
    public ErrorBicotender(){
        super();
    }
    public ErrorBicotender(String message){
        super(message);
    }
}
