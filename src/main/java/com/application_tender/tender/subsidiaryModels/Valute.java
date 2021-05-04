package com.application_tender.tender.subsidiaryModels;

public class Valute {
    public int NumCode;
    public String CharCode;
    public int Nominal;
    public String Name;
    public String Value;
    public String ID;
    public String Text;

    @Override
    public String toString() {
        return "Valute{" +
                "NumCode=" + NumCode +
                ", CharCode='" + CharCode + '\'' +
                ", Nominal=" + Nominal +
                ", Name='" + Name + '\'' +
                ", Value=" + Value +
                ", ID='" + ID + '\'' +
                ", Text='" + Text + '\'' +
                '}';
    }
}