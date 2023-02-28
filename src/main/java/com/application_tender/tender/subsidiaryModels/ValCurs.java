package com.application_tender.tender.subsidiaryModels;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

//@XmlType(name = "ValCurs", propOrder = {
//        "Valute"
//})
//@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ValCurs")
public class ValCurs implements Serializable {
    @XmlElement(name = "Valute")
    public List<Valute> Valute;

    public List<Valute> getValute() {
        return Valute;
    }

    public void setValute(List<Valute> valute) {
        Valute = valute;
    }

    public ValCurs() {
    }
}