package hj.wsProxy.profiler;

import java.io.PrintStream;

/**
 * Created by heiko on 26.07.15.
 */
class PrintStreamProfiler extends Profiler {


    private PrintStream printStream;

    public PrintStreamProfiler(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    protected void printResult(String result) {
        printStream.println(result);
    }
}
