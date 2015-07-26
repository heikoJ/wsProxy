package hj.wsProxy.profiler;

import org.slf4j.Logger;

import java.io.PrintStream;

/**
 * Created by heiko on 26.07.15.
 */
public abstract class Profiler {


    private long startTimeNanos;

    private String subject;

    protected Profiler(String subject) {
        this.subject = subject;
    }

    public static Profiler printStreamProfiler(PrintStream printStream,String subject) {
        return new PrintStreamProfiler(printStream,subject);
    }

    public static Profiler loggerProfiler(Logger logger, String subject) {
        return new LoggerProfiler(logger,subject);
    }

    public Profiler start() {
        startTimeNanos = System.nanoTime();
        return this;
    }


    public void endAndPrint() {
       long endTimeNanos = System.nanoTime();
       double durationInMilliseconds = (endTimeNanos - startTimeNanos) / 1000000.0 ;

        String result = "Execution of " + subject + " took " + durationInMilliseconds + " milliseconds";

        printResult(result);
    }

    protected abstract void printResult(String result);

}
