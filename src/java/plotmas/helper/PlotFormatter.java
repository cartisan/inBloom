package plotmas.helper;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import jason.runtime.MASConsoleLogFormatter;
import jason.runtime.MASConsoleLogHandler;
import plotmas.PlotEnvironment;

public class PlotFormatter extends MASConsoleLogFormatter {

    public String format(LogRecord record) {
    	long currentTime = (System.nanoTime() - PlotEnvironment.startTime) / 1000000;
    	
        StringBuilder builder = new StringBuilder(1000);
        builder.append("[").append(MASConsoleLogFormatter.getAgName(record));
        builder.append(String.format(" @ %d ms] ", currentTime));		
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
    
    public static MASConsoleLogHandler handler() {
        PlotFormatter formatter = new PlotFormatter();
        MASConsoleLogHandler handler = new MASConsoleLogHandler();
        handler.setFormatter(formatter);
        
        return handler;
    }
}

