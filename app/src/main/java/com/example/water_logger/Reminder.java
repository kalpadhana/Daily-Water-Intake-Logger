package com.example.water_logger;

public class Reminder {
    private String name;
    private String time;
    private boolean isEnabled;

    public Reminder(String name, String time, boolean isEnabled) {
        this.name = name;
        this.time = time;
        this.isEnabled = isEnabled;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void setTime(String time) {
        this.time = time;
    }
}