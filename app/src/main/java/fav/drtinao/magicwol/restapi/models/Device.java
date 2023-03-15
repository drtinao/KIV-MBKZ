package fav.drtinao.magicwol.restapi.models;

import com.google.gson.annotations.SerializedName;

public class Device {
    private String devName;
    private String devMAC;

    @SerializedName("device_segment")
    private String devSeg;

    public String getDevName() {
        return devName;
    }

    public String getDevMAC() {
        return devMAC;
    }

    public String getDevSeg() {
        return devSeg;
    }
}