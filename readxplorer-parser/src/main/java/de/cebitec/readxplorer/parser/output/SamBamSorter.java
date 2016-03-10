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

package de.cebitec.readxplorer.parser.output;


import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.utils.Benchmark;
import de.cebitec.readxplorer.utils.Observable;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.SamUtils;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFormatException;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

import static htsjdk.samtools.ValidationStringency.LENIENT;


/**
 * Sorts a sam or bam file according to the specified SamFileHeader.SortOrder
 * and sets the new sorted file as the file in the given TrackJob.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamBamSorter implements Observable {

    private final List<Observer> observers;


    /**
     * Sorts a sam or bam file according to the specified
     * SamFileHeader.SortOrder and sets the new sorted file as the file in the
     * given TrackJob.
     */
    public SamBamSorter() {
        this.observers = new ArrayList<>();
    }


    /**
     * Sorts a sam or bam file according to the specified
     * SamFileHeader.SortOrder and sets the new sorted file as the file in the
     * trackJob.
     * <p>
     * @param trackJob     track job containing the file to sort
     * @param sortOrder    the sort order to use
     * @param sortOrderMsg the string representation of the sort order for
     *                     status messages
     * <p>
     * @return true, if the sorting was successful, false otherwise
     */
    @NbBundle.Messages( {
        "# {0} - sort order",
        "MSG_SamBamSorter.sort.Start=Start sorting file by {0}...",
        "# {0} - sort order",
        "MSG_SamBamSorter.sort.Finish=Finished sorting file by {0}. ",
        "# {0} - track file",
        "MSG_SamBamSorter.sort.Failed=Failed sorting file {0}, therefore the file cannot be imported." } )
    public boolean sortSamBam( final TrackJob trackJob, final SAMFileHeader.SortOrder sortOrder, final String sortOrderMsg ) {

        boolean success = true;
        this.notifyObservers( Bundle.MSG_SamBamSorter_sort_Start( sortOrderMsg ) );
        final long start = System.currentTimeMillis();
        int lineNo = 0;
        String msg;
        Pair<SAMFileWriter, File> writerAndFile = null;

        SamReaderFactory.setDefaultValidationStringency( LENIENT );
        SamReaderFactory samReaderFactory = SamReaderFactory.make();
        try( final SamReader samBamReader = samReaderFactory.open( trackJob.getFile() );
             SAMRecordIterator samItor = samBamReader.iterator(); ) {
            
            SAMFileHeader header = samBamReader.getFileHeader();
            if( header.getSortOrder() != SAMFileHeader.SortOrder.queryname ) {
                header.setSortOrder( sortOrder );
                writerAndFile = SamUtils.createSamBamWriter( trackJob.getFile(), header, false, sortOrderMsg );
                SAMFileWriter writer = writerAndFile.getFirst();
                while( samItor.hasNext() ) {
                    try {
                        writer.addAlignment( samItor.next() );
                        if( ++lineNo % 500000 == 0 ) {
                            long finish = System.currentTimeMillis();
                            this.notifyObservers( Benchmark.calculateDuration( start, finish, lineNo + " mappings processed in " ) );
                        }
                    } catch( SAMFormatException e ) {
                        if( !e.getMessage().contains( "MAPQ should be 0" ) ) {
                            this.notifyObservers( e.getMessage() );
                        } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored
                    }
                }
                this.notifyObservers( "Writing sorted bam file... " );
                writer.close();

                trackJob.setFile( writerAndFile.getSecond() );
            }

            msg = Bundle.MSG_SamBamSorter_sort_Finish( sortOrderMsg );

        } catch( Exception e ) {
            if( writerAndFile != null ) {
                trackJob.setFile( writerAndFile.getSecond() );
            } else {
                trackJob.setFile( new File( trackJob.getFile(), sortOrderMsg ) );
            }
            success = false;
            this.notifyObservers( e.getMessage() );
            msg = Bundle.MSG_SamBamSorter_sort_Failed( trackJob.getFile() );
        }
        long finish = System.currentTimeMillis();
        this.notifyObservers( Benchmark.calculateDuration( start, finish, msg ) );
        return success;
    }


    @Override
    public void registerObserver( final Observer observer ) {
        this.observers.add( observer );
    }


    @Override
    public void removeObserver( final Observer observer ) {
        this.observers.remove( observer );
    }


    @Override
    public void notifyObservers( final Object data ) {
        for( Observer observer : this.observers ) {
            observer.update( data );
        }
    }


}
