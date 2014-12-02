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
package de.cebitec.readXplorer.parser.mappings;

import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.output.JokToBamConverter;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.StatsContainer;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A jok parser, which first reads the jok file, converts it into a bam file
 * sorted by mapping start position and then prepares the new bam file for
 * import into the DB as direct access track.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class JokToBamDirectParser implements MappingParserI, Observer {
    
    private static String name = "Jok to Bam Direct Access Parser";
    private static String[] fileExtension = new String[]{"out", "Jok", "jok", "JOK"};
    private static String fileDescription = "Jok Read Mappings converted to BAM";
    private SamBamParser bamParser;
    private List<Observer> observers;
    private boolean alreadyConverted = false;

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
     * @param trackJob the trackjob to preprocess
     * @return true, if the method succeeded
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Boolean preprocessData(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
        return true;
    }
    
    
    @Override
    public Boolean parseInput(TrackJob trackJob, Map<String, Integer> chromLengthMap) throws ParsingException, OutOfMemoryError {
        
        Boolean success = this.preprocessData(trackJob);
        
        if (success) {
        //parse the newly converted bam file
            bamParser.registerObserver(this);
            success = bamParser.parseInput(trackJob, chromLengthMap);
            bamParser.removeObserver(this);
        } else {
            throw new ParsingException("Preprocessing of the data did not work.");
        }
        
        return success;
    }
    
    /**
     * Converts a jok file into a bam file sorted by mapping start position.
     * Also updates the file in the track job to the new file.
     * @param trackJob the track job containing the jok file
     * @param chromLengthMap the mapping of chromosome name to chromosome length
     * for this track
     * @return true, if the conversion was successful, false otherwise
     * @throws ParsingException
     * @throws OutOfMemoryError 
     */
    @Override
    public Boolean convert(TrackJob trackJob, Map<String, Integer> chromLengthMap) throws ParsingException, OutOfMemoryError {
        Iterator<String> it = chromLengthMap.keySet().iterator();
        boolean success;
        if (it.hasNext()) {
            String chromName = it.next(); //ok, since SARUMAN only supports mapping on a single reference sequence

            //Convert jok file to bam
            JokToBamConverter jokConverter = new JokToBamConverter();
            List<File> jobs = new ArrayList<>();
            jobs.add(trackJob.getFile());
            jokConverter.registerObserver(this);
            jokConverter.setDataToConvert(jobs, chromName, chromLengthMap.get(chromName));
            success = jokConverter.convert();
            jokConverter.removeObserver(this);

            //update the track job with the new bam file
            trackJob.setFile(jokConverter.getOutputFile());
        } else {
            success = false;
        }
        return success;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }
    
    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        for (Observer observer : this.observers) {
            observer.update(data);
        }
    }

    @Override
    public void update(Object args) {
        this.notifyObservers(args);
    }

    /**
     * @return true, if the jok file has already been converted, false otherwise
     */
    public boolean isAlreadyConverted() {
        return this.alreadyConverted;
    }

    @Override
    public void setStatsContainer(StatsContainer statsContainer) {
        this.bamParser.setStatsContainer(statsContainer);
    }
    
}
