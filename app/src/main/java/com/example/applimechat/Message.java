package com.example.applimechat;

public class Message {
    private String text;
    private String deviceId;

    public Message() {}

    public Message(String text, String deviceId) {
        this.text = text;
        this.deviceId = deviceId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}