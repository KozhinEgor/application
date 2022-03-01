package com.application_tender.tender.subsidiaryModels;

import java.util.Arrays;

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
    private boolean form_factor;
    private boolean purpose;
    private boolean voltage;
    private boolean current;
    private String[] subcategory;
    private boolean subcategory_boolean;
    private String category;

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

    public boolean isForm_factor() {
        return form_factor;
    }

    public boolean isPurpose() {
        return purpose;
    }

    public boolean isVoltage() {
        return voltage;
    }

    public boolean isCurrent() {
        return current;
    }

    public String[] getSubcategory() {
        return subcategory;
    }

    public boolean isPort() {
        return port;
    }

    public String getCategory() {
        return category;
    }

    public boolean isSubcategory_boolean() {
        return subcategory_boolean;
    }

    public void setSubcategory_boolean(boolean subcategory_boolean) {
        this.subcategory_boolean = subcategory_boolean;
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
                ", form_factor=" + form_factor +
                ", purpose=" + purpose +
                ", voltage=" + voltage +
                ", current=" + current +
                ", subcategory=" + Arrays.toString(subcategory) +
                ", category='" + category + '\'' +
                '}';
    }
}
