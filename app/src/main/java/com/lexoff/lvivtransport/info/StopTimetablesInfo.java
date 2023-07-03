package com.lexoff.lvivtransport.info;

import java.util.ArrayList;
import java.util.List;

public class StopTimetablesInfo extends Info {
    private List<StopTimetable> timetables;

    public StopTimetablesInfo(){
        timetables=new ArrayList<>();
    }

    public List<StopTimetable> getTimetables(){
        return timetables;
    }

    public void addTimetable(StopTimetable timetable){
        timetables.add(timetable);
    }
}
