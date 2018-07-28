package plotmas.helper;

import java.util.logging.LogRecord;

import jason.runtime.MASConsoleLogFormatter;
import jason.runtime.MASConsoleLogHandler;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher;

/**
 * Custom log formatter that prepends the plot time to log statements
 * @author Leonid Berov
 */
public class PlotFormatter extends MASConsoleLogFormatter {

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append("[").append(MASConsoleLogFormatter.getAgName(record));
        builder.append(String.format(" @ step %d (%d ms)] ",
        							 PlotLauncher.getRunner().getUserEnvironment().getStep(), 
        							 PlotEnvironment.getPlotTimeNow()));
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }

    public static MASConsoleLogHandler handler() {
        PlotFormatter formatter = new PlotFormatter();
        MASConsoleLogHandler handler = new MASConsoleLogHandler();
        handler.setFormatter(formatter);
        
        return handler;
    }
}

