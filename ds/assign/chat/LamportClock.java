package ds.assign.chat;

public class LamportClock {

    private  int time=0;

    public synchronized int getTime(){
        return time;
    }
    public synchronized void increment(){
        time++;
    }

    public synchronized void update(int receivedTimestamp) {
        time = Math.max(time, receivedTimestamp) + 1;
    }
}
