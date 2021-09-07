import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//todo Измениь пути файлов

public class Application extends RollingFileAppender {

    static final Logger log = Logger.getLogger(ProcessingRMQ.class);
    static long nextRollover = 0L;

    public static void main(String[] args) {
        ProcessingRMQ rmq1 = new ProcessingRMQ("RMQ1");
        rmq1.start();
        ProcessingRMQ rmq2 = new ProcessingRMQ("RMQ2");
        rmq2.start();
    }

    @Override
    public void rollOver() {
        if (this.qw != null) {
            long size = ((CountingQuietWriter) this.qw).getCount();
            LogLog.debug("rolling over count=" + size);
            this.nextRollover = size + this.maxFileSize;
        }

        LogLog.debug("maxBackupIndex=" + this.maxBackupIndex);
        boolean renameSucceeded = true;
        if (this.maxBackupIndex != 0) {
            Date d = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
            File file = new File("/home/rmqsales/logs/rmqLogFile" + '.' + format.format(d) + ".log");
            if (file.exists()) {
                renameSucceeded = file.delete();
            }
        }

        if (renameSucceeded) {
            try {
                Date d = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                this.setFile("/home/rmqSales/logs/logFile" + "." + format.format(d) + ".log", false, this.bufferedIO, this.bufferSize);
                nextRollover = 0L;
            } catch (IOException var5) {
                if (var5 instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }

                LogLog.error("setFile(" + this.fileName + ", false) call failed.", var5);
            }
        }
    }

    @Override
    public void setFile(String fileName) {
        if (fileName.contains("%timestamp")) {
            Date d = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
            fileName = fileName.replaceAll("%timestamp", format.format(d));
        }
        super.setFile(fileName);
    }

}
