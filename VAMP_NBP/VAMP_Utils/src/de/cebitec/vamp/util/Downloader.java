package de.cebitec.vamp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kstaderm
 */
public class Downloader implements Runnable, Observable {

    private List<Observer> observers = new ArrayList<>();
    private String from;
    private File to;

    public Downloader(String from, File to) {
        this.from = from;
        this.to = to;
    }
    
    public static enum Status {

        RUNNING,
        FAILED,
        FINISHED;
    }

    private void startLoading() {
        notifyObservers(Downloader.Status.RUNNING);
        try {
            URL website = new URL(from);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            try (FileOutputStream fos = new FileOutputStream(to)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        } catch (IOException ex) {
            Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "{0}: Downloading file failed.", currentTimestamp);
            notifyObservers(Downloader.Status.FAILED);
        }
        notifyObservers(Downloader.Status.FINISHED);
    }

    @Override
    public void run() {
        startLoading();
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        List<Observer> tmpObservers = new ArrayList<>(observers);
        for (Iterator<Observer> it = tmpObservers.iterator(); it.hasNext();) {
            Observer observer = it.next();
            observer.update(data);
        }
    }
}
