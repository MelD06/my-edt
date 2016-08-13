package fr.sonline.enacmyedt;

/**
 * Created by Melvin on 27/04/2016.
 */
public class EDTCourse {
    String Day = null;
    String Start = null;
    String End = null;
    String Name = null;
    String Room = null;
    int dayempt = 1;

    public EDTCourse(String Dayz, String Startz, String Endz, String Namez, String Roomz){
        Day = Dayz;
        Start = Startz;
        End = Endz;
        Name = Namez;
        Room = Roomz;
        isDayEmpty();
    }

    public String getDay(){
            return Day;
        }

    public int getDayempt(){
        return dayempt;
    }

    private void isDayEmpty(){
        if(Day != null && Day.length() > 2){
            dayempt = 0;
        }
        else{
            dayempt = 1;
        }
    }

    public String getStart(){
        return Start;
    }

    public String getEnd() {
        return End;
    }

    public String getName() {
        return Name;
    }

    public String getRoom() {
        return Room;
    }

    public String getFormatTime(){
        return Start + "\n" + End;
    }
}
