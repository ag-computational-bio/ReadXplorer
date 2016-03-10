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
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.mappings.CommonsMappingParser;
import de.cebitec.readxplorer.utils.Benchmark;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.SamUtils;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFormatException;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.RuntimeEOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

import static htsjdk.samtools.ValidationStringency.LENIENT;


/**
 * Combines two mapping files (e.g. read 1 and read2 of the same pairs) in one
 * file. The first trackjob contains the new file name afterwards, while the
 * second trackjob contains an empty file name to prevent reuse of it after the
 * combination.
 * <p>
 * @author -Rolf Hilker-
 */
public class SamBamCombiner implements CombinerI {

    private final TrackJob trackJob1;
    private final TrackJob trackJob2;
    private final boolean sortCoordinate;
    private final List<Observer> observers;


    /**
     * Allows to combine two mapping files (e.g. read 1 and read2 of the same
     * pairs) in one file. The first trackjob contains the new file name
     * afterwards, while the second trackjob contains an empty file name to
     * prevent reuse of it after the combination. The merge process is started
     * by calling "combineData".
     * <p>
     * @param trackJob1      containing the first file before the merge and the
     *                       new file name after the merge process
     * @param trackJob2      containing the second file, which is merged with
     *                       the first an its file path is reset to an empty
     *                       string afterwards
     * @param sortCoordinate true, if the combined file should be sorted by
     *                       coordinate and false otherwise
     */
    public SamBamCombiner( TrackJob trackJob1, TrackJob trackJob2, boolean sortCoordinate ) {
        this.trackJob1 = trackJob1;
        this.trackJob2 = trackJob2;
        this.sortCoordinate = sortCoordinate;
        this.observers = new ArrayList<>();
    }


    /**
     * Allows to combine two mapping files (e.g. read 1 and read2 of the same
     * pairs) in one file. The first trackjob contains the new file name
     * afterwards, while the second trackjob contains an empty file name to
     * prevent reuse of it after the combination.
     * <p>
     * @throws de.cebitec.readxplorer.parser.common.ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public boolean combineData() throws ParsingException, OutOfMemoryError {

        boolean success = true;

        final long startTime = System.currentTimeMillis();
        //only proceed if the second track job contains a file
        final File fileToExtend = trackJob1.getFile();
        final File file2 = trackJob2.getFile();
        if( file2.exists() ) { //if all reads already in same file this file is null and no combination needed

            String fileName = fileToExtend.getName();
            this.notifyObservers( NbBundle.getMessage( SamBamCombiner.class, "Combiner.Combine.Start", fileName + " and " + file2.getName() ) );

            SamReaderFactory.setDefaultValidationStringency( LENIENT );
            SamReaderFactory samReaderFactory = SamReaderFactory.make();
            try( final SamReader samBamReader = samReaderFactory.open( fileToExtend );
                 final SamReader samBamReader2 = samReaderFactory.open( file2 );
                 SAMRecordIterator samBamItor = samBamReader.iterator();
                 SAMRecordIterator samBamItor2 = samBamReader2.iterator(); ) {

                SAMFileHeader header = samBamReader.getFileHeader();
                if( sortCoordinate ) {
                    header.setSortOrder( SAMFileHeader.SortOrder.coordinate );
                } else {
                    header.setSortOrder( SAMFileHeader.SortOrder.unsorted );
                }

                //determine writer type (sam or bam):
                Pair<SAMFileWriter, File> writerAndFilePair = SamUtils.createSamBamWriter(
                        fileToExtend, header, !sortCoordinate, SamUtils.COMBINED_STRING );

                try( SAMFileWriter samBamFileWriter = writerAndFilePair.getFirst() ) {
                    File outputFile = writerAndFilePair.getSecond();
                    trackJob1.setFile( outputFile );
                    trackJob2.setFile( new File( "" ) ); //clean file to make sure, it is not used anymore
                    this.readAndWrite( samBamItor, samBamFileWriter, true );
                    this.readAndWrite( samBamItor2, samBamFileWriter, false );

                    if( sortCoordinate ) {
                        success = SamUtils.createBamIndex( outputFile, this );
                    }
                }
            } catch( IOException e ) {
                throw new ParsingException( e );
            }

            long finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage( SamBamCombiner.class, "Combiner.Combine.Finished", fileName + " and " + file2.getName() );
            this.notifyObservers( Benchmark.calculateDuration( startTime, finish, msg ) );
        }

        return success;
    }


    /**
     * Carries out the actual I/O stuff. Also sets the proper read pair flags in
     * the {@link SAMRecord}. Observers are noticed in case a read cannot be
     * processed.
     * <p>
     * @param samBamItor       the iterator to read sam records from
     * @param samBamFileWriter the writer to write to
     * @param isFstFile        true, if this is the file containing read1, false
     *                         if this is the file containing read2 of the pairs
     */
    private void readAndWrite( final SAMRecordIterator samBamItor, final SAMFileWriter samBamFileWriter, final boolean isFstFile ) {

        final long startTime = System.currentTimeMillis();
        int noReads = 0;
        SAMRecord record = new SAMRecord( null );
        while( samBamItor.hasNext() ) {
            try {
                record = samBamItor.next();
                record.setReadPairedFlag( true );
                record.setFirstOfPairFlag( isFstFile );
                record.setSecondOfPairFlag( !isFstFile );
                record.setMateUnmappedFlag( true ); //we do not know whether mate from other file is mapped or not
                CommonsMappingParser.checkOrRemovePairTag( record );
                samBamFileWriter.addAlignment( record );
            } catch( RuntimeEOFException e ) {
                this.notifyObservers( "Read could not be added to new file: " + record.getReadName() );
            } catch( SAMFormatException e ) {
                if( !e.getMessage().contains( "MAPQ should be 0" ) ) {
                    this.notifyObservers( e.getMessage() );
                } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored
            }
            if( ++noReads % 500000 == 0 ) {
                long finish = System.currentTimeMillis();
                this.notifyObservers( Benchmark.calculateDuration( startTime, finish, noReads + " reads converted..." ) );
            }
        }
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


    @Override
    public void update( Object args ) {
        this.notifyObservers( args );
    }


}
