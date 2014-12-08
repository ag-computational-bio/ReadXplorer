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

package de.cebitec.readXplorer.parser.output;


import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.SamUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecordIterator;
import org.openide.util.NbBundle;


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
    public boolean sortSamBam( TrackJob trackJob, SAMFileHeader.SortOrder sortOrder, String sortOrderMsg ) {
        boolean success = true;
        this.notifyObservers( Bundle.MSG_SamBamSorter_sort_Start( sortOrderMsg ) );
        long start = System.currentTimeMillis();
        long finish;
        int lineno = 0;
        String msg;
        Pair<SAMFileWriter, File> writerAndFile = null;

        try( SAMFileReader samBamReader = new SAMFileReader( trackJob.getFile() ) ) {
            SAMRecordIterator samItor = samBamReader.iterator();
            SAMFileHeader header = samBamReader.getFileHeader();
            samBamReader.setValidationStringency( SAMFileReader.ValidationStringency.LENIENT );
            if( header.getSortOrder() != SAMFileHeader.SortOrder.queryname ) {
                header.setSortOrder( sortOrder );
                writerAndFile = SamUtils.createSamBamWriter( trackJob.getFile(), header, false, sortOrderMsg );
                SAMFileWriter writer = writerAndFile.getFirst();
                while( samItor.hasNext() ) {
                    try {
                        writer.addAlignment( samItor.next() );
                        if( ++lineno % 500000 == 0 ) {
                            finish = System.currentTimeMillis();
                            this.notifyObservers( Benchmark.calculateDuration( start, finish, lineno + " mappings processed in " ) );
                        }
                    }
                    catch( SAMFormatException e ) {
                        if( !e.getMessage().contains( "MAPQ should be 0" ) ) {
                            this.notifyObservers( e.getMessage() );
                        } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored
                    }
                }
                this.notifyObservers( "Writing sorted bam file... " );
                samItor.close();
                writer.close();

                trackJob.setFile( writerAndFile.getSecond() );
            }

            msg = Bundle.MSG_SamBamSorter_sort_Finish( sortOrderMsg );

        }
        catch( Exception e ) {
            if( writerAndFile != null ) {
                trackJob.setFile( writerAndFile.getSecond() );
            }
            else {
                trackJob.setFile( new File( trackJob.getFile(), sortOrderMsg ) );
            }
            success = false;
            this.notifyObservers( e.getMessage() );
            msg = Bundle.MSG_SamBamSorter_sort_Failed( trackJob.getFile() );
        }
        finish = System.currentTimeMillis();
        this.notifyObservers( Benchmark.calculateDuration( start, finish, msg ) );
        return success;
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
