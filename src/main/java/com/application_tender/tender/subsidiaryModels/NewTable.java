package com.application_tender.tender.subsidiaryModels;

public class NewTable {
    private String name;
    private String name_en;
    private boolean frequency;
    private boolean vendor;
    private boolean channel;
    private boolean vxi;
    private boolean usb;
    private boolean portable;
    private boolean port;

    public String getName() {
        return name;
    }

    public String getName_en() {
        return name_en;
    }

    public boolean isFrequency() {
        return frequency;
    }

    public boolean isVendor() {
        return vendor;
    }

    public boolean isChannel() {
        return channel;
    }

    public boolean isVxi() {
        return vxi;
    }

    public boolean isUsb() {
        return usb;
    }

    public boolean isPortable() {
        return portable;
    }

    @Override
    public String toString() {
        return "NewTable{" +
                "name='" + name + '\'' +
                ", name_en='" + name_en + '\'' +
                ", frequency=" + frequency +
                ", vendor=" + vendor +
                ", channel=" + channel +
                ", vxi=" + vxi +
                ", usb=" + usb +
                ", portable=" + portable +
                ", port=" + port +
                '}';
    }

    public boolean isPort() {
        return port;
    }

}
