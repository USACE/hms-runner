package usace.cc.plugin.hmsrunner;

import java.util.ArrayList;

public class SSTTable {
    Event[] Events;
    
    public SSTTable(String[] lines){
        Events = new Event[lines.length-1];
        //System.out.println(lines.length);
        for(int i =0; i<lines.length;i++){
            if(i!=0){//skip header
                String[] parts = lines[i].split(",");
                //System.out.println(lines[i]);
                //System.out.println(parts.length);
                if(parts.length==7){
                    Event e = new Event();
                    int en = Integer.parseInt(parts[0]);
                    Double x = Double.parseDouble(parts[2]);
                    Double y = Double.parseDouble(parts[3]);
                    e.EventNumber = en;
                    e.StormPath = parts[1];
                    e.X = x;
                    e.Y = y;
                    e.StormType = parts[4];
                    e.StormDate = parts[5];
                    e.BasinPath = parts[6];
                    Events[i-1] = e;
                }
            }


        }

    }
    public Event[] getEventsByName(String name){
        ArrayList<Event> events = new ArrayList<Event>();
        for(Event e : Events){
            if(e.StormPath.contains(name)){
                events.add(e);
            }
        }
        Event[] result = new Event[events.size()];
        for(int i = 0; i < events.size(); i++){
            result[i] = events.get(i);
        }
        return result;
    }
}
