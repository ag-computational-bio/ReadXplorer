package de.cebitec.vamp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for unzipping a file.
 *
 * @author kstaderm
 */
public class Unzip implements Runnable, Observable {
    
    private List<Observer> observers = new ArrayList<>();
    private File zip;
    private File to;
    
    public static enum Status {
        
        RUNNING,
        FAILED,
        FILE_NOT_FOUND,
        NO_RIGHTS,
        FINISHED;
    }
    
    public static class NoDirectoryException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public NoDirectoryException() {
            super("The destination file must be a directory!");
        }
    }
    
    /**
     * Utility class for unzipping a file.
     * @param zip zip file
     * @param to target file for the unzip process
     * @throws de.cebitec.vamp.util.Unzip.NoDirectoryException 
     */
    public Unzip(File zip, File to) throws NoDirectoryException {
        this.zip = zip;
        if (to.isDirectory()) {
            this.to = to;
        } else {
            throw new NoDirectoryException();
        }
    }
    
    @Override
    public void run() {
        notifyObservers(Status.RUNNING);
        if (to.exists()) {
            byte[] buffer = new byte[1024];
            try {
                FileInputStream fis = new FileInputStream(zip);
                try (ZipInputStream zis = new ZipInputStream(fis)) {
                    ZipEntry nextEntry = zis.getNextEntry();
                    while (nextEntry != null) {
                        String currentFileName = nextEntry.getName();
                        File currentNewFile = new File(to.getAbsolutePath() + File.separator + currentFileName);
                        
                        if (nextEntry.isDirectory()) {
                            currentNewFile.mkdirs();
                        } else {
                            
                            try (FileOutputStream fos = new FileOutputStream(currentNewFile)) {
                                int length;
                                while ((length = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, length);
                                }
                            }
                        }
                        nextEntry = zis.getNextEntry();
                    }
                    notifyObservers(Status.FINISHED);
                }                
            } catch (IOException ex) {
                notifyObservers(Status.FAILED);
            }
        } else {
            notifyObservers(Status.FILE_NOT_FOUND);
        }
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
