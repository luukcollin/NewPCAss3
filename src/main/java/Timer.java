import java.text.SimpleDateFormat;


public class Timer {
    private static long startTime = System.currentTimeMillis();
    private static boolean timerIsRunning = false;
    private static long startingState = 0;

    public static void start(){
        if(!timerIsRunning) {
            startingState = startTimer();
            System.out.println("The timer has started!");

        }else {
            System.out.println("You already have this timer instance running. Timer.stop() to stop the active timer. ");

        }
    }

    public static void stop(){
        if(timerIsRunning){
            long endState = stopTimer();
            System.out.println("The timer has stopped!");
            printTimerResults(startingState, endState);
        }else{
            System.out.println("You don't have this timer instance running. Timer.start() to avtivate this timer. ");
        }
    }

    private static long startTimer(){
        if(!timerIsRunning) {
            startTime = System.currentTimeMillis();
            timerIsRunning = true;
        }
        return startTime;
    }

    private static long stopTimer(){
        timerIsRunning = false;
        return System.currentTimeMillis();
    }
    private static void printTimerResults(long startTime, long endTime){
        System.out.printf(String.format("Timer started at %s. \nIt stopped  after %sms...Therefore\nIt ended at: %s}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(startTime).toString(), endTime-startTime, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(endTime).toString()));
    }
}
