package com.lexoff.lvivtransport.info;

import android.widget.ListView;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Stop extends Info {
    private int code;
    private String name;

    private List<String> routesAvailable;

    private double latitude;
    private double longitude;

    private StopTimetablesInfo stopTimetablesInfo;

    private TransfersInfo transfersInfo;

    public Stop(){
        routesAvailable=new ArrayList<>();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPoint getPoint(){
        return new GeoPoint(latitude, longitude);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getRoutesAvailable() {
        return routesAvailable;
    }

    public String getRoutesAvailableAsString() {
        StringBuilder sb=new StringBuilder();
        for (String s:routesAvailable) {
            sb.append(s);
            sb.append(", ");
        }

        int ci=sb.length()-2;

        if (ci>=0 && sb.charAt(ci)==','){
            sb.deleteCharAt(sb.length()-1);
            sb.deleteCharAt(sb.length()-1);
        }

        return sb.toString();
    }

    public void addRoutesAvailable(String route) {
        routesAvailable.add(route);
    }

    public void addRoutesAvailable(String[] routes) {
        Collections.addAll(routesAvailable, routes);
    }

    public StopTimetablesInfo getStopTimetablesInfo() {
        return stopTimetablesInfo;
    }

    public void setStopTimetablesInfo(StopTimetablesInfo stopTimetablesInfo) {
        this.stopTimetablesInfo = stopTimetablesInfo;
    }

    public String[][] getTimetableAsArray(){
        String[][] timetable=new String[2][stopTimetablesInfo.getTimetables().size()];

        String[] timeLefts=new String[stopTimetablesInfo.getTimetables().size()];
        String[] routes=new String[stopTimetablesInfo.getTimetables().size()];

        for (int i=0; i<stopTimetablesInfo.getTimetables().size(); i++){
            StopTimetable st=stopTimetablesInfo.getTimetables().get(i);
            TransportInfo tInfo=st.getTransportInfo();

            timeLefts[i]=st.getTimeLeft();
            routes[i]=tInfo.getShortRouteName();
        }

        timetable[0]=timeLefts;
        timetable[1]=routes;

        return timetable;
    }

    public TransfersInfo getTransfersInfo() {
        return transfersInfo;
    }

    public void setTransfersInfo(TransfersInfo transfersInfo) {
        this.transfersInfo = transfersInfo;
    }
}
