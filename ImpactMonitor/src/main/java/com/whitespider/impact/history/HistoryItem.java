package com.whitespider.impact.history;

import com.whitespider.impact.util.Point3D;

public class HistoryItem {
    private String time;
    private int severity;
    private double totalAcceleration;
    private Point3D direction;

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }

    public void setTotalAcceleration(double totalAcceleration) {
        this.totalAcceleration = totalAcceleration;
    }

    public double getTotalAcceleration() {
        return totalAcceleration;
    }

    public void setDirection(Point3D direction) {
        this.direction = direction;
    }

    public Point3D getDirection() {
        return direction;
    }
}
