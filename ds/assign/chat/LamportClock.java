package ds.assign.chat;

public class LamportClock {


    private int time = 0;

    public LamportClock(int time) {

        this.time = time;


    }

    public int getTime() {
        return time;
    }

    public void setTime() {
        this.time = 0;
    }

    public void increment() {
        this.time ++ ;
    }

    public void decrement() {
        this.time -- ;
    }
}
