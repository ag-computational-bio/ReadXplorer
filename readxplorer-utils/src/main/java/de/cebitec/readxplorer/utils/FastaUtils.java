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


import de.cebitec.common.parser.fasta.FastaIndexEntry;
import de.cebitec.common.parser.fasta.FastaIndexWriter;
import de.cebitec.common.parser.fasta.FastaIndexer;
import java.awt.Dialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.sf.picard.PicardException;
import net.sf.picard.reference.IndexedFastaSequenceFile;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;


/**
 * Contains some utils for fasta files.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class FastaUtils implements Observable {


    private final List<Observer> observers;


    /**
     * Contains some utils for fasta files.
     */
    public FastaUtils() {
        this.observers = new ArrayList<>();
    }


    /**
     * Generates a Fasta index file for an input Fasta file, if the index does
     * not already exist.
     * <p>
     * @param fastaFileToIndex The fasta file for which an index shall be
     * created, if it not existent already.
     * @param observers List of observers, which shall be updated in case of
     * errors.
     */
    public void indexFasta( File fastaFileToIndex, List<Observer> observers ) {
        for( Observer observer : observers ) {
            this.registerObserver( observer );
        }

        try {
            IndexedFastaSequenceFile fastaFile = new IndexedFastaSequenceFile( fastaFileToIndex );
        } catch( FileNotFoundException ex ) {
            try {
                FastaIndexer indexer = new FastaIndexer();
                List<FastaIndexEntry> sequences = indexer.createIndex( fastaFileToIndex.toPath() );
                FastaIndexWriter idxWriter = new FastaIndexWriter();
                Path indexFile = Paths.get( fastaFileToIndex.getAbsolutePath() + ".fai" );
                idxWriter.writeIndex( indexFile, sequences );
            } catch( IOException | IllegalStateException e ) {
                this.notifyObservers( e.getMessage() );
            }
        } catch( PicardException e ) {
            String msg = "The following reference fasta file is missing! Please restore it in order to use this DB:\n" + fastaFileToIndex.getAbsolutePath();
            JOptionPane.showMessageDialog( new JPanel(), msg, "Fasta missing error", JOptionPane.ERROR_MESSAGE );
        }

        for( Observer observer : observers ) {
            this.removeObserver( observer );
        }
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


    /**
     * @param fastaFile A fasta file, which shall be checked for an existing
     * index file.
     * <p>
     * @return The indexed fasta file, if it could be created, null otherwise.
     */
    public IndexedFastaSequenceFile getIndexedFasta( File fastaFile ) {

        IndexedFastaSequenceFile indexedFasta = null;
        try {
            if( fastaFile.exists() && fastaFile.canRead() ) {
                try {
                    indexedFasta = new IndexedFastaSequenceFile( fastaFile ); //Does only work, if index exists
                } catch( FileNotFoundException ex ) {
                    this.indexFasta( fastaFile, this.observers );
                    indexedFasta = this.getIndexedFasta( fastaFile );
                }
            } else {
                JOptionPane.showMessageDialog( new JPanel(), "Reference fasta file is missing or cannot be read! Restore the reference fasta file!",
                        "File not found exception", JOptionPane.ERROR_MESSAGE );
            }
        } catch( NoSuchElementException ex ) { //can occur if the index file is corrupted
            this.indexFasta( fastaFile, this.observers );
            indexedFasta = this.getIndexedFasta( fastaFile );
        }

        return indexedFasta;

    }


    /**
     * Creates a fasta file index and displays a notification while the creation
     * is in progress.
     * <p>
     * @param fastaFile The fasta file to index
     */
    public void recreateMissingIndex( final File fastaFile ) {

        final ProgressHandle progressHandle = ProgressHandleFactory.createHandle( "Fasta index missing, recreating it..." );
        progressHandle.start();

        final IndexFileNotificationPanel indexPanel = new IndexFileNotificationPanel();
        final JButton okButton = new JButton( "OK" );
        okButton.setEnabled( false );
        Thread indexThread = new Thread( new Runnable() {

            @Override
            public void run() {
                indexFasta( fastaFile, observers );
                progressHandle.finish();
                okButton.setEnabled( true );
            }


        } );
        indexThread.start();
        DialogDescriptor dialogDescriptor = new DialogDescriptor( indexPanel, "Fasta index missing!", true, new JButton[]{okButton}, okButton, DialogDescriptor.DEFAULT_ALIGN, null, null );
        Dialog indexDialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        indexDialog.setVisible( true );
    }


}
