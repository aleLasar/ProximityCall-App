package com.example.project;

import android.net.wifi.p2p.WifiP2pDevice;

public class Device {
    private int id;
    private String deviceName;
    private WifiP2pDevice device;

    public Device(int id, String deviceName, WifiP2pDevice device){
        this.id = id;
        this.deviceName= deviceName;
        this.device = device;
    }

    public WifiP2pDevice getDevice(){
        return device;
    }

    public  int getId(){
        return id;
    }
}
