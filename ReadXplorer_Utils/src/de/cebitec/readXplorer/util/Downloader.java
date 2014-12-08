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

package de.cebitec.readXplorer.util;


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

    private final List<Observer> observers = new ArrayList<>();
    private final String from;
    private final File to;


    public Downloader( String from, File to ) {
        this.from = from;
        this.to = to;
    }


    public static enum Status {

        RUNNING,
        FAILED,
        FINISHED;

    }


    private void startLoading() {
        notifyObservers( Downloader.Status.RUNNING );
        try {
            URL website = new URL( from );
            ReadableByteChannel rbc = Channels.newChannel( website.openStream() );
            try( FileOutputStream fos = new FileOutputStream( to ) ) {
                fos.getChannel().transferFrom( rbc, 0, Long.MAX_VALUE );
            }
        }
        catch( IOException ex ) {
            Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            Logger.getLogger( this.getClass().getName() ).log( Level.SEVERE, "{0}: Downloading file failed.", currentTimestamp );
            notifyObservers( Downloader.Status.FAILED );
        }
        notifyObservers( Downloader.Status.FINISHED );
    }


    @Override
    public void run() {
        startLoading();
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
