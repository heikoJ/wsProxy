package hj.wsProxy.profiler;

import org.slf4j.Logger;

import java.io.PrintStream;

/**
 * Created by heiko on 26.07.15.
 */
public abstract class Profiler {


    private long startTimeNanos;

    private String subject;




    public static Profiler forPrintStream(PrintStream printStream) {
        return new PrintStreamProfiler(printStream);
    }

    public static Profiler forLogger(Logger logger) {
        return new LoggerProfiler(logger);
    }

    public Profiler withSubject(String subject) {
        this.subject = subject;
        return this;
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
