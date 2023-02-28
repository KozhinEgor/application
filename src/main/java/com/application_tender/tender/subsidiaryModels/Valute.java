package com.application_tender.tender.subsidiaryModels;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Valute {
    @XmlElement(name = "NumCode")
    public int NumCode;
    @XmlElement(name = "CharCode")
    public String CharCode;
    @XmlElement(name = "Nominal")
    public int Nominal;
    @XmlElement(name = "Name")
    public String Name;
    @XmlElement(name = "Value")
    public String Value;
//    @XmlAttribute(name = "ID")
//    public String ID;

    public int getNumCode() {
        return NumCode;
    }

    public void setNumCode(int numCode) {
        NumCode = numCode;
    }

    public String getCharCode() {
        return CharCode;
    }

    public void setCharCode(String charCode) {
        CharCode = charCode;
    }

    public int getNominal() {
        return Nominal;
    }

    public void setNominal(int nominal) {
        Nominal = nominal;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

//    public String getID() {
//        return ID;
//    }
//
//    public void setID(String ID) {
//        this.ID = ID;
//    }

    @Override
    public String toString() {
        return "Valute{" +
                "NumCode=" + NumCode +
                ", CharCode='" + CharCode + '\'' +
                ", Nominal=" + Nominal +
                ", Name='" + Name + '\'' +
                ", Value=" + Value +
//                ", ID='" + ID + '\'' +
                '}';
    }
}