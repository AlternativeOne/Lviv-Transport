package com.lexoff.lvivtransport.info;

public class StopTimetable extends Info {
    private String routeId;
    private String arrivalTimeStr;
    private String timeLeft;

    private TransportInfo transportInfo;

    public StopTimetable(){

    }


    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getArrivalTime() {
        return arrivalTimeStr;
    }

    public void setArrivalTime(String arrivalTimeStr) {
        this.arrivalTimeStr = arrivalTimeStr;
    }

    public String getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(String timeLeft) {
        this.timeLeft = timeLeft;
    }

    public TransportInfo getTransportInfo() {
        return transportInfo;
    }

    public void setTransportInfo(TransportInfo transportInfo) {
        this.transportInfo = transportInfo;
    }
}
