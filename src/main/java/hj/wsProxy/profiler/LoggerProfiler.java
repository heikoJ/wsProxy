package hj.wsProxy.profiler;

import org.slf4j.Logger;

/**
 * Created by heiko on 26.07.15.
 */
public class LoggerProfiler extends Profiler {

    private Logger logger;

    public LoggerProfiler(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void printResult(String result) {
        this.logger.info(result);
    }
}
