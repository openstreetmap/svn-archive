/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstreetmap.fma.jtiledownloader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Simple and clean log formatter.
 * 
 * @author zverik
 */
public final class LogFormatter extends Formatter {

    private final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        sb.append(new SimpleDateFormat("HH:mm").format(new Date(record.getMillis())))
                .append(" ").append(record.getLevel().getLocalizedName()).append(": ")
                .append(formatMessage(record))
                .append(LINE_SEPARATOR);

        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
                // ignore
            }
        }

        return sb.toString();
    }
}
