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

package de.cebitec.readxplorer.parser.common;


import de.cebitec.readxplorer.utils.Observable;
import de.cebitec.readxplorer.utils.Observer;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openide.util.Exceptions;

import static java.util.logging.Level.SEVERE;


/**
 * A fetcher for any part of a referenc sequence stored in an indexed fasta
 * file.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class RefSeqFetcher implements Observable {

    private static final Logger LOG = Logger.getLogger( RefSeqFetcher.class.getName() );

    private List<Observer> observers;
    private IndexedFastaSequenceFile refFile = null;


    /**
     * A fetcher for any part of a referenc sequence stored in an indexed fasta
     * file.
     * <p>
     * @param indexedFastaFile The indexed fasta file from which the data shall
     * be read.
     * @param observer The observer for receiving error messages
     */
    public RefSeqFetcher( File indexedFastaFile, Observer observer ) {
        this.observers = new ArrayList<>();
        this.observers.add( observer );
        try {
            refFile = new IndexedFastaSequenceFile( indexedFastaFile );
        } catch( FileNotFoundException fnfe ) {
            LOG.log( SEVERE, fnfe.getMessage(), fnfe );
            this.notifyObservers( "Fasta reference index file not found. Please make sure it exist." );
            this.notifyObservers( fnfe.getMessage() );
        } catch( Exception pe ) {
            LOG.log( SEVERE, pe.getMessage(), pe );
            String msg = "The following reference fasta file is missing! Please restore it in order to use this DB:\n" + indexedFastaFile.getAbsolutePath();
            JOptionPane.showMessageDialog( new JPanel(), msg, "Fasta missing error", JOptionPane.ERROR_MESSAGE );
        }
    }


    /**
     * Fetches the subsequence defined by start, stop and a reference name from
     * the reference file stored in this object.
     * <p>
     * @param refName name of the reference from which the sequence shall be
     * retrieved
     * @param start start position of the interval of interest
     * @param stop stop position of the interval of interest
     * <p>
     * @return The subsequence defined by start, stop and a reference name from
     * the reference file stored in this object.
     */
    public String getSubSequence( String refName, int start, int stop ) {
        String refSeq = "";
        try {
            refSeq = new String( refFile.getSubsequenceAt( refName, start, stop ).getBases(), Charset.forName( "UTF-8" ) ).toUpperCase();
        } catch( Exception pe ) {
            LOG.log( SEVERE, pe.getMessage(), pe );
            String msg = "Mapping and reference data are out of sync for reference " + refName + ". One of the queried positions is out of reach!"
                    + "Reimport the correct reference or fix the mapping data!";
            JOptionPane.showMessageDialog( new JPanel(), msg, "Reference sequence error", JOptionPane.ERROR_MESSAGE );
            Exceptions.printStackTrace( pe );
        }
        return refSeq;
    }


    @Override
    public void registerObserver( Observer observer ) {
        this.observers.add( observer );
    }


    @Override
    public void removeObserver( Observer observer ) {
        this.observers.remove( observer );
    }


    @Override
    public void notifyObservers( Object data ) {
        for( Observer observer : this.observers ) {
            observer.update( data );
        }
    }


}
