/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Utility class for unzipping a file.
 *
 * @author kstaderm
 */
public class Unzip implements Runnable, Observable {

    private final List<Observer> observers = new ArrayList<>();
    private final File zip;
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
            super( "The destination file must be a directory!" );
        }


    }


    /**
     * Utility class for unzipping a file.
     * <p>
     * @param zip zip file
     * @param to  target file for the unzip process
     * <p>
     * @throws de.cebitec.readxplorer.utils.Unzip.NoDirectoryException
     */
    public Unzip( File zip, File to ) throws NoDirectoryException {
        this.zip = zip;
        if( to.isDirectory() ) {
            this.to = to;
        }
        else {
            throw new NoDirectoryException();
        }
    }


    @Override
    public void run() {
        notifyObservers( Status.RUNNING );
        if( to.exists() ) {
            byte[] buffer = new byte[1024];
            try {
                FileInputStream fis = new FileInputStream( zip );
                try( ZipInputStream zis = new ZipInputStream( fis ) ) {
                    ZipEntry nextEntry = zis.getNextEntry();
                    while( nextEntry != null ) {
                        String currentFileName = nextEntry.getName();
                        File currentNewFile = new File( to.getAbsolutePath() + File.separator + currentFileName );

                        if( nextEntry.isDirectory() ) {
                            currentNewFile.mkdirs();
                        }
                        else {
                            currentNewFile.getParentFile().mkdirs();
                            try( FileOutputStream fos = new FileOutputStream( currentNewFile ) ) {
                                int length;
                                while( (length = zis.read( buffer )) > 0 ) {
                                    fos.write( buffer, 0, length );
                                }
                            }
                        }
                        nextEntry = zis.getNextEntry();
                    }
                    notifyObservers( Status.FINISHED );
                }
            }
            catch( IOException ex ) {
                notifyObservers( Status.FAILED );
            }
        }
        else {
            notifyObservers( Status.FILE_NOT_FOUND );
        }
    }


    @Override
    public void registerObserver( Observer observer ) {
        observers.add( observer );
    }


    @Override
    public void removeObserver( Observer observer ) {
        observers.remove( observer );
    }


    @Override
    public void notifyObservers( Object data ) {
        List<Observer> tmpObservers = new ArrayList<>( observers );
        for( Observer observer : tmpObservers ) {
            observer.update( data );
        }
    }


}
