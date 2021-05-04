package com.application_tender.tender.subsidiaryModels;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ValCurs")
public class ValCurs {
    public List<Valute> Valute;

    public List<Valute> getValute() {
        return Valute;
    }

    public void setValute(List<Valute> valute) {
        Valute = valute;
    }
}