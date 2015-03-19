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

package de.cebitec.readxplorer.parser.mappings;


import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.output.JokToBamConverter;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.StatsContainer;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * A jok parser, which first reads the jok file, converts it into a bam file
 * sorted by mapping start position and then prepares the new bam file for
 * import into the DB as direct access track.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class JokToBamDirectParser implements MappingParserI, Observer {

    private static final String NAME = "Jok to Bam Direct Access Parser";
    private static final String[] FILE_EXTENSION = new String[]{ "out", "Jok", "jok", "JOK" };
    private static final String FILE_DESCRIPTION = "Jok Read Mappings converted to BAM";
    private final SamBamParser bamParser;
    private final List<Observer> observers;
    private final boolean alreadyConverted = false;


    /**
     * A jok parser, which first reads the jok file, converts it into a bam file
     * sorted by mapping start position and then prepares the new bam file for
     * import into the DB as direct access track.
     */
    public JokToBamDirectParser() {
        this.observers = new ArrayList<>();
        this.bamParser = new SamBamParser();
    }


    /**
     * Not implemented for this parser implementation, as currently no
     * preprocessing is needed.
     * <p>
     * @param trackJob the trackjob to preprocess
     * <p>
     * @return true, if the method succeeded
     * <p>
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Boolean preprocessData( TrackJob trackJob ) throws ParsingException, OutOfMemoryError {
        return true;
    }


    @Override
    public Boolean parseInput( final TrackJob trackJob, final Map<String, Integer> chromLengthMap ) throws ParsingException, OutOfMemoryError {

        Boolean success = this.preprocessData( trackJob );

        if( success ) {
            //parse the newly converted bam file
            bamParser.registerObserver( this );
            success = bamParser.parseInput( trackJob, chromLengthMap );
            bamParser.removeObserver( this );
        } else {
            throw new ParsingException( "Preprocessing of the data did not work." );
        }

        return success;
    }


    /**
     * Converts a jok file into a bam file sorted by mapping start position.
     * Also updates the file in the track job to the new file.
     * <p>
     * @param trackJob       the track job containing the jok file
     * @param chromLengthMap the mapping of chromosome NAME to chromosome length
     *                       for this track
     * <p>
     * @return true, if the conversion was successful, false otherwise
     * <p>
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Boolean convert( final TrackJob trackJob, final Map<String, Integer> chromLengthMap ) throws ParsingException, OutOfMemoryError {

        final boolean success;
        final Iterator<String> it = chromLengthMap.keySet().iterator();
        if( it.hasNext() ) {
            final String chromName = it.next(); //ok, since SARUMAN only supports mapping on a single reference sequence
            //TODO: might still be used in conjunction with a later merged multiple fasta file -> make sure the correct ref is used!
            //Convert jok file to bam
            final JokToBamConverter jokConverter = new JokToBamConverter();
            List<File> jobs = new ArrayList<>();
            jobs.add( trackJob.getFile() );
            jokConverter.registerObserver( this );
            jokConverter.setDataToConvert( jobs, chromName, chromLengthMap.get( chromName ) );
            success = jokConverter.convert();
            jokConverter.removeObserver( this );

            //update the track job with the new bam file
            trackJob.setFile( jokConverter.getOutputFile() );
        } else {
            success = false;
        }
        return success;
    }


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public String getInputFileDescription() {
        return FILE_DESCRIPTION;
    }


    @Override
    public String[] getFileExtensions() {
        return FILE_EXTENSION;
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
    public void notifyObservers( final Object data ) {
        for( Observer observer : this.observers ) {
            observer.update( data );
        }
    }


    @Override
    public void update( Object args ) {
        this.notifyObservers( args );
    }


    /**
     * @return true, if the jok file has already been converted, false otherwise
     */
    public boolean isAlreadyConverted() {
        return this.alreadyConverted;
    }


    @Override
    public void setStatsContainer( StatsContainer statsContainer ) {
        this.bamParser.setStatsContainer( statsContainer );
    }


}
